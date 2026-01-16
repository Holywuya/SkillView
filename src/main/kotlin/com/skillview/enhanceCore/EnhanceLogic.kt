package com.skillview.enhanceCore

import com.skillview.expansion.getDeepInt
import com.skillview.expansion.getDeepString
import com.skillview.rpgCore.RpgDefinitions
import org.bukkit.inventory.ItemStack
import kotlin.math.pow

/**
 * 强化/升级消耗计算结果
 */
data class SkillEnhanceCost(
    val endo: Double,      // 所需技能碎片（内融核心）
    val credits: Double,   // 所需货币
    val nextLevel: Int     // 目标等级
)

data class SkillBookCost(
    val starpotions: Int,  // 所需星愿点
    val nextLevel: Int     // 目标等级
)

/**
 * Mod（强化石）强化消耗计算
 * 前期低成本，后期指数爆炸 + 品质倍率影响
 */
fun modCalculation(item: ItemStack): SkillEnhanceCost {
    val currentLevel = item.getDeepInt(RpgDefinitions.ModNBT.LEVEL, 0)

    // 品质倍率表（越高后期越夸张）
    val rarityMultiplier = when (item.getDeepString("品质", "普通")) {
        "普通" -> 0.5
        "精良" -> 1.0
        "史诗" -> 1.5
        "传说" -> 2.0
        "奇迹" -> 3.0
        else   -> 1.0
    }

    // 基础配置
    val BASE_ENDO = 10.0

    // 纯指数成长：每级翻倍
    val rawEndo = BASE_ENDO * 2.0.pow(currentLevel.toDouble())

    // 应用品质倍率并取整（最低 1）
    val finalEndo = (rawEndo * rarityMultiplier).toInt().coerceAtLeast(1).toDouble()

    // 货币 = 碎片 × 5
    val credits = finalEndo * 5.0

    return SkillEnhanceCost(
        endo = finalEndo,
        credits = credits,
        nextLevel = currentLevel + 1
    )
}

/**
 * 技能书强化消耗计算（星愿点）
 */
fun skillencCalculation(item: ItemStack): SkillBookCost {
    val currentLevel = item.getDeepInt(RpgDefinitions.SkillBookNBT.LEVEL, 0)

    // 品质倍率（技能书强化更贵）
    val rarityMultiplier = when (item.getDeepString("品质", "普通")) {
        "普通" -> 1.0
        "精良" -> 2.0
        "史诗" -> 3.0
        "传说" -> 4.0
        "奇迹" -> 5.0
        else   -> 1.0
    }

    val BASE_STAR = 1
    val rawStar = BASE_STAR * 2.0.pow(currentLevel.toDouble())
    val finalStar = (rawStar * rarityMultiplier).toInt().coerceAtLeast(1)

    return SkillBookCost(
        starpotions = finalStar,
        nextLevel = currentLevel + 1
    )
}