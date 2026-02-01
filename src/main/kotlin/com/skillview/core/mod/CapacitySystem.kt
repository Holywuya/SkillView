package com.skillview.core.mod

import com.skillview.util.getModCost
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.platform.util.isAir

object CapacitySystem {

     fun getBaseCapacity(level: Int): Int = (level * 2).coerceIn(0, 60)

     fun getMaxCapacity(level: Int, hasOrokinBoost: Boolean): Int {
         val base = getBaseCapacity(level)
         return if (hasOrokinBoost) base * 2 else base
     }

     fun calculateUsedCapacity(
         inventory: Inventory,
         modSlots: List<Int>
     ): Int {
         var total = 0
         modSlots.forEachIndexed { index, slot ->
             val item = inventory.getItem(slot)
             if (item != null && !item.isAir()) {
                 total += item.getModCost()
             }
         }
         return total
     }

     fun calculateUsedCapacity(mods: Map<Int, ItemStack>): Int {
         var total = 0
         mods.forEach { (index, item) ->
             if (!item.isAir()) {
                 total += item.getModCost()
             }
         }
         return total
     }

     fun validateLoadout(
         mods: Map<Int, ItemStack>,
         maxCapacity: Int
     ): CapacityResult {
         val used = calculateUsedCapacity(mods)
         return CapacityResult(used, maxCapacity, used <= maxCapacity)
     }

     data class CapacityResult(
         val used: Int,
         val max: Int,
         val isValid: Boolean
     ) {
         val remaining: Int get() = max - used
         val overCapacity: Int get() = if (isValid) 0 else used - max
     }
}
