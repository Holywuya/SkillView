package com.servercore.ui.mod

import com.servercore.core.mod.SkillModLogic
import com.servercore.data.RpgDefinitions
import com.servercore.util.getDeepString
import com.servercore.util.hasCustomTag
import com.servercore.util.hasTagValue
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.submit
import taboolib.library.xseries.XMaterial
import taboolib.module.chat.colored
import taboolib.module.ui.conditionSlot
import taboolib.module.ui.openMenu
import taboolib.module.ui.returnItems
import taboolib.module.ui.type.Chest
import taboolib.platform.util.buildItem
import taboolib.platform.util.isAir

object SkillMod {

    private const val SKILL_TAG_KEY = "技能书基础属性.技能id"
    private const val MOD_TYPE_VALUE = "技能Mod"

    fun openSkillMod(player: Player) {
        player.openMenu<Chest>("&8技能MOD配装系统".colored()) {
            rows(4)
            map(
                "#########",
                "###M#M###",
                "###M#M###",
                "####U####"
            )

            val modSlotIds = getSlots('M')
            val bookSlotId = getFirstSlot('U')

            set('#', buildItem(XMaterial.GRAY_STAINED_GLASS_PANE) { name = " " }) { isCancelled = true }

            fun refreshModDisplay(inventory: Inventory) {
                // 1. 先清空 MOD 槽位
                modSlotIds.forEach { inventory.setItem(it, null) }

                // 2. 获取技能书
                val bookItem = inventory.getItem(bookSlotId)

                // 3. 如果有书，读取 NBT 并显示 MOD
                if (bookItem != null && !bookItem.isAir() && bookItem.hasCustomTag(SKILL_TAG_KEY)) {
                    modSlotIds.forEachIndexed { index, slotId ->
                        val modItem = SkillModLogic.getSkillModInSlot(bookItem, index)
                        if (modItem != null && !modItem.isAir()) {
                            inventory.setItem(slotId, modItem)
                        }
                    }
                } else {
                    // 4. 如果没书，显示屏障
                    modSlotIds.forEach {
                        inventory.setItem(it, buildItem(XMaterial.BARRIER) {
                            name = "&c请先放入技能书".colored()
                        })
                    }
                }
            }

            // 2. onBuild 直接调用刷新函数
            onBuild { _, inventory ->
                refreshModDisplay(inventory)
            }

            // 3. 点击交互逻辑
            onClick { event ->
                val rawSlot = event.rawSlot
                val inventory = event.inventory
                val cursor = event.clicker.itemOnCursor

                // 背包点击处理...
                if (rawSlot >= event.inventory.size) {
                    event.isCancelled = event.clickEvent().action == InventoryAction.MOVE_TO_OTHER_INVENTORY
                    return@onClick
                }

                // --- 情况 A: 操作技能书槽位 (U) ---
                if (rawSlot == bookSlotId) {
                    // 1. 放入书
                    if (!cursor.isAir()) {
                        if (!cursor.hasCustomTag(SKILL_TAG_KEY)) {
                            event.isCancelled = true
                            player.sendMessage("&c这不是有效的技能书！".colored())
                            return@onClick
                        }
                        // 允许放入
                        event.isCancelled = false
                        // 放入后刷新界面（显示 MOD）
                        submit(delay = 1) {
                            refreshModDisplay(inventory)
                        }
                    }
                    // 2. 取出书
                    else {
                        val currentBook = inventory.getItem(bookSlotId)
                        // 取出前先保存
                        if (currentBook != null && !currentBook.isAir()) {
                            saveGuiModsToBook(currentBook, inventory, modSlotIds)
                            player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1f)
                        }
                        event.isCancelled = false

                        submit(delay = 1) {
                            refreshModDisplay(inventory)
                        }
                    }
                    return@onClick
                }

                // --- 情况 B: 操作 MOD 槽位 (M) ---
                if (rawSlot in modSlotIds) {
                    val bookItem = inventory.getItem(bookSlotId)

                    if (bookItem == null || bookItem.isAir()) {
                        event.isCancelled = true
                        player.sendMessage("&c请先放入一本技能书！".colored())
                        return@onClick
                    }

                    event.conditionSlot(rawSlot,
                        condition = { put, _ ->
                            if (put == null || put.isAir()) return@conditionSlot true
                            if (!put.hasTagValue(RpgDefinitions.ModNBT.TYPE, MOD_TYPE_VALUE)) {
                                player.sendMessage("&c只能放入类型为 '$MOD_TYPE_VALUE' 的物品！".colored())
                                return@conditionSlot false
                            }

                            val incomingId = put.getDeepString(RpgDefinitions.ModNBT.MOD_ID)
                            val isDuplicate = modSlotIds.any { otherSlot ->
                                if (otherSlot == rawSlot) return@any false
                                val otherItem = inventory.getItem(otherSlot)
                                otherItem != null && !otherItem.isAir() &&
                                        otherItem.getDeepString(RpgDefinitions.ModNBT.MOD_ID) == incomingId
                            }

                            if (isDuplicate) {
                                player.sendMessage("&c你已经装备了一个相同的Mod了！".colored())
                                return@conditionSlot false
                            }
                            true
                        }
                    )
                }
            }

            // 4. 关闭逻辑
            onClose { event ->
                val inventory = event.inventory
                val bookItem = inventory.getItem(bookSlotId)

                // 只有当书还在槽位里时才保存
                if (bookItem != null && !bookItem.isAir()) {
                    saveGuiModsToBook(bookItem, inventory, modSlotIds)
                    event.returnItems(listOf(bookSlotId))
                }

            }
        }
    }

    private fun saveGuiModsToBook(book: ItemStack, inventory: Inventory, modSlots: List<Int>) {
        modSlots.forEachIndexed { index, slotId ->
            val modItem = inventory.getItem(slotId)
            SkillModLogic.removeModFromBook(book, index)
            if (modItem != null && !modItem.isAir() && modItem.hasTagValue(RpgDefinitions.ModNBT.TYPE, MOD_TYPE_VALUE)) {
                SkillModLogic.installModToBook(book, modItem, index)
            }
        }
    }
}