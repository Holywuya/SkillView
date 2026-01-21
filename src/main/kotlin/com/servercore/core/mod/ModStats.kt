package com.servercore.core.mod

import java.util.concurrent.ConcurrentHashMap

object ModStats {
    /**
     * MOD 全局属性统计类
     */
    class ModStats {
        private val values = ConcurrentHashMap<String, Double>()

        fun add(attribute: String, value: Double) {
            val current = values.getOrDefault(attribute, 0.0)
            values[attribute] = current + value
        }

        // --- 业务层快捷访问器 ---
        val extraMana: Double   get() = values.getOrDefault("魔力上限", 0.0)
        val efficiency: Double  get() = values.getOrDefault("技能效率", 0.0)
        val cdReduction: Double get() = values.getOrDefault("冷却缩减", 0.0)
        val maxHp: Double       get() = values.getOrDefault("最大生命", 0.0)
        val damageBonus: Double get() = values.getOrDefault("伤害加成", 0.0)
        val damageMore: Double  get() = values.getOrDefault("最终伤害", 0.0)
        val extraRange: Double  get() = values.getOrDefault("额外范围", 0.0)
        val critRate: Double    get() = values.getOrDefault("暴击几率", 0.0)
        val critDamage: Double  get() = values.getOrDefault("暴击伤害", 0.0)
        val skillPower: Double   get() = values.getOrDefault("技能强度", 0.0)
        val manaRegen: Double    get() = values.getOrDefault("魔力恢复", 0.0)
    }
}