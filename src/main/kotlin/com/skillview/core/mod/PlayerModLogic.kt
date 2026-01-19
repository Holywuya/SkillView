package com.skillview.core.mod

import com.skillview.core.mod.ModRuntime.ModStats
import com.skillview.data.RpgDefinitions
import com.skillview.data.SkillStorage
import com.skillview.util.getDeepDouble
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.isAir
import java.util.*
import java.util.concurrent.ConcurrentHashMap


object PlayerModLogic {

    private val playerModStats = ConcurrentHashMap<UUID, ModStats>()
    private const val MOD_NBT_ROOT = RpgDefinitions.ModNBT.ROOT_MOD


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