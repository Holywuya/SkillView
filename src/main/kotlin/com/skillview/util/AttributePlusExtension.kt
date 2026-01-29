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
 * 为实体添加特定的 AP 属性
 * @param source 来源 ID (同一个 ID 的再次调用会覆盖旧属性)
 */

fun addEntityAttribute(
    entity: LivingEntity,
    source: String,
    attrName: String,
    value: Double,
    time: Double = -1.0,  // -1 表示永久，>0 表示持续时间（秒）
    saveToDatabase: Boolean = false  // 是否保存到数据库（玩家下线后保留）
) {
    val attrData = AttributeAPI.getAttrData(entity)

    val attributes = listOf("$attrName:$value")

    // 使用带 saveToDatabase 参数的重载（如果不需要保存到数据库可省略，默认 false）
    AttributeAPI.addPersistentSourceAttribute(attrData, source, attributes, time, saveToDatabase)
}

/**
 * 移除玩家来自特定源的属性
 */
fun removePlayerAttribute(player: Player, source: String) {
    val attrData = AttributeAPI.getAttrData(player as LivingEntity)
    AttributeAPI.takeSourceAttribute(attrData, source)
}
