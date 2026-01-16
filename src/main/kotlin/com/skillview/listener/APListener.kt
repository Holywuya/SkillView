package com.skillview.listener

import com.skillview.modCore.ModRuntime
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.serverct.ersha.api.AttributeAPI
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit

object APListener {

    // 统一定义 AP 属性来源，方便后续覆盖或删除
    private const val ATTR_SOURCE = "SkillView_MOD"

    /**
     * 玩家进服时，延迟同步 MOD 属性到 AP 系统
     */
    @SubscribeEvent
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player
        // 延迟 100 刻（5秒）确保 ModRuntime 已经完成初始化且 AP 插件已就绪
        submit(delay = 100L) {
            if (player.isOnline) {
                syncModStatsToAP(player)
            }
        }
    }

    /**
     * 将 ModRuntime 里的属性数据同步给 AttributePlus
     */
    fun syncModStatsToAP(player: Player) {
        // 1. 获取玩家当前的所有 MOD 加成数据
        val modStats = ModRuntime.getStats(player)

        // 2. 获取 AP 属性数据对象
        val attrData = AttributeAPI.getAttrData(player as LivingEntity)

        // 3. 构建属性映射 Map (AP 的格式要求: HashMap<属性名, Array<Number>>)
        val attributeMap = HashMap<String, Array<Number>>()

        // --- 基础生存与消耗 ---
        if (modStats.extraMana != 0.0)   attributeMap["魔力上限"] = arrayOf(modStats.extraMana)
        if (modStats.maxHp != 0.0)       attributeMap["生命力"] = arrayOf(modStats.maxHp)
        if (modStats.efficiency != 0.0)  attributeMap["技能效率"] = arrayOf(modStats.efficiency)
        if (modStats.cdReduction != 0.0) attributeMap["冷却缩减"] = arrayOf(modStats.cdReduction)

        // --- 伤害与战斗加成 ---
        if (modStats.damageBonus != 0.0) attributeMap["伤害加成"] = arrayOf(modStats.damageBonus)
        if (modStats.damageMore != 0.0)  attributeMap["最终伤害"] = arrayOf(modStats.damageMore)
        if (modStats.extraRange != 0.0)  attributeMap["额外范围"] = arrayOf(modStats.extraRange)

        // --- 暴击系统 ---
        if (modStats.critRate != 0.0)    attributeMap["暴击几率"] = arrayOf(modStats.critRate)
        if (modStats.critDamage != 0.0)  attributeMap["暴伤倍率"] = arrayOf(modStats.critDamage)

        // 4. 调用 AP 接口注入属性
        // 如果 attributeMap 为空，AP 会自动清理该 Source 的属性
        AttributeAPI.addSourceAttribute(attrData, ATTR_SOURCE, attributeMap, true)

        // 5. DEBUG 提示
        // println("[Debug] 已同步玩家 ${player.name} 的 MOD 属性至 AttributePlus")
    }
}