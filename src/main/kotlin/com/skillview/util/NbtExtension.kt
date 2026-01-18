package com.skillview.util

import org.bukkit.inventory.ItemStack
import taboolib.module.nms.ItemTag
import taboolib.module.nms.ItemTagData
import taboolib.module.nms.getItemTag
import taboolib.platform.util.isAir

// ==========================================
//          NBT 工具扩展
// ==========================================

private fun getItemNBT(item: ItemStack, path: String): ItemTagData? {
    if (item.isAir()) return null
    return item.getItemTag().getDeep(path)
}

// --- 读取扩展 ---
fun ItemStack.getDeepDouble(path: String, default: Double = 0.0) =
    getItemNBT(this, path)?.asDouble() ?: default

fun ItemStack.getDeepInt(path: String, default: Int = 0) =
    getItemNBT(this, path)?.asInt() ?: default

fun ItemStack.getDeepString(path: String, default: String = "") =
    getItemNBT(this, path)?.asString() ?: default

fun ItemStack.getDeepLong(path: String, default: Long = 0L) =
    getItemNBT(this, path)?.asLong() ?: default

// --- 写入扩展（自动保存）---
fun ItemStack.setDeep(path: String, value: Any?) {
    if (this.isAir()) return
    val tag = this.getItemTag()
    if (value == null) tag.removeDeep(path) else tag.putDeep(path, value)
    tag.saveTo(this)
}

fun ItemStack.removeDeep(path: String) {
    if (this.isAir()) return
    val tag = this.getItemTag()
    tag.removeDeep(path)
    tag.saveTo(this)
}

// --- 校验扩展 ---
fun ItemStack?.hasCustomTag(path: String): Boolean {
    if (this == null || this.isAir()) return false
    val data = this.getItemTag().getDeep(path) ?: return false
    val str = data.asString()
    return str.isNotEmpty() && str != "none"
}

fun ItemStack?.hasTagValue(path: String, value: String): Boolean {
    if (this == null || this.isAir()) return false
    val data = this.getItemTag().getDeep(path) ?: return false
    return data.asString() == value
}

// --- ItemTag 扩展 ---
fun ItemTag.getDeepDouble(path: String, default: Double = 0.0) =
    this.getDeep(path)?.asDouble() ?: default

fun ItemTag.getDeepInt(path: String, default: Int = 0) =
    this.getDeep(path)?.asInt() ?: default

fun ItemTag.getDeepString(path: String, default: String = "") =
    this.getDeep(path)?.asString() ?: default

// --- Mod 快捷读取 ---
fun ItemStack.getModCost() = this.getDeepInt("Mod属性.消耗", 0)
fun ItemStack.getModPolarity() = this.getDeepString("Mod属性.极性", "无")

// --- 技能书快捷读取 ---
fun ItemStack.getSkillId(): String = this.getDeepString("技能书基础属性.技能id", "")

// --- 数值累加扩展 ---
fun ItemTag.addDeep(path: String, value: Double) {
    val current = this.getDeep(path)?.asDouble() ?: 0.0
    this.putDeep(path, current + value)
}

fun ItemTag.addDeepInt(path: String, value: Int) {
    val current = this.getDeep(path)?.asInt() ?: 0
    this.putDeep(path, current + value)
}

fun ItemStack.addDeep(path: String, value: Double) {
    if (this.isAir()) return
    val tag = this.getItemTag()
    tag.addDeep(path, value)
    tag.saveTo(this)
}