package com.skillview.core.skill

import com.skillview.core.rpg.RpgRuntime
import com.skillview.data.RpgDefinitions
import com.skillview.data.SkillStorage
import com.skillview.util.round
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
import kotlin.math.abs

object SkillPacketSender {

    const val UI_ID = "SkillHud"
    private const val AIR_ICON = "air"
    private val DEBUG get() = RpgDefinitions.Config.DEBUG
    private const val CD_THRESHOLD = 0.1

    private val cdCache = ConcurrentHashMap<UUID, DoubleArray>()
    private val iconCache = ConcurrentHashMap<UUID, Array<String?>>()
    private val lastCdSent = ConcurrentHashMap<UUID, DoubleArray>()

    private fun updateCdCache(player: Player) {
        val arr = cdCache.computeIfAbsent(player.uniqueId) { DoubleArray(5) { -1.0 } }
        val uuid = player.uniqueId
        for (i in 0..4) {
            val id = SkillStorage.getSkillId(player, i)
            arr[i] = if (id != null) {
                RpgRuntime.checkCooldown(player, id).coerceAtLeast(0.0)
            } else {
                -1.0
            }
        }
    }

    private fun refreshIconCache(player: Player) {
        val arr = arrayOfNulls<String>(5)
        for (i in 0..4) {
            val id = SkillStorage.getSkillId(player, i) ?: continue
            arr[i] = "SkillTextures/${id.lowercase()}.png"
        }
        iconCache[player.uniqueId] = arr
    }

    @Awake(LifeCycle.ACTIVE)
    fun startUpdateTask() {
        submit(period = 5L) {
            onlinePlayers().forEach { p ->
                val player = p.cast<Player>()
                updateCdCache(player)
                if (shouldSendCdPacket(player)) {
                    sendCdPacket(player)
                }
            }
        }
    }

    private fun shouldSendCdPacket(player: Player): Boolean {
        val uuid = player.uniqueId
        val current = cdCache[uuid] ?: return false
        val last = lastCdSent[uuid]

        if (last == null) {
            lastCdSent[uuid] = current.copyOf()
            return true
        }

        for (i in 0..4) {
            val diff = abs(current[i] - last[i])
            if (diff > CD_THRESHOLD || (last[i] > 0 && current[i] <= 0)) {
                lastCdSent[uuid] = current.copyOf()
                return true
            }
        }
        return false
    }

    private fun sendCdPacket(player: Player) {
        val arr = cdCache[player.uniqueId] ?: return
        val data = HashMap<String, Any>(5)
        for (i in 1..5) {
            val v = arr[i - 1]
            data["skill${i}_cd"] = if (v >= 0) v.round() else 0.0
        }
        ArcartXUIRegistry.sendPacket(player, UI_ID, "update_skills_cd", data)
    }

    private val debouncedIconUpdate = debounce<Player>(500) { p ->
        refreshIconCache(p)
        sendIconPacket(p)
    }

    fun sendSkillIdPacket(player: Player) {
        debouncedIconUpdate(player)
    }

    private fun sendIconPacket(player: Player) {
        val arr = iconCache[player.uniqueId] ?: return
        val data = HashMap<String, Any>(5)
        for (i in 1..5) {
            data["skill$i"] = arr[i - 1] ?: AIR_ICON
        }
        if (DEBUG) println("[Debug] 发送图标更新包: ${player.name} -> $data")
        ArcartXUIRegistry.sendPacket(player, UI_ID, "update_skills_icon", data)
    }

    @SubscribeEvent
    fun onQuit(e: PlayerQuitEvent) {
        val uuid = e.player.uniqueId
        cdCache.remove(uuid)
        iconCache.remove(uuid)
        lastCdSent.remove(uuid)
    }
}
