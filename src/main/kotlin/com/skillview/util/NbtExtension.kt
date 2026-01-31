package com.skillview.util

import org.bukkit.inventory.ItemStack
import taboolib.module.nms.ItemTag
import taboolib.module.nms.getItemTag
import taboolib.platform.util.isAir
import java.util.*

/**
 * ItemTag 缓存系统
 * 避免每次NBT访问都创建新的ItemTag对象
 */
private object ItemTagCache {
    private data class CachedTag(val tag: ItemTag, val hashCode: Int)
    
    private val cache = WeakHashMap<ItemStack, CachedTag>()
    
    fun get(item: ItemStack): ItemTag {
        val hash = item.hashCode()
        val cached = cache[item]
        
        return if (cached != null && cached.hashCode == hash) {
            cached.tag
        } else {
            val newTag = item.getItemTag()
            cache[item] = CachedTag(newTag, hash)
            newTag
        }
    }
    
    fun invalidate(item: ItemStack) {
        cache.remove(item)
    }
}

fun ItemStack.getCachedTag(): ItemTag {
    if (this.isAir()) return ItemTag()
    return ItemTagCache.get(this)
}

fun ItemStack.getDeepDouble(path: String, default: Double = 0.0): Double {
    if (this.isAir()) return default
    return this.getCachedTag().getDeep(path)?.asDouble() ?: default
}

fun ItemStack.getDeepInt(path: String, default: Int = 0): Int {
    if (this.isAir()) return default
    return this.getCachedTag().getDeep(path)?.asInt() ?: default
}

fun ItemStack.getDeepString(path: String, default: String = ""): String {
    if (this.isAir()) return default
    return this.getCachedTag().getDeep(path)?.asString() ?: default
}

fun ItemStack.getDeepLong(path: String, default: Long = 0L): Long {
    if (this.isAir()) return default
    return this.getCachedTag().getDeep(path)?.asLong() ?: default
}

fun ItemStack.setDeep(path: String, value: Any?) {
    if (this.isAir()) return
    ItemTagCache.invalidate(this)
    val tag = this.getItemTag()
    if (value == null) tag.removeDeep(path) else tag.putDeep(path, value)
    tag.saveTo(this)
}

fun ItemStack.removeDeep(path: String) {
    if (this.isAir()) return
    ItemTagCache.invalidate(this)
    val tag = this.getItemTag()
    tag.removeDeep(path)
    tag.saveTo(this)
}

fun ItemStack?.hasCustomTag(path: String): Boolean {
    if (this == null || this.isAir()) return false
    val data = this.getCachedTag().getDeep(path) ?: return false
    val str = data.asString()
    return str.isNotEmpty() && str != "none"
}

fun ItemStack?.hasTagValue(path: String, value: String): Boolean {
    if (this == null || this.isAir()) return false
    val data = this.getCachedTag().getDeep(path) ?: return false
    return data.asString() == value
}

fun ItemTag.getDeepDouble(path: String, default: Double = 0.0) =
    this.getDeep(path)?.asDouble() ?: default

fun ItemTag.getDeepInt(path: String, default: Int = 0) =
    this.getDeep(path)?.asInt() ?: default

fun ItemTag.getDeepString(path: String, default: String = "") =
    this.getDeep(path)?.asString() ?: default

fun ItemStack.getModCost() = this.getDeepInt("Mod属性.消耗", 0)
fun ItemStack.getModPolarity() = this.getDeepString("Mod属性.极性", "无")
fun ItemStack.getModLevel() = this.getDeepInt("Mod.等级", 0)
fun ItemStack.getModId() = this.getDeepString("Mod.id", "")

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
    ItemTagCache.invalidate(this)
    val tag = this.getItemTag()
    tag.addDeep(path, value)
    tag.saveTo(this)
}
