package com.skillview.core.mod

import com.skillview.data.RpgDefinitions
import com.skillview.util.getDeepDouble
import com.skillview.util.getDeepString
import org.bukkit.inventory.ItemStack
import taboolib.module.nms.getItemTag
import taboolib.platform.util.isAir
import top.maplex.arim.tools.gson.GsonUtils

object SkillModLogic {

    private const val NBT_MOD_ROOT = RpgDefinitions.SkillBookNBT.MOD_SLOTS
    private const val MOD_SLOT_FORMAT = RpgDefinitions.SkillBookNBT.MOD_SLOT_FORMAT

    fun installModToBook(book: ItemStack, modItem: ItemStack, slotIndex: Int): Boolean {
        if (book.isAir() || modItem.isAir()) return false

        val bookTag = book.getItemTag()
        val modJson = GsonUtils.toJson(modItem)
        bookTag.putDeep("$MOD_SLOT_FORMAT.$slotIndex", modJson)
        bookTag.saveTo(book)
        return true
    }

    fun removeModFromBook(book: ItemStack, slotIndex: Int) {
        if (book.isAir()) return

        val bookTag = book.getItemTag()
        bookTag.removeDeep("$MOD_SLOT_FORMAT.$slotIndex")
        bookTag.saveTo(book)
    }

    fun getSkillModInSlot(book: ItemStack, index: Int): ItemStack? {
        val json = book.getDeepString("$MOD_SLOT_FORMAT.$index")
        if (json.isEmpty()) return null
        return GsonUtils.fromJson(json, ItemStack::class.java)
    }

    fun getModInSlot(book: ItemStack, index: Int): ItemStack? {
        if (book.isAir()) return null

        val modJson = book.getDeepString("$MOD_SLOT_FORMAT.$index")
        if (modJson.isEmpty() || modJson == "null") return null

        return try {
            GsonUtils.fromJson(modJson, ItemStack::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun getSkillBookModStats(book: ItemStack?): ModStats.ModStats {
        val totalStats = ModStats.ModStats()
        if (book == null || book.isAir()) return totalStats

        for (i in 0..3) {
            val modItem = getModInSlot(book, i) ?: continue
            if (modItem.isAir()) continue

            RpgDefinitions.SkillMod_ATTRIBUTES.forEach { attrName ->
                val value = modItem.getDeepDouble("${RpgDefinitions.ModNBT.ROOT_MOD}.$attrName", 0.0)
                if (value != 0.0) {
                    totalStats.add(attrName, value)
                }
            }
        }
        return totalStats
    }
}