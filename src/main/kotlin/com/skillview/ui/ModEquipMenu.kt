package com.skillview.ui

import com.skillview.SkillStorage
import com.skillview.expansion.getModCost
import com.skillview.expansion.hasTagValue
import com.skillview.listener.APListener.syncModStatsToAP
import com.skillview.modCore.ModRuntime
import com.skillview.modCore.ModRuntime.recalculate
import com.skillview.rpgCore.RpgDefinitions
import org.bukkit.entity.Player
import taboolib.library.xseries.XMaterial
import taboolib.module.chat.colored
import taboolib.module.ui.conditionSlot
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Chest
import taboolib.platform.util.buildItem
import taboolib.platform.util.isAir

object ModEquipMenu {

    fun openModEquipMenu(player: Player) {
        player.openMenu<Chest>("&8MOD配装系统".colored()) {
            rows(4)
            map(
                "#########",
                "#M#M#M#M#",
                "#M#M#M#M#",
                "P#######U"
            )

            val modSlot = getSlots('M')
            val filler = buildItem(XMaterial.GRAY_STAINED_GLASS_PANE) { name = " " }
            set('#', filler) { isCancelled = true }
            set('P', buildItem(XMaterial.BARRIER) {
                name = " "
            }) { isCancelled = true }
            set('U', buildItem(XMaterial.BARRIER) {
                name = " "
            }) { isCancelled = true }


            onClick('U') { }
            onClick { event ->
                if (event.rawSlot >= event.inventory.size) {
                    event.isCancelled = false
                    return@onClick
                }

                modSlot.forEach { targetSlot ->
                    event.conditionSlot(
                        targetSlot,
                        condition = { put, out ->
                            if (put != null && !put.isAir()) put.hasTagValue(RpgDefinitions.ModNBT.TYPE, "角色Mod") else true
                        },
                        failedCallback = {
                            event.clicker.sendMessage("&c这里只能放入有效的角色Mod！".colored())
                        }
                    )
                }
            }

            onBuild { _, inventory ->
                // 1. 获取玩家数据对象
                val loadout = SkillStorage.getModLoadout(player)
                val stats = ModRuntime.getStats(player)

                // 2. 计算容量限制 (基础30/扩容60)
                val maxCapacity = if (loadout.isCapacityUpgraded) 60 else 30
                var currentUsed = 0

                // 3. 渲染 0-7 号 MOD 插槽
                modSlot.forEachIndexed { index, slot ->
                    val modItem = loadout.mods[index] // 从 Map 中按索引取

                    if (modItem != null && !modItem.isAir()) {
                        // 累加已用容量
                        currentUsed += modItem.getModCost()
                        inventory.setItem(slot, modItem)
                    }

                }
                onClose { event ->
                    // 创建一个新的配装方案对象
                    val newLoadout = SkillStorage.ModLoadout()
                    modSlot.forEachIndexed { index, i ->
                        val item = event.inventory.getItem(i)

                        if (item != null && !item.isAir() && item.hasTagValue(RpgDefinitions.ModNBT.TYPE, "角色Mod")) {
                            newLoadout.mods[index] = item
                        }
                    }
                    SkillStorage.saveModLoadout(player, newLoadout)
                    recalculate(player)
                    syncModStatsToAP(player)
                    player.sendMessage("&aMod栏已同步到云端数据库！".colored())
                }
            }
        }
    }
}