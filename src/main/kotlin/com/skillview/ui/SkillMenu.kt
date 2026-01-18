package com.skillview.ui

import com.skillview.data.SkillStorage
import com.skillview.util.SkillPacketSender
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

    // 将槽位映射提取为常量，方便维护
    private val CHAR_TO_SLOT = mapOf('A' to 11, 'B' to 12, 'C' to 13, 'D' to 14, 'E' to 15)
    private const val SKILL_TAG = "技能书基础属性.技能id"

    fun openSkillEquipMenu(player: Player) {
        player.openMenu<Chest>("&8技能装备栏".colored()) {
            map(
                "#########",
                "##ABCDE##",
                "#########"
            )

            // 1. 装饰背景设置
            set('#', buildItem(XMaterial.GRAY_STAINED_GLASS_PANE) { name = " " }) { isCancelled = true }

            // 2. 交互逻辑：限制只能放入带特定 NBT 的技能书
            onClick { event ->
                // 点击玩家背包区域直接放行
                if (event.rawSlot >= event.inventory.size) {
                    event.isCancelled = false
                    return@onClick
                }

                // 针对 A-E 五个槽位进行条件检查
                CHAR_TO_SLOT.values.forEach { targetSlot ->
                    event.conditionSlot(targetSlot,
                        condition = { put, _ ->
                            // 如果是拿起物品（put为空气）则允许；如果是放入，检查 NBT
                            put == null || put.isAir() || put.hasCustomTag(SKILL_TAG)
                        },
                        failedCallback = {
                            event.clicker.sendMessage("&c这里只能放入有效的技能书！".colored())
                        }
                    )
                }
            }

            // 3. 回显逻辑：从 SkillLoadout 对象中还原物品
            onBuild { _, inventory ->
                // 获取整个技能栏配装方案 (只读一次数据库)
                val loadout = SkillStorage.getSkillLoadout(player)

                // 按索引 (0..4) 填充到对应的 GUI 格子 (11..15)
                CHAR_TO_SLOT.entries.forEachIndexed { index, entry ->
                    val savedItem = loadout.slots[index]
                    if (savedItem != null && !savedItem.isAir()) {
                        inventory.setItem(entry.value, savedItem)
                    } else {
                        inventory.setItem(entry.value, ItemStack(Material.AIR))
                    }
                }
            }

            // 4. 保存逻辑：将当前 GUI 状态一次性存入 SkillLoadout
            onClose { event ->
                // 创建一个新的配装方案对象
                val newLoadout = SkillStorage.SkillLoadout()

                // 遍历 GUI 槽位
                CHAR_TO_SLOT.entries.forEachIndexed { index, entry ->
                    val item = event.inventory.getItem(entry.value)

                    // 双重校验：非空气且具备技能标签（防止意外卡入普通物品）
                    if (item != null && !item.isAir() && item.hasCustomTag(SKILL_TAG)) {
                        newLoadout.slots[index] = item
                    }
                }

                // 一次性保存整个 JSON 到数据库，效率最高
                SkillStorage.saveSkillLoadout(player, newLoadout)

                player.sendMessage("&a技能栏已同步到云端数据库！".colored())

                // 刷新客户端 HUD 缓存
                SkillPacketSender.sendSkillIdPacket(player)
            }
        }
    }
}