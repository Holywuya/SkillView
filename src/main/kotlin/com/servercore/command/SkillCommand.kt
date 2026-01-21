package com.servercore.command

import com.servercore.config.RpgConfig
import com.servercore.core.skill.SkillCaster
import com.servercore.data.SkillStorage
import com.servercore.ui.SkillMenu
import com.servercore.ui.SkillUpgradeMenu
import com.servercore.ui.mod.PlayerMod
import com.servercore.ui.mod.SkillMod
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.*
import taboolib.module.chat.colored
import top.maplex.arim.Arim

@CommandHeader(
    name = "skill",
    aliases = ["sk"],
    permission = "skill.use",
    newParser = true // 启用新一代解析器，支持选项(Options)和更智能的参数匹配
)
object SkillCommand {

    // --- 基础菜单指令 ---

    @CommandBody
    val open = subCommand {
        // 定义动态参数：菜单类型
        dynamic("menu") {
            // 提供 Tab 补全建议
            suggestion<Player> { _, _ ->
                listOf("equip", "upgrade", "player","skill")
            }

            execute<Player> { sender, context, _ ->
                // 获取参数并转换为小写，进行模糊匹配或精确匹配
                when (context["menu"].lowercase()) {
                    "equip", "skills" -> SkillMenu.openSkillEquipMenu(sender)
                    "upgrade", "enhance" -> SkillUpgradeMenu.openUpgradeMenu(sender)
                    "player" -> PlayerMod.openPlayerMod(sender)
                    "skill" -> SkillMod.openSkillMod(sender)
                    else -> sender.sendMessage("§c[系统] 未知的菜单类型! 可选: equip, upgrade, mod".colored())
                }
            }
        }

        // 可选：如果玩家只输入 /skill open 不带任何参数，默认执行的逻辑
        execute<Player> { sender, _, _ ->
            SkillMenu.openSkillEquipMenu(sender)
        }
    }

    // --- 战斗指令 ---

    @CommandBody
    val cast = subCommand {
        // 使用专用的 int 节点，自动验证输入并提供类型安全
        int("slotIndex") {
            suggestion<Player> { _, _ -> (0..4).map { it.toString() } }
            execute<Player> { sender, context, _ ->
                val index = context.int("slotIndex")
                SkillCaster.skillcast(sender, index.toString())
            }
        }
    }

    // --- 管理员指令 ---

    @CommandBody(permission = "skill.admin")
    val get = subCommand {
        dynamic("skillId") {
            suggestion<Player> { _, _ -> RpgConfig.getAllSkillIds() }
            execute<Player> { sender, context, _ ->
                giveItem(sender, context["skillId"])
            }
        }
    }

    @CommandBody(permission = "skill.admin")
    val give = subCommand {
        // 星愿点分支
        literal("points") {
            player("target") {
                int("amount") {
                    execute<CommandSender> { sender, context, _ ->
                        val target = context.player("target").cast<Player>()
                        val amount = context.int("amount")
                        val isSilent = context.hasOption("silent")

                        SkillStorage.addStarPoints(target, amount)

                        sender.sendMessage("§a[系统] 成功给予 ${target.name} §f$amount §a点星愿点。")
                        if (!isSilent) {
                            target.sendMessage("&6&l⭐ &7你获得了 &f$amount &7点 &e&l星愿点&7。".colored())
                        }
                    }
                }
            }
        }
        // 技能书分支
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

                        giveItem(target, skillId)
                        sender.sendMessage("§a[系统] 已成功给予 ${target.name} 技能书: §f$skillId")
                    }
                }
            }
        }
    }


    /**
     * 内部发放逻辑
     */
    private fun giveItem(player: Player, skillId: String) {
        val item = Arim.itemManager.parse2ItemStack("neigeitems:${skillId}", player)
        player.inventory.addItem(item.itemStack)
    }
}