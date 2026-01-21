package com.servercore.util

import com.servercore.data.RpgDefinitions
import org.bukkit.inventory.ItemStack

/**
 * 强化/升级消耗计算结果
 */
data class SkillEnhanceCost(
    val endo: Long,      // 所需技能碎片（内融核心）
    val credits: Long,   // 所需货币
    val nextLevel: Int     // 目标等级
)

data class SkillBookCost(
    val starpotions: Int,  // 所需星愿点
    val nextLevel: Int     // 目标等级
)

/**
 * Mod强化消耗计算
 * 前期低成本，后期指数爆炸 + 品质倍率影响
 */
fun modCalculation(item: ItemStack): SkillEnhanceCost {
    // 常量与限制
    val MAX_LEVEL = 60
    val BASE_ENDO = 10L
    val CREDITS_MULTIPLIER = 5L

    val currentLevelRaw = item.getDeepInt(RpgDefinitions.ModNBT.LEVEL, 0)
    val currentLevel = currentLevelRaw.coerceAtLeast(0).coerceAtMost(MAX_LEVEL)

    // 品质倍率表（越高后期越夸张）
    val rarityMultiplier = when (item.getDeepString("品质", "普通")) {
        "普通" -> 0.5
        "精良" -> 1.0
        "史诗" -> 1.5
        "传说" -> 2.0
        "奇迹" -> 3.0
        else   -> 1.0
    }

    // 使用整数位移实现 2^level，避免浮点幂带来的精度问题
    val rawEndo = if (currentLevel >= 0) {
        // 1L shl currentLevel 等于 2^currentLevel（当 currentLevel 合理受限时安全）
        BASE_ENDO * (1L shl currentLevel)
    } else {
        BASE_ENDO
    }

    // 应用品质倍率并取整（最低 1）
    val finalEndo = (rawEndo * rarityMultiplier).toLong().coerceAtLeast(1L)

    // 货币 = 碎片 × multiplier
    val credits = finalEndo * CREDITS_MULTIPLIER

    return SkillEnhanceCost(
        endo = finalEndo,
        credits = credits,
        nextLevel = currentLevel + 1
    )
}

/**
 * 技能书强化消耗计算（星愿点）
 */
fun calculateSkillBookCost(item: ItemStack): SkillBookCost {
    val MAX_LEVEL = 60
    val BASE_STAR = 1

    val currentLevelRaw = item.getDeepInt(RpgDefinitions.SkillBookNBT.LEVEL, 0)
    val currentLevel = currentLevelRaw.coerceAtLeast(0).coerceAtMost(MAX_LEVEL)

    // 品质倍率（技能书强化更贵）
    val rarityMultiplier = when (item.getDeepString("品质", "普通")) {
        "普通" -> 1.0
        "精良" -> 2.0
        "史诗" -> 3.0
        "传说" -> 4.0
        "奇迹" -> 5.0
        else   -> 1.0
    }

    val rawStar = BASE_STAR * (1 shl currentLevel)
    val finalStar = (rawStar * rarityMultiplier).toInt().coerceAtLeast(1)

    return SkillBookCost(
        starpotions = finalStar,
        nextLevel = currentLevel + 1
    )
}

