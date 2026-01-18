package com.skillview.util

import com.skillview.core.rpg.RpgRuntime
import com.skillview.data.RpgDefinitions
import com.skillview.data.SkillStorage
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import priv.seventeen.artist.arcartx.core.ui.ArcartXUIRegistry
import taboolib.common.LifeCycle
import taboolib.common.function.debounce
import taboolib.common.platform.Awake
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.platform.function.submit
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object SkillPacketSender {

    const val UI_ID = "SkillHud"
    private const val AIR_ICON = "air"
    private val DEBUG = RpgDefinitions.Config.DEBUG

    /* ===== 两条完全独立的缓存 ===== */
    // CD 缓存：index 0~4 对应技能 1~5，-1 表示该槽无技能
    private val cdCache   = ConcurrentHashMap<UUID, DoubleArray>()
    // 图标缓存：index 0~4 对应技能 1~5，null 表示该槽无技能
    private val iconCache = ConcurrentHashMap<UUID, Array<String?>>()

    /* ===== CD 刷新 ===== */
    private fun refreshCdCache(player: Player) {
        val arr = DoubleArray(5) { -1.0 }
        val uuid = player.uniqueId
        for (i in 0..4) {
            val id = SkillStorage.getSkillId(player, i) ?: continue
            arr[i] = RpgRuntime.checkCooldown(player, id).coerceAtLeast(0.0)
        }
        cdCache[uuid] = arr
    }

    /* ===== 图标刷新 ===== */
    private fun refreshIconCache(player: Player) {
        val arr = arrayOfNulls<String>(5)
        val uuid = player.uniqueId
        for (i in 0..4) {
            val id = SkillStorage.getSkillId(player, i) ?: continue
            arr[i] = "SkillTextures/${id.lowercase()}.png"
        }
        iconCache[uuid] = arr
    }

    /* ===== 高频任务：只发 CD ===== */
    @Awake(LifeCycle.ACTIVE)
    fun startUpdateTask() {
        submit(period = 2L) {
            onlinePlayers().forEach { p ->
                refreshCdCache(p.cast())          // 每 2tick 刷新一次 CD 缓存
                sendCdPacket(p.cast())              // 立即发包
            }
        }
    }

    private fun sendCdPacket(player: Player) {
        val arr = cdCache[player.uniqueId] ?: return
        val data = hashMapOf<String, Any>()
        for (i in 1..5) {
            val v = arr[i - 1]
            data["skill${i}_cd"] = if (v >= 0) v.round1() else 0.0
        }
        ArcartXUIRegistry.sendPacket(player, UI_ID, "update_skills_cd", data)
    }

    /* ===== 图标发包：带防抖 ===== */
    private val debouncedIconUpdate = debounce<Player>(500) { p ->
        refreshIconCache(p)          // 真正刷新图标缓存
        sendIconPacket(p)             // 立即发包
    }

    fun sendSkillIdPacket(player: Player) {
        // 外部调用触发图标更新
        debouncedIconUpdate(player)
    }

    private fun sendIconPacket(player: Player) {
        val arr = iconCache[player.uniqueId] ?: return
        val data = hashMapOf<String, Any>()
        for (i in 1..5) {
            data["skill$i"] = arr[i - 1] ?: AIR_ICON
        }
        if (DEBUG) println("[Debug] 发送图标更新包: ${player.name} -> $data")
        ArcartXUIRegistry.sendPacket(player, UI_ID, "update_skills_icon", data)
    }

    /* ===== 退出清理 ===== */
    @SubscribeEvent
    fun onQuit(e: PlayerQuitEvent) {
        val uuid = e.player.uniqueId
        cdCache.remove(uuid)
        iconCache.remove(uuid)
    }
}