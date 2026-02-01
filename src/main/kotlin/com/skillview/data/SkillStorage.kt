package com.skillview.data

import com.skillview.util.getDeepString
import com.skillview.util.hasCustomTag
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.event.SubscribeEvent
import taboolib.expansion.getDataContainer
import taboolib.platform.util.isAir
import top.maplex.arim.tools.gson.GsonUtils
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 技能与MOD存储管理器
 * 
 * 性能优化:
 * - 三层缓存: Loadout缓存 -> SkillId缓存 -> 脏标记
 * - 只在数据变化时刷新缓存
 * - 玩家退出时自动清理
 */
object SkillStorage {

    // ==========================================
    //          数据结构定义
    // ==========================================

    data class ModLoadout(
        var isCapacityUpgraded: Boolean = false,
        val mods: MutableMap<Int, ItemStack> = mutableMapOf(),
        val slotPolarities: MutableMap<Int, String> = mutableMapOf()
    )

    data class SkillLoadout(
        val slots: MutableMap<Int, ItemStack> = mutableMapOf()
    )

    // ==========================================
    //          缓存层 (性能优化核心)
    // ==========================================

    /** 技能装备缓存 */
    private val skillLoadoutCache = ConcurrentHashMap<UUID, SkillLoadout>()
    
    /** MOD装备缓存 */
    private val modLoadoutCache = ConcurrentHashMap<UUID, ModLoadout>()
    
    /** 技能ID预解析缓存 - 避免每tick解析JSON */
    private val skillIdCache = ConcurrentHashMap<UUID, Array<String?>>()
    
    /** 脏标记集合 - 只有标记为脏的玩家才需要刷新缓存 */
    private val dirtySkillPlayers = ConcurrentHashMap.newKeySet<UUID>()
    private val dirtyModPlayers = ConcurrentHashMap.newKeySet<UUID>()

    // ==========================================
    //          脏标记管理
    // ==========================================

    /**
     * 标记玩家技能数据需要刷新
     * 在技能装备菜单关闭时调用
     */
    fun markSkillDirty(player: Player) {
        dirtySkillPlayers.add(player.uniqueId)
    }

    /**
     * 标记玩家MOD数据需要刷新
     * 在MOD装备菜单关闭时调用
     */
    fun markModDirty(player: Player) {
        dirtyModPlayers.add(player.uniqueId)
    }

    /**
     * 检查技能缓存是否需要刷新
     */
    fun isSkillCacheDirty(player: Player): Boolean = dirtySkillPlayers.contains(player.uniqueId)

    /**
     * 检查MOD缓存是否需要刷新
     */
    fun isModCacheDirty(player: Player): Boolean = dirtyModPlayers.contains(player.uniqueId)

    // ==========================================
    //          JSON解析工具
    // ==========================================

    private inline fun <reified T> parseJson(json: String, default: T): T {
        if (json.isEmpty()) return default
        return try {
            GsonUtils.fromJson(json, T::class.java)
        } catch (e: Exception) {
            default
        }
    }

    // ==========================================
    //          技能装备管理 (带缓存)
    // ==========================================

    /**
     * 获取技能装备 (优先使用缓存)
     */
    fun getSkillLoadout(player: Player): SkillLoadout {
        val uuid = player.uniqueId
        
        // 如果缓存有效且未标记为脏，直接返回
        if (!dirtySkillPlayers.contains(uuid)) {
            skillLoadoutCache[uuid]?.let { return it }
        }
        
        // 从数据库加载并缓存
        return loadSkillLoadoutFromDB(player).also { loadout ->
            skillLoadoutCache[uuid] = loadout
            refreshSkillIdCache(player, loadout)
            dirtySkillPlayers.remove(uuid)
        }
    }

    /**
     * 从数据库加载技能装备 (内部方法)
     */
    private fun loadSkillLoadoutFromDB(player: Player): SkillLoadout {
        val json = player.getDataContainer()["active_skill_loadout"] ?: ""
        return parseJson(json, SkillLoadout())
    }

    /**
     * 刷新技能ID缓存 (预解析所有槽位的技能ID)
     */
    private fun refreshSkillIdCache(player: Player, loadout: SkillLoadout) {
        val ids = Array(5) { i ->
            loadout.slots[i]?.let { item ->
                if (item.isAir()) return@let null
                val id = item.getDeepString("技能书基础属性.技能id")
                if (id.isEmpty() || id == "none") null else id
            }
        }
        skillIdCache[player.uniqueId] = ids
    }

