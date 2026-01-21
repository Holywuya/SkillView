package com.skillview.core.mod

import com.skillview.data.RpgDefinitions
import com.skillview.util.getDeepDouble
import com.skillview.util.getDeepString
import org.bukkit.inventory.ItemStack
import taboolib.module.nms.getItemTag
import taboolib.platform.util.isAir
import top.maplex.arim.tools.gson.GsonUtils

object SkillModLogic {

    // NBT 路径常量
    private const val NBT_MOD_ROOT = RpgDefinitions.SkillBookNBT.MOD_SLOTS

    /**
     * 将 MOD 安装到技能书上
     * @param book 目标技能书
     * @param modItem 准备安装的 MOD 物品
     * @param slotIndex 目标插槽索引 (0-7)
     * @return Boolean 是否安装成功
     */
    fun installModToBook(book: ItemStack, modItem: ItemStack, slotIndex: Int): Boolean {
        if (book.isAir() || modItem.isAir()) return false

        // 1. 获取物品 Tag
        val bookTag = book.getItemTag()

        // 3. 序列化 MOD 物品为 JSON
        val modJson = GsonUtils.toJson(modItem)

        // 4. 写入 NBT
        // 路径示例: MOD系统.插槽.0 = "{...}"
        bookTag.putDeep("$NBT_MOD_ROOT.插槽.$slotIndex", modJson)

        // 5. 保存回物品
        bookTag.saveTo(book)
        return true
    }

    fun removeModFromBook(book: ItemStack, slotIndex: Int) {
        if (book.isAir()) return

        // 1. 获取物品 Tag
        val bookTag = book.getItemTag()

        // 2. 移除指定插槽的 MOD 数据
        bookTag.removeDeep("$NBT_MOD_ROOT.插槽.$slotIndex")

        // 3. 保存回物品
        bookTag.saveTo(book)
    }

    fun getSkillModInSlot(book: ItemStack, index: Int): ItemStack? {
        val json = book.getDeepString("$NBT_MOD_ROOT.插槽.$index")
        if (json.isEmpty()) return null
        return GsonUtils.fromJson(json, ItemStack::class.java)
    }

    /**
     * 获取技能书特定槽位上的 MOD 物品
     * 用于 UI 渲染回显
     */
    fun getModInSlot(book: ItemStack, index: Int): ItemStack? {
        if (book.isAir()) return null

        // 读取存储的 JSON 字符串
        val modJson = book.getDeepString("$NBT_MOD_ROOT.插槽.$index")
        if (modJson.isEmpty() || modJson == "null") return null

        return try {
            GsonUtils.fromJson(modJson, ItemStack::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 【核心函数】汇总技能书上所有 MOD 的属性
     * 供 SkillCaster 释放技能时调用
     */
    fun getSkillBookModStats(book: ItemStack?): ModStats.ModStats {
        val totalStats = ModStats.ModStats()
        if (book.isAir() && book == null) return totalStats

        // 1. 获取所有定义的属性列表
        val attributes = try {
            RpgDefinitions.MOD_GLOBAL_ATTRIBUTES
        } catch (e: Exception) {
            listOf("伤害加成", "最终伤害", "冷却缩减", "技能效率")
        }

        // 2. 遍历技能书的所有 MOD 插槽
        for (i in 0..3) {
            val modItem = getModInSlot(book, i) ?: continue
            if (modItem.isAir()) continue

            // 3. 遍历属性大全，累加该 MOD 的所有属性
            attributes.forEach { attrName ->
                // 从 MOD 物品读取属性：Mod属性.基础伤害 等
                val value = modItem.getDeepDouble("${RpgDefinitions.ModNBT.ROOT_MOD}.$attrName", 0.0)
                if (value != 0.0) {
                    totalStats.add(attrName, value)
                }
            }
        }
        return totalStats
    }
}