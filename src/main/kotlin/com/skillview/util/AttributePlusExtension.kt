package com.skillview.util

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.serverct.ersha.api.AttributeAPI

/**
 * 获取实体属性值
 */
fun getattr(player: LivingEntity, attr: String): Double {
    val attrData = AttributeAPI.getAttrData(player)
    return attrData.getAttributeValue(attr).getOrElse(0) { 0.0 }.toDouble()
}

/**
 * 为玩家添加特定的 AP 属性
 * @param source 来源 ID (同一个 ID 的再次调用会覆盖旧属性)
 */
fun addPlayerAttribute(player: Player, source: String, attrName: String, value: Double) {
    val attrData = AttributeAPI.getAttrData(player as LivingEntity)

    val map = HashMap<String, Array<Number>>()
    map[attrName] = arrayOf(value)

    // 调用 API 添加源属性，async = true 异步更新防止卡顿
    AttributeAPI.addSourceAttribute(attrData, source, map, true)
}

/**
 * 移除玩家来自特定源的属性
 */
fun removePlayerAttribute(player: Player, source: String) {
    val attrData = AttributeAPI.getAttrData(player as LivingEntity)
    AttributeAPI.takeSourceAttribute(attrData, source)
}
