package com.skillview.ui

import com.skillview.data.managers.EnhanceManager
import com.skillview.data.RpgDefinitions
import com.skillview.util.getDeepString
import org.bukkit.entity.Player
import taboolib.module.chat.colored
import taboolib.library.xseries.XMaterial

object SkillUpgradeMenu {

    fun openUpgradeMenu(player: Player) {
        player.sendMessage("&7打开升级界面...".colored())
    }
}
