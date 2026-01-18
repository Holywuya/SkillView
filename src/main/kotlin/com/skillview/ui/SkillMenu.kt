package com.skillview.ui

import com.skillview.data.RpgDefinitions
import com.skillview.data.SkillStorage
import com.skillview.util.SkillPacketSender
import com.skillview.util.getDeepString
import com.skillview.util.hasCustomTag
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.library.xseries.XMaterial
import taboolib.module.chat.colored
import taboolib.module.ui.conditionSlot
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Chest
import taboolib.platform.util.buildItem
import taboolib.platform.util.isAir

object SkillMenu {

    private val CHAR_TO_SLOT = mapOf('A' to 11, 'B' to 12, 'C' to 13, 'D' to 14, 'E' to 15)
    private const val SKILL_TAG = RpgDefinitions.SkillBookNBT.SKILL_ID

    fun openSkillEquipMenu(player: Player) {
        player.openMenu<Chest>("&8技能装备栏".colored()) {
            map(
                "#########",
                "##ABCDE##",
                "#########"
            )

            set('#', buildItem(XMaterial.GRAY_STAINED_GLASS_PANE) { name = " " }) { isCancelled = true }

            onClick { event ->
                if (event.rawSlot >= event.inventory.size) {
                    event.isCancelled = false
                    return@onClick
                }

                CHAR_TO_SLOT.values.forEach { targetSlot ->
                    event.conditionSlot(targetSlot,
                        condition = { put, _ ->
                            // 1. 取回物品动作允许
                            if (put == null || put.isAir()) return@conditionSlot true

                            // 2. 基础标签校验
                            if (!put.hasCustomTag(SKILL_TAG)) return@conditionSlot false

                            // 3. 查重逻辑
                            val incomingId = put.getDeepString(SKILL_TAG)

                            // 检查 A-E 其他槽位是否已经存在相同 ID 的技能
                            val isDuplicate = CHAR_TO_SLOT.values.any { otherSlot ->
                                // 跳过当前正在点击的格子
                                if (otherSlot == targetSlot) return@any false

                                val itemInOtherSlot = event.inventory.getItem(otherSlot)
                                if (itemInOtherSlot == null || itemInOtherSlot.isAir()) return@any false

                                // 对比技能 ID
                                itemInOtherSlot.getDeepString(SKILL_TAG) == incomingId
                            }

                            if (isDuplicate) {
                                event.clicker.sendMessage("&c你已经装备了相同的技能书！".colored())
                                return@conditionSlot false
                            }

                            true
                        },
                        failedCallback = {
                            event.clicker.sendMessage("&c只能放入技能书！且不可重复！".colored())
                        }
                    )
                }
            }

            onBuild { _, inventory ->
                val loadout = SkillStorage.getSkillLoadout(player)

                CHAR_TO_SLOT.entries.forEachIndexed { index, entry ->
                    val savedItem = loadout.slots[index]
                    if (savedItem != null && !savedItem.isAir()) {
                        inventory.setItem(entry.value, savedItem)
                    } else {
                        inventory.setItem(entry.value, ItemStack(Material.AIR))
                    }
                }
            }

            onClose { event ->
                val newLoadout = SkillStorage.SkillLoadout()

                CHAR_TO_SLOT.entries.forEachIndexed { index, entry ->
                    val item = event.inventory.getItem(entry.value)
                    if (item != null && !item.isAir() && item.hasCustomTag(SKILL_TAG)) {
                        newLoadout.slots[index] = item
                    }
                }

                SkillStorage.saveSkillLoadout(player, newLoadout)
                player.sendMessage("&a技能栏已保存".colored())
                SkillPacketSender.sendSkillIdPacket(player)
            }
        }
    }
}