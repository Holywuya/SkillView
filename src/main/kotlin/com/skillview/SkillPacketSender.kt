package com.skillview

import com.skillview.rpgCore.RpgRuntime
import com.skillview.expansion.round1
import com.skillview.rpgCore.SkillCaster.conf
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.platform.function.submit
import taboolib.common.function.debounce
import priv.seventeen.artist.arcartx.core.ui.ArcartXUIRegistry
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.event.player.PlayerQuitEvent

object SkillPacketSender {

    const val UI_ID = "SkillHud"
    private const val AIR_ICON = "air"

    private val DEBUG by lazy { conf.getBoolean("DEBUG") }

    // 缓存结构：仅存储 ID 和 图标路径
    private val playerCache = ConcurrentHashMap<UUID, Array<SkillCache?>>()

    private data class SkillCache(
        val id: String,
        val icon: String
    )

    // 图标发包的防抖（防止菜单内频繁切换导致发包过频）
    private val debouncedIconUpdate = debounce<Player>(500) { player ->
        performIconUpdate(player)
    }

    /**
     * 刷新缓存：仅在技能变动时调用
     */
    private fun refreshPlayerCache(player: Player) {
        if (DEBUG) println("[Debug] 正在刷新 ${player.name} 的技能图标缓存...")
        val cache = arrayOfNulls<SkillCache>(5)
        for (i in 0..4) {
            val skillId = SkillStorage.getSkillId(player, i)
            if (skillId != null) {
                val iconPath = "SkillTextures/${skillId.lowercase()}.png"
                cache[i] = SkillCache(skillId, iconPath)
            }
        }
        playerCache[player.uniqueId] = cache
    }

    @Awake(LifeCycle.ACTIVE)
    fun startUpdateTask() {
        // 高频任务：只负责发 CD 包
        submit(period = 2L) {
            onlinePlayers().forEach { proxyPlayer ->
                sendCdPacket(proxyPlayer.cast())
            }
        }
    }

    fun sendCdPacket(player: Player) {
        val cacheArray = playerCache[player.uniqueId] ?: return
        val data = HashMap<String, Any>()

        for (i in 1..5) {
            val skillData = cacheArray[i - 1]
            val cdKey = "skill${i}_cd"

            if (skillData != null) {
                val currentLeftCD = RpgRuntime.checkCooldown(player, skillData.id)
                data[cdKey] = if (currentLeftCD > 0) currentLeftCD.round1() else 0.0
            } else {
                data[cdKey] = 0.0
            }
        }
        ArcartXUIRegistry.sendPacket(player, UI_ID, "update_skills_cd", data)
    }

    fun sendSkillIdPacket(player: Player) {
        refreshPlayerCache(player)
        debouncedIconUpdate(player)
    }

    private fun performIconUpdate(player: Player) {
        val cacheArray = playerCache[player.uniqueId] ?: return
        val data = HashMap<String, Any>()

        for (i in 1..5) {
            val skillData = cacheArray[i - 1]
            val iconKey = "skill$i"
            data[iconKey] = skillData?.icon ?: AIR_ICON
        }

        if (DEBUG) println("[Debug] 发送图标更新包: ${player.name} -> $data")
        ArcartXUIRegistry.sendPacket(player, UI_ID, "update_skills_icon", data)
    }

    @SubscribeEvent
    fun onQuit(e: PlayerQuitEvent) {
        playerCache.remove(e.player.uniqueId)
    }
}