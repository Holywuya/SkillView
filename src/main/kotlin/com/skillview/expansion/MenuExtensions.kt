package com.skillview.expansion

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import taboolib.module.nms.getItemTag
import taboolib.module.ui.ClickEvent
import taboolib.platform.util.isAir




/**
 * 扩展方法：锁定槽位 (禁止交互)
 */
fun ClickEvent.lockSlots(slots: List<Int>) {
    if (slots.contains(this.rawSlot)) {
        this.isCancelled = true
    }
}

