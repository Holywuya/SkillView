package com.servercore.ui.mod

import org.bukkit.entity.Player
import taboolib.library.xseries.XMaterial
import taboolib.module.chat.colored
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Chest
import taboolib.platform.util.buildItem

object WeaponMod {
    fun openWeaponMod(player: Player){
        player.openMenu<Chest>("&8武器MOD配装系统".colored()) {
            rows(4)
            map(
                "#######",
                "##M#M##",
                "##M#M##",
                "###U###"
            )
            val modSlots = getSlots('M')
            val filler = buildItem(XMaterial.GRAY_STAINED_GLASS_PANE) { name = " " }

            set('#', filler) { isCancelled = true }

        }
    }
}