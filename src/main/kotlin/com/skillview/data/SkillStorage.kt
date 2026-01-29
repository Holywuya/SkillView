package com.skillview.data

import com.skillview.util.getDeepString
import com.skillview.util.hasCustomTag
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.expansion.getDataContainer
import taboolib.platform.util.isAir
import top.maplex.arim.tools.gson.GsonUtils

object SkillStorage {

    data class ModLoadout(
        var isCapacityUpgraded: Boolean = false,
        val mods: MutableMap<Int, ItemStack> = mutableMapOf()
    )

    data class SkillLoadout(
        val slots: MutableMap<Int, ItemStack> = mutableMapOf()
    )

    private inline fun <reified T> parseJson(json: String, default: T): T {
        if (json.isEmpty()) return default
        return try {
            GsonUtils.fromJson(json, T::class.java)
        } catch (e: Exception) {
            default
        }
    }

    fun getSkillLoadout(player: Player): SkillLoadout {
        val json = player.getDataContainer()["active_skill_loadout"] ?: ""
        return parseJson(json, SkillLoadout())
    }

    fun saveSkillLoadout(player: Player, loadout: SkillLoadout) {
        player.getDataContainer()["active_skill_loadout"] = GsonUtils.toJson(loadout)
    }

    fun updateSkillSlot(player: Player, slotIndex: Int, item: ItemStack?) {
        val loadout = getSkillLoadout(player)
        if (item == null || item.isAir() || !item.hasCustomTag("技能书基础属性.技能id")) {
            loadout.slots.remove(slotIndex)
        } else {
            loadout.slots[slotIndex] = item
        }
        saveSkillLoadout(player, loadout)
    }

    fun getSkillItem(player: Player, slotIndex: Int): ItemStack? =
        getSkillLoadout(player).slots[slotIndex]

    fun getSkillId(player: Player, slotIndex: Int): String? {
        val item = getSkillItem(player, slotIndex) ?: return null
        val id = item.getDeepString("技能书基础属性.技能id")
        return if (id.isEmpty() || id == "none") null else id
    }

    fun getModLoadout(player: Player): ModLoadout {
        val json = player.getDataContainer()["player_mod_loadout"] ?: ""
        return parseJson(json, ModLoadout())
    }

    fun saveModLoadout(player: Player, loadout: ModLoadout) {
        player.getDataContainer()["player_mod_loadout"] = GsonUtils.toJson(loadout)
    }

    fun installMod(player: Player, slotIndex: Int, item: ItemStack) {
        val loadout = getModLoadout(player)
        loadout.mods[slotIndex] = item
        saveModLoadout(player, loadout)
    }

    fun uninstallMod(player: Player, slotIndex: Int): ItemStack? {
        val loadout = getModLoadout(player)
        val removed = loadout.mods.remove(slotIndex)
        if (removed != null) saveModLoadout(player, loadout)
        return removed
    }

    fun getStarPoints(player: Player): Int {
        val container = player.getDataContainer()
        val rawValue = container["star_points"]
        if (rawValue == null) {
            container["star_points"] = "0"
            return 0
        }
        return rawValue.toIntOrNull() ?: 0
    }

    fun setStarPoints(player: Player, amount: Int) {
        player.getDataContainer()["star_points"] = amount.coerceAtLeast(0).toString()
    }

    fun addStarPoints(player: Player, amount: Int) {
        setStarPoints(player, getStarPoints(player) + amount)
    }

    fun takeStarPoints(player: Player, amount: Int): Boolean {
        val current = getStarPoints(player)
        if (current < amount) return false
        setStarPoints(player, current - amount)
        return true
    }
}