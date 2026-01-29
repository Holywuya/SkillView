package com.skillview.core.mod

import com.skillview.core.mod.ModStats.ModStats
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
        playerModStats[player.uniqueId] = ModStats()

        submit(delay = 100L) {
            if (player.isOnline) {
                recalculate(player)
            }
        }
    }

    @SubscribeEvent
    fun onQuit(e: PlayerQuitEvent) {
        playerModStats.remove(e.player.uniqueId)
    }

    fun recalculate(player: Player) {
        val loadout = SkillStorage.getModLoadout(player)
        val stats = ModStats()

        loadout.mods.values.forEach { item ->
            if (item.isAir()) return@forEach

            RpgDefinitions.PlayerMod_ATTRIBUTES.forEach { attrName ->
                val value = item.getDeepDouble("$MOD_NBT_ROOT.$attrName", 0.0)
                if (value != 0.0) {
                    stats.add(attrName, value)
                }
            }
        }
        playerModStats[player.uniqueId] = stats
    }

    fun getStats(player: Player): ModStats =
        playerModStats[player.uniqueId] ?: ModStats()

    fun removeCache(uuid: UUID) {
        playerModStats.remove(uuid)
    }
}