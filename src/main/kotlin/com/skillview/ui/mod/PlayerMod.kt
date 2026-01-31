package com.skillview.ui.mod

import com.skillview.core.mod.CapacitySystem
import com.skillview.core.mod.PlayerModLogic
import com.skillview.data.RpgDefinitions
import com.skillview.data.SkillStorage
import com.skillview.util.getDeepString
import com.skillview.util.getModCost
import com.skillview.util.hasTagValue
import org.bukkit.entity.Player
import taboolib.common.platform.function.submit
import taboolib.library.xseries.XMaterial
import taboolib.module.chat.colored
import taboolib.module.ui.conditionSlot
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Chest
import taboolib.platform.util.buildItem
import taboolib.platform.util.isAir

object PlayerMod {

    private const val DEFAULT_CAPACITY = 60

    fun openPlayerMod(player: Player) {
        player.openMenu<Chest>("&8角色MOD配装系统".colored()) {
            rows(4)
            map(
                "#########",
                "#M#M#M#M#",
                "#M#M#M#M#",
                "P###C###U"
            )

            val modSlots = getSlots('M')
            val capacitySlotId = getFirstSlot('C')
            val filler = buildItem(XMaterial.GRAY_STAINED_GLASS_PANE) { name = " " }

            set('#', filler) { isCancelled = true }
            set('P', buildItem(XMaterial.BARRIER) { name = "&e属性面板".colored() }) { isCancelled = true }
            set('U', buildItem(XMaterial.ANVIL) { name = "&6确认强化".colored() }) { isCancelled = true }

            fun updateCapacityDisplay(inventory: org.bukkit.inventory.Inventory) {
                val used = CapacitySystem.calculateUsedCapacity(inventory, modSlots)
                val max = DEFAULT_CAPACITY
                val color = if (used <= max) "&a" else "&c"
                
                inventory.setItem(capacitySlotId, buildItem(XMaterial.EXPERIENCE_BOTTLE) {
                    name = "&e容量: $color$used&7/&f$max".colored()
                    lore.addAll(listOf(
                        "",
                        if (used <= max) "&a✔ 容量正常" else "&c✖ 超出容量！",
                        "&7极性匹配可减少消耗"
                    ).map { it.colored() })
                })
            }

            onBuild { _, inventory ->
                val loadout = SkillStorage.getModLoadout(player)

                modSlots.forEachIndexed { index, slot ->
                    val modItem = loadout.mods[index]
                    if (modItem != null && !modItem.isAir()) {
                        inventory.setItem(slot, modItem)
                    }
                }
                
                updateCapacityDisplay(inventory)
            }

            onClick { event ->
                if (event.rawSlot >= event.inventory.size) {
                    event.isCancelled = false
                    return@onClick
                }

                modSlots.forEach { targetSlot ->
                    event.conditionSlot(
                        targetSlot,
                        condition = { put, taken ->
                            if (put == null || put.isAir()) {
                                submit(delay = 1) { updateCapacityDisplay(event.inventory) }
                                return@conditionSlot true
                            }

                            if (!put.hasTagValue(RpgDefinitions.ModNBT.TYPE, "角色Mod")) {
                                event.clicker.sendMessage("&c只能放入角色Mod！".colored())
                                return@conditionSlot false
                            }

                            val incomingId = put.getDeepString(RpgDefinitions.ModNBT.MOD_ID)

                            val isDuplicate = modSlots.any { otherSlot ->
                                if (otherSlot == targetSlot) return@any false

                                val itemInOtherSlot = event.inventory.getItem(otherSlot)
                                if (itemInOtherSlot == null || itemInOtherSlot.isAir()) return@any false

                                itemInOtherSlot.getDeepString(RpgDefinitions.ModNBT.MOD_ID) == incomingId
                            }

                            if (isDuplicate) {
                                event.clicker.sendMessage("&c你已经装备了一个相同的Mod了！".colored())
                                return@conditionSlot false
                            }

                            val currentUsed = CapacitySystem.calculateUsedCapacity(event.inventory, modSlots)
                            val takenCost = if (taken != null && !taken.isAir()) taken.getModCost() else 0
                            val putCost = put.getModCost()
                            val newUsed = currentUsed - takenCost + putCost
                            
                            if (newUsed > DEFAULT_CAPACITY) {
                                event.clicker.sendMessage("&c容量不足！需要: &e$newUsed&c/&f$DEFAULT_CAPACITY &7(超出 &c${newUsed - DEFAULT_CAPACITY}&7)".colored())
                                return@conditionSlot false
                            }

                            submit(delay = 1) { updateCapacityDisplay(event.inventory) }
                            true
                        },
                        failedCallback = {
                        }
                    )
                }
            }

            onClose { event ->
                val newLoadout = SkillStorage.ModLoadout()
                val inventory = event.inventory

                modSlots.forEachIndexed { index, slotId ->
                    val item = inventory.getItem(slotId)
                    if (item != null && !item.isAir() && item.hasTagValue(RpgDefinitions.ModNBT.TYPE, "角色Mod")) {
                        newLoadout.mods[index] = item
                    }
                }

                SkillStorage.saveModLoadout(player, newLoadout)
                SkillStorage.markModDirty(player)
                PlayerModLogic.markDirty(player)
                player.sendMessage("&aMod已保存".colored())
            }
        }
    }
}