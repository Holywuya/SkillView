package com.skillview.util

import taboolib.module.ui.ClickEvent


/**
 * 扩展方法：锁定槽位 (禁止交互)
 */
fun ClickEvent.lockSlots(slots: List<Int>) {
    if (slots.contains(this.rawSlot)) {
        this.isCancelled = true
    }
}

