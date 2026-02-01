package com.skillview.ui.mod

import com.skillview.core.mod.SlotPolarityManager
import org.bukkit.entity.Player
import taboolib.library.xseries.XMaterial
import taboolib.module.chat.colored
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Chest
import taboolib.platform.util.buildItem

/**
 * 可复用的极性选择菜单
 * 
 * 用法:
 * PolaritySelectionMenu.openPolaritySelection(
 *     player = player,
 *     modType = SlotPolarityManager.ModType.WEAPON,
 *     slotIndex = 0,
 *     onSelect = { polarity -> /* 处理选择 */ }
 * )
 */
object PolaritySelectionMenu {

    enum class PolarityType(val symbol: String, val color: String, val display: String, val material: XMaterial) {
        MADURAI("V", "&c", "红 - Madurai (攻击/伤害)", XMaterial.RED_DYE),
        VAZARIN("D", "&b", "蓝 - Vazarin (防御/护甲)", XMaterial.BLUE_DYE),
        NARAMON("-", "&a", "绿 - Naramon (敏捷/速度)", XMaterial.LIME_DYE),
        ZENURIK("=", "&9", "蓝紫 - Zenurik (能量/效率)", XMaterial.PURPLE_DYE),
        UNAIRU("R", "&6", "橙 - Unairu (生存/冷却)", XMaterial.ORANGE_DYE),
        PENJAGA("Y", "&d", "粉 - Penjaga (状态/CC)", XMaterial.MAGENTA_DYE),
        UNIVERSAL("*", "&f", "通用 - Universal (任意匹配)", XMaterial.WHITE_DYE),
        NONE("无", "&7", "清除 - 无极性", XMaterial.GRAY_DYE)
    }

    /**
     * 打开极性选择菜单
     * 
     * @param player 玩家
     * @param modType MOD类型 (WEAPON/PLAYER/SKILL)
     * @param slotIndex 槽位索引 (0-7)
     * @param currentPolarity 当前极性 (可空)
     * @param onSelect 选择回调 - 参数为选中的极性符号 (如 "V", "D", "-", "无")
     */
    fun openPolaritySelection(
        player: Player,
        modType: SlotPolarityManager.ModType,
        slotIndex: Int,
        currentPolarity: String? = null,
        onSelect: (polarity: String) -> Unit
    ) {
        player.openMenu<Chest>("&8选择极性 - 槽位 #$slotIndex".colored()) {
            rows(2)
            map(
                "#########",
                "VDBRYPU*N"
            )

            val filler = buildItem(XMaterial.GRAY_STAINED_GLASS_PANE) { name = " " }
            set('#', filler) { isCancelled = true }

            // 设置各个极性槽位
            PolarityType.values().forEachIndexed { index, polarity ->
                val slot = when (polarity) {
                    PolarityType.MADURAI -> 'V'
                    PolarityType.VAZARIN -> 'D'
                    PolarityType.NARAMON -> 'B'
                    PolarityType.ZENURIK -> 'R'
                    PolarityType.UNAIRU -> 'Y'
                    PolarityType.PENJAGA -> 'P'
                    PolarityType.UNIVERSAL -> 'U'
                    PolarityType.NONE -> '*'
                }

                set(slot, buildItem(polarity.material) {
                    name = "${polarity.color}${polarity.display}".colored()
                    lore.addAll(listOf(
                        "",
                        if (currentPolarity == polarity.symbol) "&e✔ 已选中" else "&7点击选择",
                        "&7选择此极性用于该槽位"
                    ).map { it.colored() })
                }) {
                    isCancelled = true
                    onSelect(polarity.symbol)
                    player.closeInventory()
                }
            }

            set('N', buildItem(XMaterial.BARRIER) {
                name = "&c清除极性".colored()
                lore.addAll(listOf(
                    "",
                    if (currentPolarity == "无") "&e✔ 已选中" else "&7点击清除",
                    "&7移除此槽位的极性配置"
                ).map { it.colored() })
            }) {
                isCancelled = true
                onSelect("无")
                player.closeInventory()
            }
        }
    }
}
