package com.skillview.data

object NbtPaths {

    object SkillBook {
        const val TYPE = "类型"
        const val RARITY = "品质"
        const val ROOT_BASIC = "技能书基础属性"
        const val ROOT_MODIFIER = "技能书属性强化"
        const val MOD_SLOTS = "Mod系统"

        const val SKILL_ID = "$ROOT_BASIC.技能id"
        const val LEVEL = "$ROOT_BASIC.等级"
        const val MULTIPLIER = "$ROOT_MODIFIER.技能倍率"
        const val DAMAGE_MORE = "$ROOT_MODIFIER.最终伤害"
        const val DAMAGE_BONUS = "$ROOT_MODIFIER.伤害加成"
        const val EXTRA_RANGE = "$ROOT_MODIFIER.额外范围"
        const val SKILL_POWER = "$ROOT_MODIFIER.技能强度"
        const val COOLDOWN_REDUCTION = "$ROOT_MODIFIER.冷却缩减"
        const val MANA_REDUCTION = "$ROOT_MODIFIER.魔力减耗"
    }

    object Mod {
        const val TYPE = "类型"
        const val RARITY = "品质"
        const val LEVEL = "Mod.等级"
        const val MOD_ID = "Mod.id"

        const val ROOT_MOD = "Mod属性"
        const val COST = "$ROOT_MOD.消耗"
        const val POLARITY = "$ROOT_MOD.极性"

        const val DAMAGE_BONUS = "$ROOT_MOD.伤害加成"
        const val DAMAGE_MORE = "$ROOT_MOD.最终伤害"
        const val EXTRA_RANGE = "$ROOT_MOD.额外范围"
        const val SKILL_POWER = "$ROOT_MOD.技能强度"
        const val EFFICIENCY = "$ROOT_MOD.技能效率"
        const val COOLDOWN_REDUCTION = "$ROOT_MOD.冷却缩减"
        const val MANA_REGEN = "$ROOT_MOD.魔力恢复"
        const val MANA_MAX = "$ROOT_MOD.魔力上限"
    }
}
