package com.skillview.ui

import com.skillview.core.skill.SkillPacketSender
import com.skillview.data.RpgDefinitions
import com.skillview.data.SkillStorage
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

    private val SLOT_MAPPING = mapOf('A' to 11, 'B' to 12, 'C' to 13, 'D' to 14, 'E' to 15)
    private val SKILL_SLOTS = SLOT_MAPPING.values.toList()
    private const val SKILL_TAG = RpgDefinitions.SkillBookNBT.SKILL_ID

    fun openSkillEquipMenu(player: Player) {
        player.openMenu<Chest>("&8技能装备栏".colored()) {
            map("#########", "##ABCDE##", "#########")

            set('#', buildItem(XMaterial.GRAY_STAINED_GLASS_PANE) { name = " " }) { isCancelled = true }

            onClick { event ->
                if (event.rawSlot >= event.inventory.size) {
                    event.isCancelled = false
                    return@onClick
                }

                SKILL_SLOTS.forEach { targetSlot ->
                    event.conditionSlot(targetSlot,
                        condition = { put, _ ->
                            if (put == null || put.isAir()) return@conditionSlot true
                            if (!put.hasCustomTag(SKILL_TAG)) return@conditionSlot false

                            val incomingId = put.getDeepString(SKILL_TAG)
                            val isDuplicate = SKILL_SLOTS.any { otherSlot ->
                                otherSlot != targetSlot &&
                                event.inventory.getItem(otherSlot)?.getDeepString(SKILL_TAG) == incomingId
                            }

                            if (isDuplicate) {
                                event.clicker.sendMessage("&c你已经装备了相同的技能书！".colored())
                                return@conditionSlot false
                            }

                            true
                        },
                        failedCallback = {}
                    )
                }
            }

            onBuild { _, inventory ->
                val loadout = SkillStorage.getSkillLoadout(player)

                SLOT_MAPPING.entries.forEachIndexed { index, entry ->
                    val savedItem = loadout.slots[index]
                    inventory.setItem(entry.value, savedItem ?: ItemStack(Material.AIR))
                }
            }

            onClose { event ->
                val newLoadout = SkillStorage.SkillLoadout()

                SLOT_MAPPING.entries.forEachIndexed { index, entry ->
                    val item = event.inventory.getItem(entry.value)
                    if (item != null && !item.isAir() && item.hasCustomTag(SKILL_TAG)) {
                        newLoadout.slots[index] = item
                    }
                }

                SkillStorage.saveSkillLoadout(player, newLoadout)
                SkillStorage.markSkillDirty(player)
                player.sendMessage("&a技能栏已保存".colored())
                SkillPacketSender.sendSkillIdPacket(player)
            }
        }
    }
}