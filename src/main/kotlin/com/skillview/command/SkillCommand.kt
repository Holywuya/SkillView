package com.skillview.command

import com.skillview.config.RpgConfig
import com.skillview.ui.SkillMenu
import com.skillview.ui.SkillUpgradeMenu
import com.skillview.ui.mod.PlayerMod
import com.skillview.ui.mod.SkillMod
import com.skillview.ui.mod.WeaponMod
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.*
import taboolib.module.chat.colored

@CommandHeader(
    name = "skill",
    aliases = ["sk"],
    permission = "skill.use",
    newParser = true
)
object SkillCommand {

    @CommandBody
    val open = subCommand {
        dynamic("menu") {
            suggestion<Player> { _, _ ->
                listOf("equip", "upgrade", "player", "skill", "weapon")
            }

            execute<Player> { sender, context, _ ->
                when (context["menu"].lowercase()) {
                    "equip", "skills" -> SkillMenu.openSkillEquipMenu(sender)
                    "upgrade", "enhance" -> SkillUpgradeMenu.openUpgradeMenu(sender)
                    "player" -> PlayerMod.openPlayerMod(sender)
                    "skill" -> SkillMod.openSkillMod(sender)
                    "weapon" -> WeaponMod.openWeaponMod(sender)
                    else -> sender.sendMessage("§c[系统] 未知的菜单类型! 可选: equip, upgrade, player, skill, weapon".colored())
                }
            }
        }

        execute<Player> { sender, _, _ ->
            SkillMenu.openSkillEquipMenu(sender)
        }
    }

    @CommandBody
    val cast = subCommand {
        int("slotIndex") {
            suggestion<Player> { _, _ -> (0..4).map { it.toString() } }
            execute<Player> { sender, context, _ ->
                val index = context.int("slotIndex")
                executeCast(sender, index)
            }
        }
    }

    @CommandBody(permission = "skill.admin")
    val get = subCommand {
        dynamic("skillId") {
            suggestion<Player> { _, _ -> RpgConfig.getAllSkillIds() }
            execute<Player> { sender, context, _ ->
                val success = CommandLogic.giveSkillBook(sender, context["skillId"])
                if (!success) {
                    sender.sendMessage("§c[错误] 给予技能书失败".colored())
                }
            }
        }
    }

    @CommandBody(permission = "skill.admin")
    val give = subCommand {
        literal("points") {
            player("target") {
                int("amount") {
                    execute<CommandSender> { sender, context, _ ->
                        val target = context.player("target").cast<Player>()
                        val amount = context.int("amount")
                        val isSilent = context.hasOption("silent")

                        CommandLogic.addStarPoints(target, amount)

                        sender.sendMessage("§a[系统] 成功给予 ${target.name} §f$amount §a点星愿点。")
                        if (!isSilent) {
                            target.sendMessage("&6&l⭐ &7你获得了 &f$amount &7点 &e&l星愿点&7。".colored())
                        }
                    }
                }
            }
        }

        literal("book") {
            player("target") {
                dynamic("skillId") {
                    suggestion<CommandSender> { _, _ -> RpgConfig.getAllSkillIds() }
                    execute<CommandSender> { sender, context, _ ->
                        val target = context.player("target").cast<Player>()
                        val skillId = context["skillId"]

                        if (RpgConfig.getSkill(skillId) == null) {
                            sender.sendMessage("§c[错误] 技能 ID §f$skillId §c不存在。")
                            return@execute
                        }

                        val success = CommandLogic.giveSkillBook(target, skillId)
                        if (success) {
                            sender.sendMessage("§a[系统] 已成功给予 ${target.name} 技能书: §f$skillId")
                        } else {
                            sender.sendMessage("§c[错误] 给予技能书失败")
                        }
                    }
                }
            }
        }
    }

    fun executeCast(player: Player, slotIndex: Int) {
        com.skillview.core.skill.SkillCaster.skillcast(player, slotIndex.toString())
    }
}
