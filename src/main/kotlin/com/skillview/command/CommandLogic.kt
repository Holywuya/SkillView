package com.skillview.command

import com.skillview.config.RpgConfig
import top.maplex.arim.Arim
import taboolib.module.chat.colored

object CommandLogic {

    fun giveSkillBook(player: org.bukkit.entity.Player, skillId: String): Boolean {
        val setting = RpgConfig.getSkill(skillId) ?: return false
        val item = Arim.itemManager.parse2ItemStack("neigeitems:${skillId}", player)
        player.inventory.addItem(item.itemStack)
        player.sendMessage("&a获得技能书: $skillId".colored())
        return true
    }

    fun addStarPoints(player: org.bukkit.entity.Player, amount: Int) {
        com.skillview.data.SkillStorage.addStarPoints(player, amount)
    }
}
