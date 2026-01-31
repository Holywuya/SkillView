package com.skillview.core.mod

import java.util.concurrent.ConcurrentHashMap

object ModStats {
    
    class ModStats {
        private val values = ConcurrentHashMap<String, Double>()

        fun reset() {
            values.clear()
        }

        fun add(attribute: String, value: Double) {
            values.merge(attribute, value, Double::plus)
        }

        fun get(attribute: String): Double = values.getOrDefault(attribute, 0.0)

        val extraMana: Double get() = get("魔力上限")
        val efficiency: Double get() = get("技能效率")
        val cdReduction: Double get() = get("冷却缩减")
        val maxHp: Double get() = get("最大生命")
        val damageBonus: Double get() = get("伤害加成")
        val damageMore: Double get() = get("最终伤害")
        val extraRange: Double get() = get("额外范围")
        val critRate: Double get() = get("暴击几率")
        val critDamage: Double get() = get("暴击伤害")
        val skillPower: Double get() = get("技能强度")
        val manaRegen: Double get() = get("魔力恢复")
    }
}
