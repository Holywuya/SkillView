package com.skillview.data

/**
 * NBT 路径常量 - 集中管理所有NBT键名
 * 所有项目中使用的NBT字符串均在此处定义
 * 分层结构：RootPath -> SubPaths，避免重复和散落
 */
object NbtPaths {

    object SkillBook {
        // ========== 基础信息 ==========
        const val TYPE = "类型"
        const val RARITY = "品质"

        // ========== 属性根路径 ==========
        const val ROOT_BASIC = "技能书基础属性"
        const val ROOT_MODIFIER = "技能书属性强化"

        // ========== 基础属性详细路径 ==========
        const val SKILL_ID = "$ROOT_BASIC.技能id"
        const val LEVEL = "$ROOT_BASIC.等级"

        // ========== 属性强化详细路径 ==========
        const val MULTIPLIER = "$ROOT_MODIFIER.技能倍率"
        const val DAMAGE_MORE = "$ROOT_MODIFIER.最终伤害"
        const val DAMAGE_BONUS = "$ROOT_MODIFIER.伤害加成"
        const val EXTRA_RANGE = "$ROOT_MODIFIER.额外范围"
        const val SKILL_POWER = "$ROOT_MODIFIER.技能强度"
        const val COOLDOWN_REDUCTION = "$ROOT_MODIFIER.冷却缩减"
        const val MANA_REDUCTION = "$ROOT_MODIFIER.魔力减耗"

        // ========== MOD系统相关 ==========
        const val MOD_SLOTS = "Mod系统"
        const val MOD_SLOT_FORMAT = "$MOD_SLOTS.插槽"  // 例: "Mod系统.插槽.0"
    }

    object Mod {
        // ========== 基础信息 ==========
        const val TYPE = "类型"
        const val RARITY = "品质"

        // ========== MOD标识信息 ==========
        const val LEVEL = "Mod.等级"
        const val MOD_ID = "Mod.id"

        // ========== MOD属性根路径 ==========
        const val ROOT_MOD = "Mod属性"

        // ========== MOD属性详细路径 ==========
         const val COST = "$ROOT_MOD.消耗"
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
