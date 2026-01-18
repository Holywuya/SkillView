package com.skillview.core.mod

import com.skillview.data.RpgDefinitions
import com.skillview.data.SkillStorage
import com.skillview.util.getDeepDouble
import com.skillview.util.getDeepString
import com.skillview.util.hasCustomTag
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.module.nms.getItemTag
import taboolib.platform.util.isAir
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object ModRuntime {

    // 存储玩家通过 MOD 系统获得的全局加成
    private val playerModStats = ConcurrentHashMap<UUID, ModStats>()

    // NBT 路径常量
    private const val MOD_NBT_ROOT = RpgDefinitions.ModNBT.ROOT_MOD

    /**
     * MOD 全局属性统计类
     */
    class ModStats {
        private val values = ConcurrentHashMap<String, Double>()

        fun add(attribute: String, value: Double) {
            val current = values.getOrDefault(attribute, 0.0)
            values[attribute] = current + value
        }

        // --- 业务层快捷访问器 ---
        val extraMana: Double   get() = values.getOrDefault("魔力上限", 0.0)
        val efficiency: Double  get() = values.getOrDefault("技能效率", 0.0)
        val cdReduction: Double get() = values.getOrDefault("冷却缩减", 0.0)
        val maxHp: Double       get() = values.getOrDefault("最大生命", 0.0)
        val damageBonus: Double get() = values.getOrDefault("伤害加成", 0.0)
        val damageMore: Double  get() = values.getOrDefault("最终伤害", 0.0)
        val extraRange: Double  get() = values.getOrDefault("额外范围", 0.0)
        val critRate: Double    get() = values.getOrDefault("暴击几率", 0.0)
        val critDamage: Double  get() = values.getOrDefault("暴击伤害", 0.0)
        val skillPower: Double   get() = values.getOrDefault("技能强度", 0.0)
        val manaRegen: Double    get() = values.getOrDefault("魔力恢复", 0.0)
    }

    // --- [核心：进退服逻辑完善] ---

    @SubscribeEvent
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player
        val uuid = player.uniqueId

        // 初始化一个空的 Stats 对象，防止加载延迟期间 getStats 报错
        playerModStats[uuid] = ModStats()

        // 采用 100 刻延迟读取（约 5 秒），确保数据库和属性插件全部加载完毕
        submit(delay = 100L) {
            if (player.isOnline) {
                recalculate(player)
                // println("[Debug] 玩家 ${player.name} 的 MOD 全局属性已加载")
            }
        }
    }

    @SubscribeEvent
    fun onQuit(e: PlayerQuitEvent) {
        // 玩家离线，立即清理内存缓存
        removeCache(e.player.uniqueId)
    }

    // --- [逻辑操作] ---

    /**
     * 重新计算玩家的 MOD 属性汇总
     * (在 进服、装配/卸下 MOD 时调用)
     */
    fun recalculate(player: Player) {
        val loadout = SkillStorage.getModLoadout(player)
        val stats = ModStats()

        val attributes = try {
            RpgDefinitions.MOD_GLOBAL_ATTRIBUTES
        } catch (e: Exception) {
            listOf("魔力上限", "冷却缩减", "伤害加成", "技能效率")
        }

        loadout.mods.values.forEach { item ->
            if (item.isAir()) return@forEach

            attributes.forEach { attrName ->
                val path = "$MOD_NBT_ROOT.$attrName"
                val value = item.getDeepDouble(path, 0.0)
                if (value != 0.0) {
                    stats.add(attrName, value)
                }
            }
        }
        playerModStats[player.uniqueId] = stats
    }

    /**
     * 获取玩家当前的 MOD 属性缓存
     */
    fun getStats(player: Player): ModStats {
        return playerModStats[player.uniqueId] ?: ModStats()
    }

    /**
     * 清理缓存
     */
    fun removeCache(uuid: UUID) {
        playerModStats.remove(uuid)
    }
}

object SkillUpgradeLogic {

    // 缓存属性列表
    private val ATTRIBUTES_LIST by lazy {
        try {
            RpgDefinitions.UPGRADEABLE_ATTRIBUTES
        } catch (e: NoClassDefFoundError) {
            listOf("基础伤害", "最终伤害", "额外范围", "冷却缩减")
        }
    }

    /**
     * 将强化石的属性叠加到技能书上
     * 逻辑：强化石有 -> 技能书就加 (技能书没有该路径则自动创建)
     */
    fun applyModAttributes(book: ItemStack, stone: ItemStack): Boolean {
        // 1. 基础校验
        if (book.isAir() || stone.isAir()) return false
        if (book.getDeepString("类型") != "技能书") return false
        if (stone.getDeepString("类型") != "技能Mod") return false

        val bookTag = book.getItemTag()
        var hasChanges = false

        // 2. 循环处理所有定义的属性
        ATTRIBUTES_LIST.forEach { attrName ->
            val sourcePath = "Mod属性.$attrName"
            val targetPath = "技能书属性强化.$attrName"

            // A. 读取强化石数值
            val addValue = stone.getDeepDouble(sourcePath, 0.0)

            // B. 只有当强化石有这个属性时，才操作
            if (addValue != 0.0) {
                // C. 读取技能书当前数值
                // 如果技能书本来没有这个节点，getDeepDouble 默认返回 0.0
                val currentVal = bookTag.getDeepDouble(targetPath, 0.0)

                // D. 累加并写入 (核心：putDeep 会自动创建缺失的路径)
                // 效果：0.0 + 10.0 = 10.0 (新增) 或 5.0 + 10.0 = 15.0 (累加)
                bookTag.putDeep(targetPath, currentVal + addValue)
                hasChanges = true
            }
        }

        // 3. 保存更改
        if (hasChanges) {
            bookTag.saveTo(book)
            return true
        }
        return false
    }

    /**
     * 从技能书中移除强化石属性
     */
    fun removeModAttributes(book: ItemStack, stone: ItemStack): Boolean {
        if (book.isAir() || stone.isAir()) return false
        // 拆解时只要有ID即可，放宽类型检查防止bug
        if (!book.hasCustomTag("技能书基础属性.技能id")) return false
        if (stone.getDeepString("类型") != "技能Mod") return false

        val bookTag = book.getItemTag()
        var hasChanges = false

        ATTRIBUTES_LIST.forEach { attrName ->
            val sourcePath = "强化石属性强化.$attrName"
            val targetPath = "Mod属性.$attrName"

            val removeValue = stone.getDeepDouble(sourcePath, 0.0)

            if (removeValue != 0.0) {
                // 读取当前值
                val currentVal = bookTag.getDeepDouble(targetPath, 0.0)

                // 执行减法，防止负数
                val newValue = (currentVal - removeValue).coerceAtLeast(0.0)

                if (newValue != currentVal) {
                    // 更新数值
                    bookTag.putDeep(targetPath, newValue)
                    hasChanges = true
                }
            }
        }

        if (hasChanges) {
            bookTag.saveTo(book)
            return true
        }
        return false
    }
}