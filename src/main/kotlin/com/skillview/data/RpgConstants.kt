package com.skillview.data

object RpgConstants {

    object AttributeLists {

        val SKILL_MOD_ATTRIBUTES = listOf(
            "最终伤害",
            "额外范围",
            "冷却缩减",
            "魔力上限",
            "伤害加成",
            "技能效率",
            "技能倍率",
            "魔力减耗",
            "魔力恢复"
        )

        val PLAYER_MOD_ATTRIBUTES = listOf(
            "伤害加成",
            "最终伤害",
            "暴击几率",
            "暴伤倍率",
            "技能强度",
            "生命力",
            "额外范围",
            "冷却缩减",
            "魔力恢复",
            "魔力上限",
            "技能效率"
        )
    }

    object AttributeNames {
        const val COOLDOWN = "冷却缩减"
        const val EFFICIENCY = "技能效率"
        const val MANA_MAX = "魔力上限"
        const val MANA_REGEN = "魔力恢复"
        const val DAMAGE_BONUS = "伤害加成"
        const val DAMAGE_MORE = "最终伤害"
        const val EXTRA_RANGE = "额外范围"
        const val SKILL_POWER = "技能强度"
        const val CRIT_RATE = "暴击几率"
        const val CRIT_DAMAGE = "暴伤倍率"
        const val MAX_HP = "生命力"
        const val SKILL_MULTIPLIER = "技能倍率"
        const val MANA_REDUCTION = "魔力减耗"
    }

    object GameConfig {
        const val DEFAULT_MANA = 100.0
        const val BASE_MANA_REGEN = 2.0
        const val SKILL_SLOTS_COUNT = 5
        const val MOD_SLOTS_COUNT = 8
        const val DEFAULT_MOD_CAPACITY = 60
        const val DEFAULT_MOD_COST = 0
        const val DEFAULT_RARITY = "普通"
    }

    object StorageKeys {
        const val SKILL_LOADOUT_KEY = "active_skill_loadout"
        const val MOD_LOADOUT_KEY = "player_mod_loadout"
        const val STAR_POINTS_KEY = "star_points"
    }

    object ModTypes {
        const val PLAYER_MOD = "角色Mod"
        const val SKILL_MOD = "技能Mod"
        const val WEAPON_MOD = "武器Mod"
        const val SKILL_BOOK = "技能书"
    }

    object Rarities {
        const val COMMON = "普通"
        const val UNCOMMON = "罕见"
        const val RARE = "稀有"
        const val EPIC = "史诗"
        const val LEGENDARY = "传奇"
    }
}
