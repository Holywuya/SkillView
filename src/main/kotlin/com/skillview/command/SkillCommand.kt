package com.skillview.command

import com.skillview.config.RpgConfig
import com.skillview.core.skill.SkillCaster
import com.skillview.data.SkillStorage
import com.skillview.ui.ModEquipMenu
import com.skillview.ui.SkillMenu
import com.skillview.ui.SkillUpgradeMenu
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
        execute<Player> { sender, _, _ -> SkillMenu.openSkillEquipMenu(sender) }
    }

    @CommandBody
    val enhance = subCommand {
        execute<Player> { sender, _, _ -> SkillUpgradeMenu.openUpgradeMenu(sender) }
    }

    @CommandBody
    val mod = subCommand {
        execute<Player> { sender, _, _ -> ModEquipMenu.openModEquipMenu(sender) }
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