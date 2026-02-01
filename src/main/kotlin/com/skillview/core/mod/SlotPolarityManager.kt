package com.skillview.core.mod

import org.bukkit.entity.Player
import taboolib.expansion.getDataContainer

object SlotPolarityManager {

    fun getSlotPolarities(player: Player, type: ModType): Map<Int, String> {
        val key = when (type) {
            ModType.WEAPON -> "weapon_slot_polarities"
            ModType.PLAYER -> "player_slot_polarities"
            ModType.SKILL -> "skill_slot_polarities"
        }
        
        val json = player.getDataContainer()[key] as? String ?: return emptyMap()
        return try {
            json.split(";")
                .filter { it.isNotEmpty() }
                .associate { pair ->
                    val (index, polarity) = pair.split(":")
                    index.toInt() to polarity
                }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun setSlotPolarity(player: Player, type: ModType, slotIndex: Int, polarity: String) {
        val key = when (type) {
            ModType.WEAPON -> "weapon_slot_polarities"
            ModType.PLAYER -> "player_slot_polarities"
            ModType.SKILL -> "skill_slot_polarities"
        }
        
        val current = getSlotPolarities(player, type).toMutableMap()
        
        if (polarity == "无") {
            current.remove(slotIndex)
        } else {
            current[slotIndex] = polarity
        }
        
        val json = current.entries.joinToString(";") { "${it.key}:${it.value}" }
        player.getDataContainer()[key] = json
    }

    fun clearSlotPolarity(player: Player, type: ModType, slotIndex: Int) {
        setSlotPolarity(player, type, slotIndex, "无")
    }

    fun clearAllPolarities(player: Player, type: ModType) {
        val key = when (type) {
            ModType.WEAPON -> "weapon_slot_polarities"
            ModType.PLAYER -> "player_slot_polarities"
            ModType.SKILL -> "skill_slot_polarities"
        }
        player.getDataContainer()[key] = ""
    }

    enum class ModType {
        WEAPON, PLAYER, SKILL
    }
}
