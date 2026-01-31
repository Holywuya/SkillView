package com.skillview.core.mod

object PolaritySystem {

    enum class Polarity(val symbol: String, val color: String) {
        MADURAI("V", "&c"),
        VAZARIN("D", "&b"),
        NARAMON("-", "&a"),
        ZENURIK("=", "&9"),
        UNAIRU("R", "&6"),
        PENJAGA("Y", "&d"),
        UNIVERSAL("*", "&f"),
        NONE("无", "&7")
    }

    fun calculateModCost(modPolarity: String, slotPolarity: String, baseCost: Int): Int {
        return when {
            slotPolarity == "无" || slotPolarity.isEmpty() -> baseCost
            modPolarity == slotPolarity -> baseCost / 2
            modPolarity == "*" -> baseCost / 2
            else -> (baseCost * 1.25).toInt()
        }
    }

    fun getPolarityColor(polarity: String): String {
        return Polarity.entries.find { it.symbol == polarity }?.color ?: "&7"
    }

    fun getPolarityDisplay(polarity: String): String {
        val color = getPolarityColor(polarity)
        return "$color[$polarity]"
    }
}
