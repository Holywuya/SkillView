package com.skillview.util.mythicmobs

import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.core.skills.variables.Variable
import io.lumine.mythic.core.skills.variables.VariableScope
import io.lumine.mythic.core.skills.variables.VariableType
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.library.reflex.Reflex.Companion.getProperty

fun setSkillData(player: Player, key: String, value: Double) {
    val variableManager = MythicBukkit.inst().variableManager

    val abstractPlayer = BukkitAdapter.adapt(player)

    val registry = variableManager.getRegistry(VariableScope.CASTER, abstractPlayer)

    // 写入变量
    registry.put(key, Variable.ofType(VariableType.FLOAT, value))
}

@SubscribeEvent
fun onQuit(e: PlayerQuitEvent) {
    val abstractPlayer = BukkitAdapter.adapt(e.player)
    val registry = MythicBukkit.inst().variableManager.getRegistry(VariableScope.CASTER, abstractPlayer)
    registry.getProperty<MutableMap<String, Any>>("entries")?.clear()
}