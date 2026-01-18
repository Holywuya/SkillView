package com.skillview.util
/**
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.serverct.ersha.AttributePlus
import org.serverct.ersha.api.AttributeAPI
import org.serverct.ersha.api.annotations.AutoRegister
import org.serverct.ersha.api.component.SubAttribute
import org.serverct.ersha.attribute.enums.AttributeType
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.warning

// --- 属性名常量定义 ---
const val ATTR_SKILL_COOLDOWN = "冷却缩减"
const val ATTR_SKILL_EFFICIENCY = "技能效率"
const val ATTR_SKILL_MANAINCREASE = "魔力上限"
const val ATTR_SKILL_MANARE = "魔力恢复"
const val ATTR_DAMAGE_BONUS = "伤害加成"
const val ATTR_DAMAGE_MORE = "最终伤害"
const val ATTR_EXTRA_RANGE = "额外范围"
const val ATTR_SKILL_POWER = "技能强度"

// ==========================================
//          自定义属性类注册 (SubAttribute)
// ==========================================

@AutoRegister
class SkillCooldownAttribute : SubAttribute(-1, 1.0, ATTR_SKILL_COOLDOWN, AttributeType.OTHER, "skill_apcd")

@AutoRegister
class SkillEfficiencyAttribute : SubAttribute(-1, 1.0, ATTR_SKILL_EFFICIENCY, AttributeType.OTHER, "skill_apeff")

@AutoRegister
class ManaIncrease : SubAttribute(-1, 1.0, ATTR_SKILL_MANAINCREASE, AttributeType.OTHER, "skill_apmana")

@AutoRegister
class ManaRE : SubAttribute(-1, 1.0, ATTR_SKILL_MANARE, AttributeType.OTHER, "skill_apmanare")

/**
 * 伤害加成 (Damage Bonus)
 */
@AutoRegister
class DamageBonusAttribute : SubAttribute(-1, 0.0, ATTR_DAMAGE_BONUS, AttributeType.OTHER, "skill_apdmg_bonus")

/**
 * 最终伤害 (Damage More)
 */
@AutoRegister
class DamageMoreAttribute : SubAttribute(-1, 0.0, ATTR_DAMAGE_MORE, AttributeType.OTHER, "skill_apdmg_more")

/**
 * 额外范围 (Extra Range)
 */
@AutoRegister
class ExtraRangeAttribute : SubAttribute(-1, 0.0, ATTR_EXTRA_RANGE, AttributeType.OTHER, "skill_aprange")

/**
 * 技能强度 (Skill Power)
 */
@AutoRegister
class SkillPowerAttribute : SubAttribute(-1, 0.0, ATTR_SKILL_POWER, AttributeType.OTHER, "skill_apskill_power")
// ==========================================
//          生命周期管理与 API
// ==========================================

@Awake(LifeCycle.ACTIVE)
fun registerAttributes() {
    val apPlugin = Bukkit.getPluginManager().getPlugin("AttributePlus")
    if (apPlugin == null || !apPlugin.isEnabled) {
        warning("§c未找到 AttributePlus，属性注册跳过。")
        return
    }

    try {
        val manager = AttributePlus.attributeManager

        // 注册属性
        manager.registerAttribute(SkillCooldownAttribute())
        manager.registerAttribute(SkillEfficiencyAttribute())
        manager.registerAttribute(ManaIncrease())
        manager.registerAttribute(ManaRE())
        manager.registerAttribute(DamageBonusAttribute())
        manager.registerAttribute(DamageMoreAttribute())
        manager.registerAttribute(ExtraRangeAttribute())
        manager.registerAttribute(SkillPowerAttribute())

        println("[SkillView] 成功向 AttributePlus 注册了 8 个自定义属性")
    } catch (e: Exception) {
        warning("§c注册属性时发生错误: ${e.message}")
        e.printStackTrace()
    }
}

/**
 * 获取玩家属性值
 * 优化：增加 getOrElse(0) 防止 List 为空时报错
 */
fun getattr(player: Player, attr: String): Double {
    val attrData = AttributeAPI.getAttrData(player as LivingEntity)
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
 **/