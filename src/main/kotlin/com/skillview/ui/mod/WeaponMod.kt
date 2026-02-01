package com.skillview.ui.mod

import com.skillview.core.mod.CapacitySystem
import com.skillview.core.mod.PolaritySystem
import com.skillview.core.mod.SlotPolarityManager
import com.skillview.data.RpgDefinitions
import com.skillview.data.SkillStorage
import com.skillview.util.getDeepString
import com.skillview.util.getModCost
import com.skillview.util.getModPolarity
import com.skillview.util.hasCustomTag
import com.skillview.util.hasTagValue
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.expansion.getDataContainer
import taboolib.library.xseries.XMaterial
import taboolib.module.chat.colored
import taboolib.module.ui.conditionSlot
import taboolib.module.ui.openMenu
import taboolib.module.ui.returnItems
import taboolib.module.ui.type.Chest
import taboolib.platform.util.buildItem
import taboolib.platform.util.isAir
import top.maplex.arim.tools.gson.GsonUtils
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object WeaponMod {

    private const val WEAPON_TAG = "武器属性.武器id"
    private const val MOD_TYPE_VALUE = "武器Mod"
    private const val DEFAULT_CAPACITY = 60

    data class WeaponModLoadout(
        val weaponItem: ItemStack? = null,
        val mods: MutableMap<Int, ItemStack> = mutableMapOf(),
        val slotPolarities: MutableMap<Int, String> = mutableMapOf()
    )

    private val loadoutCache = ConcurrentHashMap<UUID, WeaponModLoadout>()

    fun openWeaponMod(player: Player) {
        player.openMenu<Chest>("&8武器MOD配装系统".colored()) {
            rows(6)
            map(
                "#########",
                "##M#W#M##",
                "##M###M##",
                "#########",
                "###C#S###",
                "#P#P#P#P#"
            )

            val modSlotIds = getSlots('M')
            val polarityConfigIds = getSlots('P')
            val weaponSlotId = getFirstSlot('W')
            val capacitySlotId = getFirstSlot('C')
            val saveSlotId = getFirstSlot('S')

            val filler = buildItem(XMaterial.GRAY_STAINED_GLASS_PANE) { name = " " }

            set('#', filler) { isCancelled = true }
            set('S', buildItem(XMaterial.EMERALD) { name = "&a保存配置".colored() }) {
                isCancelled = true
                saveWeaponLoadout(player, inventory, weaponSlotId, modSlotIds)
                player.sendMessage("&a武器MOD配置已保存！".colored())
            }

            fun updateCapacityDisplay(inventory: Inventory) {
                val used = CapacitySystem.calculateUsedCapacity(inventory, modSlotIds)
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

            fun updatePolarityDisplay(inventory: Inventory) {
                val loadout = getWeaponModLoadout(player)
                polarityConfigIds.forEachIndexed { index, slotId ->
                    val polarity = loadout.slotPolarities[index] ?: "无"
                    val polaritySymbol = when (polarity) {
                        "V" -> "&c[V]"
                        "D" -> "&b[D]"
                        "-" -> "&a[-]"
                        "=" -> "&9[=]"
                        "R" -> "&6[R]"
                        "Y" -> "&d[Y]"
                        "*" -> "&f[*]"
                        else -> "&7[无]"
                    }
                    
                    inventory.setItem(slotId, buildItem(XMaterial.AMETHYST_SHARD) {
                        name = "&e极性 #${index}: $polaritySymbol".colored()
                        lore.addAll(listOf(
                            "",
                            "&7点击配置此槽位的极性",
                            "&7当前: $polarity"
                        ).map { it.colored() })
                    })
                }
            }

            fun refreshModDisplay(inventory: Inventory) {
                modSlotIds.forEach { inventory.setItem(it, null) }
                
                val weaponItem = inventory.getItem(weaponSlotId)
                
                if (weaponItem != null && !weaponItem.isAir() && weaponItem.hasCustomTag(WEAPON_TAG)) {
                    val loadout = getWeaponModLoadout(player)
                    modSlotIds.forEachIndexed { index, slotId ->
                        val modItem = loadout.mods[index]
                        if (modItem != null && !modItem.isAir()) {
                            inventory.setItem(slotId, modItem)
                        }
                    }
                } else {
                    modSlotIds.forEach {
                        inventory.setItem(it, buildItem(XMaterial.BARRIER) {
                            name = "&c请先放入武器".colored()
                        })
                    }
                }
                updateCapacityDisplay(inventory)
                updatePolarityDisplay(inventory)
            }

            onBuild { _, inventory ->
                refreshModDisplay(inventory)
            }

            onClick { event ->
                val rawSlot = event.rawSlot
                val inventory = event.inventory
                val cursor = event.clicker.itemOnCursor

                if (rawSlot >= event.inventory.size) {
                    event.isCancelled = false
                    return@onClick
                }

                if (rawSlot in polarityConfigIds) {
                    event.isCancelled = true
                    val slotIndex = polarityConfigIds.indexOf(rawSlot)
                    val loadout = getWeaponModLoadout(player)
                    val currentPolarity = loadout.slotPolarities[slotIndex]
                    
                    PolaritySelectionMenu.openPolaritySelection(
                        player = player,
                        modType = SlotPolarityManager.ModType.WEAPON,
                        slotIndex = slotIndex,
                        currentPolarity = currentPolarity
                    ) { selectedPolarity ->
                        val updatedLoadout = getWeaponModLoadout(player)
                        if (selectedPolarity == "无") {
                            updatedLoadout.slotPolarities.remove(slotIndex)
                        } else {
                            updatedLoadout.slotPolarities[slotIndex] = selectedPolarity
                        }
                        saveWeaponLoadout(player, inventory, weaponSlotId, modSlotIds)
                        player.getDataContainer()["weapon_mod_loadout"] = GsonUtils.toJson(updatedLoadout)
                        loadoutCache[player.uniqueId] = updatedLoadout
                        player.sendMessage("&a极性 #$slotIndex 已设置为: &e$selectedPolarity".colored())
                    }
                    return@onClick
                }

                if (rawSlot == weaponSlotId) {
                    if (!cursor.isAir()) {
                        if (!cursor.hasCustomTag(WEAPON_TAG)) {
                            event.isCancelled = true
                            player.sendMessage("&c这不是有效的武器！".colored())
                            return@onClick
                        }
                        event.isCancelled = false
                        submit(delay = 1) { refreshModDisplay(inventory) }
                    } else {
                        saveWeaponLoadout(player, inventory, weaponSlotId, modSlotIds)
                        event.isCancelled = false
                        submit(delay = 1) { refreshModDisplay(inventory) }
                    }
                    return@onClick
                }

                if (rawSlot in modSlotIds) {
                    val weaponItem = inventory.getItem(weaponSlotId)

                    if (weaponItem == null || weaponItem.isAir()) {
                        event.isCancelled = true
                        player.sendMessage("&c请先放入一把武器！".colored())
                        return@onClick
                    }

                    event.conditionSlot(rawSlot,
                        condition = { put, taken ->
                            if (put == null || put.isAir()) {
                                submit(delay = 1) { updateCapacityDisplay(inventory) }
                                return@conditionSlot true
                            }
                            
                            if (!put.hasTagValue(RpgDefinitions.ModNBT.TYPE, MOD_TYPE_VALUE)) {
                                player.sendMessage("&c只能放入武器Mod！".colored())
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
                                player.sendMessage("&c你已经装备了相同的Mod！".colored())
                                return@conditionSlot false
                            }

                            val currentUsed = CapacitySystem.calculateUsedCapacity(inventory, modSlotIds)
                            val takenCost = if (taken != null && !taken.isAir()) taken.getModCost() else 0
                            val putCost = put.getModCost()
                            val newUsed = currentUsed - takenCost + putCost
                            
                            if (newUsed > DEFAULT_CAPACITY) {
                                player.sendMessage("&c容量不足！需要: &e$newUsed&c/&f$DEFAULT_CAPACITY &7(超出 &c${newUsed - DEFAULT_CAPACITY}&7)".colored())
                                return@conditionSlot false
                            }

                            submit(delay = 1) { updateCapacityDisplay(inventory) }
                            true
                        }
                    )
                }
            }

            onClose { event ->
                val inventory = event.inventory
                val weaponItem = inventory.getItem(weaponSlotId)
                
                if (weaponItem != null && !weaponItem.isAir()) {
                    saveWeaponLoadout(player, inventory, weaponSlotId, modSlotIds)
                    event.returnItems(listOf(weaponSlotId))
                }
            }
        }
    }

    private fun saveWeaponLoadout(
        player: Player,
        inventory: Inventory,
        weaponSlot: Int,
        modSlots: List<Int>
    ) {
        val loadout = WeaponModLoadout()
        
        val weaponItem = inventory.getItem(weaponSlot)
        if (weaponItem != null && !weaponItem.isAir()) {
            loadout.weaponItem?.let { }
        }

        modSlots.forEachIndexed { index, slotId ->
            val item = inventory.getItem(slotId)
            if (item != null && !item.isAir() && item.hasTagValue(RpgDefinitions.ModNBT.TYPE, MOD_TYPE_VALUE)) {
                loadout.mods[index] = item
            }
        }

        loadoutCache[player.uniqueId] = loadout
        player.getDataContainer()["weapon_mod_loadout"] = GsonUtils.toJson(loadout)
    }

    fun getWeaponModLoadout(player: Player): WeaponModLoadout {
        loadoutCache[player.uniqueId]?.let { return it }
        
        val json = player.getDataContainer()["weapon_mod_loadout"] ?: ""
        val loadout = if (json.isEmpty()) {
            WeaponModLoadout()
        } else {
            try {
                GsonUtils.fromJson(json, WeaponModLoadout::class.java)
            } catch (e: Exception) {
                WeaponModLoadout()
            }
        }
        loadoutCache[player.uniqueId] = loadout
        return loadout
    }

    @SubscribeEvent
    fun onQuit(e: PlayerQuitEvent) {
        loadoutCache.remove(e.player.uniqueId)
    }
}
