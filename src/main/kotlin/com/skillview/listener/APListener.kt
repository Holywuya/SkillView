package com.skillview.listener

import com.skillview.core.mod.PlayerModLogic
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.serverct.ersha.api.AttributeAPI
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.submit
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration


object APListener {

    private const val ATTR_SOURCE = "PlayerModStats"

    @Config("config.yml")
    lateinit var conf: Configuration

    private val reusableAttrList = ThreadLocal.withInitial { ArrayList<String>(10) }

    @Awake(LifeCycle.ACTIVE)
    fun run() {
        submit(period = 40, async = true, delay = 20) {
            Bukkit.getOnlinePlayers().forEach { player ->
                if (PlayerModLogic.recalculateIfNeeded(player)) {
                    syncModStatsToAP(player)
                }
            }
        }
    }

    fun syncModStatsToAP(player: Player) {
        val modStats = PlayerModLogic.getStats(player)
        val attrData = AttributeAPI.getAttrData(player)

        val attributeList = reusableAttrList.get()
        attributeList.clear()

        if (modStats.maxHp != 0.0) attributeList.add("生命力:${modStats.maxHp}")
        if (modStats.critRate != 0.0) attributeList.add("暴击几率:${modStats.critRate}")
        if (modStats.critDamage != 0.0) attributeList.add("暴伤倍率:${modStats.critDamage}")

        AttributeAPI.addPersistentSourceAttribute(
            attrData,
            ATTR_SOURCE,
            attributeList,
            -1.0
        )
    }

    fun forceSyncPlayer(player: Player) {
        PlayerModLogic.markDirty(player)
        submit(async = true) {
            PlayerModLogic.recalculateIfNeeded(player)
            syncModStatsToAP(player)
        }
    }
}
