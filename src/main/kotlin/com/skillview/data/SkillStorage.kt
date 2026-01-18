package com.skillview.data

import com.skillview.util.getDeepString
import com.skillview.util.hasCustomTag
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.expansion.getDataContainer
import taboolib.platform.util.isAir
import top.maplex.arim.tools.gson.GsonUtils

object SkillStorage {

    // ==========================================
    //           数据结构定义 (Data Classes)
    // ==========================================

    /**
     * 玩家的 MOD 配置方案 (Warframe 风格)
     */
    data class ModLoadout(
        var isCapacityUpgraded: Boolean = false, // 反应堆扩容状态
        val mods: MutableMap<Int, ItemStack> = mutableMapOf() // 槽位 0-7 -> MOD物品
    )

    /**
     * 玩家的主动技能栏方案
     */
    data class SkillLoadout(
        val slots: MutableMap<Int, ItemStack> = mutableMapOf() // 槽位 0-4 -> 技能书
    )

    // ==========================================
    //        第一部分：主动技能栏 (Skills)
    // ==========================================

    /** 获取技能栏配置 */
    fun getSkillLoadout(player: Player): SkillLoadout {
        val json = player.getDataContainer()["active_skill_loadout"] ?: ""
        return if (json.isEmpty()) SkillLoadout()
        else try { GsonUtils.fromJson(json, SkillLoadout::class.java) } catch (e: Exception) { SkillLoadout() }
    }

    /** 保存技能栏配置 */
    fun saveSkillLoadout(player: Player, loadout: SkillLoadout) {
        player.getDataContainer()["active_skill_loadout"] = GsonUtils.toJson(loadout)
    }

    /** 更新特定技能槽位 */
    fun updateSkillSlot(player: Player, slotIndex: Int, item: ItemStack?) {
        val loadout = getSkillLoadout(player)
        if (item == null || item.isAir() || !item.hasCustomTag("技能书基础属性.技能id")) {
            loadout.slots.remove(slotIndex)
        } else {
            loadout.slots[slotIndex] = item
        }
        saveSkillLoadout(player, loadout)
    }

    /** 获取指定槽位的技能物品 */
    fun getSkillItem(player: Player, slotIndex: Int): ItemStack? {
        return getSkillLoadout(player).slots[slotIndex]
    }

    /** 获取指定槽位的技能 ID (供 Caster 使用) */
    fun getSkillId(player: Player, slotIndex: Int): String? {
        val item = getSkillItem(player, slotIndex) ?: return null
        val id = item.getDeepString("技能书基础属性.技能id")
        return if (id.isEmpty() || id == "none") null else id
    }

    // ==========================================
    //        第二部分：MOD 负载系统 (MODs)
    // ==========================================

    /** 获取 MOD 配置 */
    fun getModLoadout(player: Player): ModLoadout {
        val json = player.getDataContainer()["player_mod_loadout"] ?: ""
        return if (json.isEmpty()) ModLoadout()
        else try { GsonUtils.fromJson(json, ModLoadout::class.java) } catch (e: Exception) { ModLoadout() }
    }

    /** 保存 MOD 配置 */
    fun saveModLoadout(player: Player, loadout: ModLoadout) {
        player.getDataContainer()["player_mod_loadout"] = GsonUtils.toJson(loadout)
    }

    /** 安装 MOD */
    fun installMod(player: Player, slotIndex: Int, item: ItemStack) {
        val loadout = getModLoadout(player)
        loadout.mods[slotIndex] = item
        saveModLoadout(player, loadout)
    }

    /** 卸载 MOD */
    fun uninstallMod(player: Player, slotIndex: Int): ItemStack? {
        val loadout = getModLoadout(player)
        val removed = loadout.mods.remove(slotIndex)
        if (removed != null) saveModLoadout(player, loadout)
        return removed
    }

    /**
     * ==========================================
     *        第三部分：星源点 (Star Points)
     * ==========================================
     */

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
        val container = player.getDataContainer()
        val safeAmount = amount.coerceAtLeast(0)
        container["star_points"] = safeAmount.toString()
    }

    fun addStarPoints(player: Player, amount: Int) {
        val current = getStarPoints(player)
        setStarPoints(player, current + amount)
    }

    fun takeStarPoints(player: Player, amount: Int): Boolean {
        val current = getStarPoints(player)
        if (current < amount) {
            return false
        }
        setStarPoints(player, current - amount)
        return true
    }
}