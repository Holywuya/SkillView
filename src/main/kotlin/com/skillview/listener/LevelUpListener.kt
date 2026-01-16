package com.skillview.listener

import com.skillview.SkillStorage
import org.bukkit.Sound
import org.bukkit.event.player.PlayerLevelChangeEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.module.chat.colored

object LevelUpListener {

    @SubscribeEvent
    fun onLevelChange(e: PlayerLevelChangeEvent) {
        val player = e.player
        val old = e.oldLevel
        val new = e.newLevel

        // 仅在等级确实提升时执行
        if (new > old) {
            val gain = new - old

            SkillStorage.addStarPoints(player, gain)

            player.sendMessage("&6&l⭐ &7等级提升！你获得了 &f$gain &7点 &e&l星源点&7。".colored())
            player.sendMessage("&8(当前总计: &f${SkillStorage.getStarPoints(player)}&8)".colored())

        }
    }
}