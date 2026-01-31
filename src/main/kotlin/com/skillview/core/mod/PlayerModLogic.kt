package com.skillview.core.mod

import com.skillview.core.mod.ModStats.ModStats
import com.skillview.data.RpgDefinitions
import com.skillview.data.SkillStorage
import com.skillview.util.getCachedTag
import com.skillview.util.getDeepDouble
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.module.nms.ItemTag
import taboolib.platform.util.isAir
import java.util.*
import java.util.concurrent.ConcurrentHashMap


object PlayerModLogic {

    private val playerModStats = ConcurrentHashMap<UUID, ModStats>()
    private val dirtyFlags = ConcurrentHashMap.newKeySet<UUID>()
    private const val MOD_NBT_ROOT = RpgDefinitions.ModNBT.ROOT_MOD

    @SubscribeEvent
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player
        playerModStats[player.uniqueId] = ModStats()
        markDirty(player)

        submit(delay = 100L) {
            if (player.isOnline) {
                recalculateIfNeeded(player)
            }
        }
    }

    @SubscribeEvent
    fun onQuit(e: PlayerQuitEvent) {
        val uuid = e.player.uniqueId
        playerModStats.remove(uuid)
        dirtyFlags.remove(uuid)
    }

    fun markDirty(player: Player) {
        dirtyFlags.add(player.uniqueId)
    }

    fun recalculateIfNeeded(player: Player): Boolean {
        val uuid = player.uniqueId
        if (!dirtyFlags.contains(uuid)) return false

        recalculateInternal(player)
        dirtyFlags.remove(uuid)
        return true
    }

    private fun recalculateInternal(player: Player) {
        val loadout = SkillStorage.getModLoadout(player)
        val stats = playerModStats.computeIfAbsent(player.uniqueId) { ModStats() }
        stats.reset()

        loadout.mods.values.forEach { item ->
            if (item.isAir()) return@forEach

            val tag = item.getCachedTag()
            RpgDefinitions.PlayerMod_ATTRIBUTES.forEach { attrName ->
                val value = tag.getDeepDouble("$MOD_NBT_ROOT.$attrName", 0.0)
                if (value != 0.0) {
                    stats.add(attrName, value)
                }
            }
        }
    }

    fun getStats(player: Player): ModStats {
        recalculateIfNeeded(player)
        return playerModStats[player.uniqueId] ?: ModStats()
    }

    fun removeCache(uuid: UUID) {
        playerModStats.remove(uuid)
        dirtyFlags.remove(uuid)
    }
}