    fun saveSkillLoadout(player: Player, loadout: SkillLoadout) {
        player.getDataContainer()["active_skill_loadout"] = GsonUtils.toJson(loadout)
        // 保存后更新缓存
        skillLoadoutCache[player.uniqueId] = loadout
        refreshSkillIdCache(player, loadout)
        dirtySkillPlayers.remove(player.uniqueId)
    }

    fun updateSkillSlot(player: Player, slotIndex: Int, item: ItemStack?) {
        val loadout = getSkillLoadout(player)
        if (item == null || item.isAir() || !item.hasCustomTag("技能书基础属性.技能id")) {
            loadout.slots.remove(slotIndex)
        } else {
            loadout.slots[slotIndex] = item
        }
        saveSkillLoadout(player, loadout)
    }

    fun getSkillItem(player: Player, slotIndex: Int): ItemStack? =
        getSkillLoadout(player).slots[slotIndex]

    /**
     * 获取技能ID (使用缓存，极大减少JSON解析)
     * 
     * 性能: 从每tick解析5次JSON -> 仅在数据变化时解析1次
     */
    fun getSkillId(player: Player, slotIndex: Int): String? {
        val uuid = player.uniqueId
        
        // 如果需要刷新，先刷新缓存
        if (dirtySkillPlayers.contains(uuid) || !skillIdCache.containsKey(uuid)) {
            getSkillLoadout(player) // 这会触发缓存刷新
        }
        
        return skillIdCache[uuid]?.getOrNull(slotIndex)
    }

    // ==========================================
    //          MOD装备管理 (带缓存)
    // ==========================================

    /**
     * 获取MOD装备 (优先使用缓存)
     */
    fun getModLoadout(player: Player): ModLoadout {
        val uuid = player.uniqueId
        
        // 如果缓存有效且未标记为脏，直接返回
        if (!dirtyModPlayers.contains(uuid)) {
            modLoadoutCache[uuid]?.let { return it }
        }
        
        // 从数据库加载并缓存
        return loadModLoadoutFromDB(player).also { loadout ->
            modLoadoutCache[uuid] = loadout
            dirtyModPlayers.remove(uuid)
        }
    }

    /**
     * 从数据库加载MOD装备 (内部方法)
     */
    private fun loadModLoadoutFromDB(player: Player): ModLoadout {
        val json = player.getDataContainer()["player_mod_loadout"] ?: ""
        return parseJson(json, ModLoadout())
    }

    fun saveModLoadout(player: Player, loadout: ModLoadout) {
        player.getDataContainer()["player_mod_loadout"] = GsonUtils.toJson(loadout)
        // 保存后更新缓存
        modLoadoutCache[player.uniqueId] = loadout
        dirtyModPlayers.remove(player.uniqueId)
    }

    fun installMod(player: Player, slotIndex: Int, item: ItemStack) {
        val loadout = getModLoadout(player)
        loadout.mods[slotIndex] = item
        saveModLoadout(player, loadout)
    }

    fun uninstallMod(player: Player, slotIndex: Int): ItemStack? {
        val loadout = getModLoadout(player)
        val removed = loadout.mods.remove(slotIndex)
        if (removed != null) saveModLoadout(player, loadout)
        return removed
    }

    // ==========================================
    //          星愿点数管理
    // ==========================================

    fun getStarPoints(player: Player): Int {
        val container = player.getDataContainer()
        val rawValue = container["star_points"]
        if (rawValue == null) {
            container["star_points"] = "0"
            return 0
        }
        return rawValue.toIntOrNull() ?: 0
    }

    fun setStarPoints(player: Player, amount: Int) {
        player.getDataContainer()["star_points"] = amount.coerceAtLeast(0).toString()
    }

    fun addStarPoints(player: Player, amount: Int) {
        setStarPoints(player, getStarPoints(player) + amount)
    }

    fun takeStarPoints(player: Player, amount: Int): Boolean {
        val current = getStarPoints(player)
        if (current < amount) return false
        setStarPoints(player, current - amount)
        return true
    }

    // ==========================================
    //          生命周期管理
    // ==========================================

    @SubscribeEvent
    fun onQuit(e: PlayerQuitEvent) {
        val uuid = e.player.uniqueId
        skillLoadoutCache.remove(uuid)
        modLoadoutCache.remove(uuid)
        skillIdCache.remove(uuid)
        dirtySkillPlayers.remove(uuid)
        dirtyModPlayers.remove(uuid)
    }
}
