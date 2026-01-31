package com.skillview.ui.mod

import com.skillview.core.mod.PlayerModLogic
import com.skillview.data.RpgDefinitions
import com.skillview.data.SkillStorage
import com.skillview.util.getDeepString
import com.skillview.util.hasTagValue
import org.bukkit.entity.Player
import taboolib.library.xseries.XMaterial
import taboolib.module.chat.colored
import taboolib.module.ui.conditionSlot
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Chest
import taboolib.platform.util.buildItem
import taboolib.platform.util.isAir

object PlayerMod {

    fun openPlayerMod(player: Player) {
        player.openMenu<Chest>("&8角色MOD配装系统".colored()) {
            rows(4)
            map(
                "#########",
                "#M#M#M#M#",
                "#M#M#M#M#",
                "P#######U"
            )

            val modSlots = getSlots('M')
            val filler = buildItem(XMaterial.GRAY_STAINED_GLASS_PANE) { name = " " }

            set('#', filler) { isCancelled = true }
            set('P', buildItem(XMaterial.BARRIER) { name = "&e属性面板".colored() }) { isCancelled = true }
            set('U', buildItem(XMaterial.ANVIL) { name = "&6确认强化".colored() }) { isCancelled = true }

            onClick { event ->
                // 点击玩家背包直接放行
                if (event.rawSlot >= event.inventory.size) {
                    event.isCancelled = false
                    return@onClick
                }

                modSlots.forEach { targetSlot ->
                    event.conditionSlot(
                        targetSlot,
                        condition = { put, _ ->
                            // 1. 如果是取回物品（put为空气），允许操作
                            if (put == null || put.isAir()) return@conditionSlot true

                            // 2. 基础类型校验
                            if (!put.hasTagValue(RpgDefinitions.ModNBT.TYPE, "角色Mod")) {
                                return@conditionSlot false
                            }

                            // 获取正要放入的 MOD ID
                            val incomingId = put.getDeepString(RpgDefinitions.ModNBT.MOD_ID)

                            // 检查其他槽位是否已经有了相同 ID 的 MOD
                            val isDuplicate = modSlots.any { otherSlot ->
                                // 跳过当前正在操作的格子
                                if (otherSlot == targetSlot) return@any false

                                val itemInOtherSlot = event.inventory.getItem(otherSlot)
                                if (itemInOtherSlot == null || itemInOtherSlot.isAir()) return@any false

                                // 比较 ID 是否相同
                                itemInOtherSlot.getDeepString(RpgDefinitions.ModNBT.MOD_ID) == incomingId
                            }

                            if (isDuplicate) {
                                event.clicker.sendMessage("&c你已经装备了一个相同的Mod了！".colored())
                                return@conditionSlot false
                            }

                            true
                        },
                        failedCallback = {
                        }
                    )
                }
            }

            onBuild { _, inventory ->
                val loadout = SkillStorage.getModLoadout(player)

                // 渲染已保存的 MOD
                modSlots.forEachIndexed { index, slot ->
                    val modItem = loadout.mods[index]
                    if (modItem != null && !modItem.isAir()) {
                        inventory.setItem(slot, modItem)
                    }
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