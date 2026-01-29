package com.skillview.util

import org.bukkit.inventory.ItemStack
import taboolib.module.nms.ItemTag
import taboolib.module.nms.getItemTag
import taboolib.platform.util.isAir

fun ItemStack.getDeepDouble(path: String, default: Double = 0.0): Double {
    if (this.isAir()) return default
    return this.getItemTag().getDeep(path)?.asDouble() ?: default
}

fun ItemStack.getDeepInt(path: String, default: Int = 0): Int {
    if (this.isAir()) return default
    return this.getItemTag().getDeep(path)?.asInt() ?: default
}

fun ItemStack.getDeepString(path: String, default: String = ""): String {
    if (this.isAir()) return default
    return this.getItemTag().getDeep(path)?.asString() ?: default
}

fun ItemStack.getDeepLong(path: String, default: Long = 0L): Long {
    if (this.isAir()) return default
    return this.getItemTag().getDeep(path)?.asLong() ?: default
}

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

inline fun ItemTag.getDeepDouble(path: String, default: Double = 0.0) =
    this.getDeep(path)?.asDouble() ?: default

inline fun ItemTag.getDeepInt(path: String, default: Int = 0) =
    this.getDeep(path)?.asInt() ?: default

inline fun ItemTag.getDeepString(path: String, default: String = "") =
    this.getDeep(path)?.asString() ?: default

fun ItemStack.getModCost() = this.getDeepInt("Mod属性.消耗", 0)
fun ItemStack.getModPolarity() = this.getDeepString("Mod属性.极性", "无")

fun ItemStack.getSkillId(): String = this.getDeepString("技能书基础属性.技能id", "")

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