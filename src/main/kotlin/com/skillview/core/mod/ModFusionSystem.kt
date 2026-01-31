package com.skillview.core.mod

import com.skillview.config.RpgConfig
import com.skillview.data.RpgDefinitions
import com.skillview.data.SkillStorage
import com.skillview.util.getModId
import com.skillview.util.getModLevel
import com.skillview.util.setDeep
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.module.nms.getItemTag

object ModFusionSystem {

    private val LEVEL_COSTS = intArrayOf(
        0, 10, 30, 70, 150, 310, 630, 1270, 2550, 5110, 10230
    )

    private val RARITY_MULTIPLIER = mapOf(
        "普通" to 1.0,
        "精良" to 1.5,
        "稀有" to 2.0,
        "史诗" to 2.5,
        "传奇" to 3.0
    )

    fun getUpgradeCost(currentLevel: Int, targetLevel: Int, rarity: String): Int {
        if (targetLevel <= currentLevel) return 0

        val baseCost = (currentLevel until targetLevel).sumOf { level ->
            LEVEL_COSTS.getOrElse(level + 1) { 10230 }
        }

        val multiplier = RARITY_MULTIPLIER[rarity] ?: 1.0
        return (baseCost * multiplier).toInt()
    }

    fun getNextLevelCost(currentLevel: Int, rarity: String): Int {
        return getUpgradeCost(currentLevel, currentLevel + 1, rarity)
    }

    fun upgradeMod(player: Player, mod: ItemStack, targetLevel: Int): UpgradeResult {
        val currentLevel = mod.getModLevel()
        val modId = mod.getModId()
        val setting = RpgConfig.getMod(modId) ?: return UpgradeResult.MOD_NOT_FOUND

        if (targetLevel > setting.maxLevel) return UpgradeResult.MAX_LEVEL_REACHED

        val rarity = mod.getItemTag().getDeep(RpgDefinitions.ModNBT.RARITY)?.asString() ?: "普通"
        val cost = getUpgradeCost(currentLevel, targetLevel, rarity)

        if (!SkillStorage.takeStarPoints(player, cost)) return UpgradeResult.INSUFFICIENT_POINTS

        mod.setDeep(RpgDefinitions.ModNBT.LEVEL, targetLevel)

        val baseDrain = setting.baseDrain
        val drainStep = setting.drainStep
        mod.setDeep(RpgDefinitions.ModNBT.COST, baseDrain + (targetLevel * drainStep))

        updateModAttributes(mod, setting, targetLevel)

        return UpgradeResult.SUCCESS
    }

    private fun updateModAttributes(mod: ItemStack, setting: com.skillview.data.ModSetting, level: Int) {
        val tag = mod.getItemTag()
        setting.attributes.forEach { (attr, baseValue) ->
            val scaledValue = baseValue * (level + 1)
            tag.putDeep("${RpgDefinitions.ModNBT.ROOT_MOD}.$attr", scaledValue)
        }
        tag.saveTo(mod)
    }

    enum class UpgradeResult {
        SUCCESS,
        INSUFFICIENT_POINTS,
        MAX_LEVEL_REACHED,
        MOD_NOT_FOUND
    }
}
