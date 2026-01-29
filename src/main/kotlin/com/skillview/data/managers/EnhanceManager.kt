package com.skillview.data.managers

import com.skillview.config.RpgConfig
import com.skillview.data.RpgDefinitions
import com.skillview.data.SkillStorage
import com.skillview.util.*

object EnhanceManager {

    fun calculateSkillBookCost(item: org.bukkit.inventory.ItemStack): Int {
        val currentLevel = item.getDeepInt(RpgDefinitions.SkillBookNBT.LEVEL, 0)
        return currentLevel * 50 + 10
    }

    fun calculateModUpgradeCost(item: org.bukkit.inventory.ItemStack): Pair<Int, Int> {
        val currentLevel = item.getDeepInt(RpgDefinitions.ModNBT.LEVEL, 0)
        val baseCost = currentLevel * 100
        return Pair(baseCost + 50, baseCost + 1000)
    }
}
