package com.skillview.data

data class SkillSetting(
    val mmSkill: String,
    val cooldown: Double,
    val mana: Int,
    val baseMultiplier: Double,
    val maxLevel: Int,
    val reduceMana: Double,
    val reduceCd: Double,
    val multiplierUp: Double,
    val enhanceMultiplier: Double,
    val tags: List<String>
)

data class RpgClass(
    val baseHealth: Double,
    val baseMana: Double,
    val manaRegen: Double
)

data class ModSetting(
    val rarity: String,
    val polarity: String,
    val baseDrain: Int,
    val drainStep: Int,
    val maxLevel: Int,
    val attributes: Map<String, Double>,
    val tags: List<String>
)
