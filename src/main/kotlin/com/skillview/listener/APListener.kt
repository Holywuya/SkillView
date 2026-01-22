package com.skillview.listener

import com.skillview.core.mod.PlayerModLogic
import com.skillview.core.mod.PlayerModLogic.recalculate
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.serverct.ersha.api.AttributeAPI
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Schedule
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration


object APListener {

    // 统一定义 AP 属性来源
    private const val ATTR_SOURCE = "PlayerModStats"

    @Config("config.yml")
    lateinit var conf: Configuration

        @Awake(LifeCycle.ACTIVE)
        fun run(){
            submit(period = 10, async = true, delay = 20) {
                for (player in org.bukkit.Bukkit.getOnlinePlayers()) {
                    syncModStatsToAP(player)
                }
            }
        }

        fun syncModStatsToAP(player: Player) {
            recalculate(player)
            // 1. 获取 MOD 数据
            val modStats = PlayerModLogic.getStats(player)

            // 2. 获取 AP 数据对象
            val attrData = AttributeAPI.getAttrData(player)

            // 3. 构建 List<String>
            val attributeList = ArrayList<String>()

            // --- 基础生存与消耗 ---
            if (modStats.maxHp != 0.0) attributeList.add("生命力:${modStats.maxHp}")
            // --- 暴击系统 ---
            if (modStats.critRate != 0.0) attributeList.add("暴击几率:${modStats.critRate}")
            if (modStats.critDamage != 0.0) attributeList.add("暴伤倍率:${modStats.critDamage}")

            // 4. 调用 AP 接口添加持久化源属性
            AttributeAPI.addPersistentSourceAttribute(
                attrData,
                ATTR_SOURCE,
                attributeList,
                -1.0
            )

        }
    }

