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

    fun installModToBook(book: ItemStack, modItem: ItemStack, slotIndex: Int): Boolean {
        if (book.isAir() || modItem.isAir()) return false

        val bookTag = book.getItemTag()
        val modJson = GsonUtils.toJson(modItem)
        bookTag.putDeep("$NBT_MOD_ROOT.插槽.$slotIndex", modJson)
        bookTag.saveTo(book)
        return true
    }

    fun removeModFromBook(book: ItemStack, slotIndex: Int) {
        if (book.isAir()) return

        val bookTag = book.getItemTag()
        bookTag.removeDeep("$NBT_MOD_ROOT.插槽.$slotIndex")
        bookTag.saveTo(book)
    }

    fun getSkillModInSlot(book: ItemStack, index: Int): ItemStack? {
        val json = book.getDeepString("$NBT_MOD_ROOT.插槽.$index")
        if (json.isEmpty()) return null
        return GsonUtils.fromJson(json, ItemStack::class.java)
    }

    fun getModInSlot(book: ItemStack, index: Int): ItemStack? {
        if (book.isAir()) return null

        val modJson = book.getDeepString("$NBT_MOD_ROOT.插槽.$index")
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