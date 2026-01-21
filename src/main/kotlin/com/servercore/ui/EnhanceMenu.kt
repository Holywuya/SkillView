package com.servercore.ui

import com.servercore.config.RpgConfig
import com.servercore.data.RpgDefinitions
import com.servercore.data.SkillStorage.getStarPoints
import com.servercore.data.SkillStorage.takeStarPoints
import com.servercore.util.*
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.submit
import taboolib.library.xseries.XMaterial
import taboolib.module.chat.colored
import taboolib.module.nms.getItemTag
import taboolib.module.ui.openMenu
import taboolib.module.ui.returnItems
import taboolib.module.ui.type.Chest
import taboolib.platform.compat.getBalance
import taboolib.platform.compat.withdrawBalance
import taboolib.platform.util.buildItem
import taboolib.platform.util.isAir

object SkillUpgradeMenu {

    /**
     * 打开技能强化/升级界面
     */
    fun openUpgradeMenu(player: Player) {
        player.openMenu<Chest>("&8技能系统 &0- &8System".colored()) {
            rows(3)
            map(
                "#########",
                "###S#I###",
                "####U####"
            )

            // 1. 装饰背景
            set('#', buildItem(XMaterial.GRAY_STAINED_GLASS_PANE) { name = " " }) { isCancelled = true }

            // 2. 核心槽位 (Slot 12)
            set('S', ItemStack(Material.AIR)) { isCancelled = false }

            // 3. 需求展示槽位 (Slot 14)
            set('I', buildItem(XMaterial.BARRIER) { name = "&c请放入物品".colored() }) { isCancelled = true }

            // 4. 确认按钮
            set('U', buildItem(XMaterial.ANVIL) {
                name = "&a&l确认处理".colored()
                lore.add("&7请放入技能书或强化石".colored())
            }) { isCancelled = true }

            // --- 动态刷新逻辑：根据 NBT 类型切换显示 ---
            onBuild(async = false) { _, inventory ->
                submit(period = 10L) {
                    if (inventory.viewers.isEmpty()) {
                        this.cancel()
                        return@submit
                    }
                    val item = inventory.getItem(getFirstSlot('S'))
                    val infoSlot = getFirstSlot('I')
                    // 安全读取类型：空物品或无 NBT 时设为 "未知"
                    val type = if (item == null || item.isAir()) "未知" else item.getDeepString(RpgDefinitions.ModNBT.TYPE, "未知")

                    when {
                        type.contains("Mod", ignoreCase = true) -> {
                            val cost = modCalculation(item!!)
                            // 修正为读取 Mod 的等级路径
                            val currentLv = item.getDeepInt(RpgDefinitions.ModNBT.LEVEL, 1)
                            inventory.setItem(infoSlot, buildItem(XMaterial.EMERALD) {
                                name = "§eMod升级 §7(Lv.$currentLv -> §aLv.${cost.nextLevel}§7)".colored()
                                lore.add("")
                                lore.add("§7升级消耗:".colored())
                                lore.add("§7- §b灵能核心: §f${cost.endo}".colored())
                                lore.add("§7- §6所需金币: §f${cost.credits}".colored())
                                lore.add("")
                                lore.add("§a▶ 点击铁砧提升Mod等级".colored())
                            })
                        }

                        type == "技能书" -> {
                            // 技能书逻辑：词条/插槽强化 (此处展示占位)
                            val skillId = item!!.getDeepString(RpgDefinitions.SkillBookNBT.SKILL_ID, "未知")
                            inventory.setItem(infoSlot, buildItem(XMaterial.ENCHANTED_BOOK) {
                                name = "§b技能书强化 §7($skillId)".colored()
                                lore.add("")
                                lore.add("§7当前状态:".colored())
                                lore.add("§7- §f已开启插槽: §e2/3".colored())
                                lore.add("§7- §f附加属性: §a已锁定".colored())
                                lore.add("")
                                lore.add("§d▶ 点击铁砧执行技能书强化".colored())
                            })
                        }

                        else -> {
                            inventory.setItem(
                                infoSlot,
                                buildItem(XMaterial.BARRIER) { name = "&c无效物品类型".colored() })
                        }
                    }
                }
            }

            // --- 升级执行逻辑 ---
            onClick('U') {event ->
                val item = event.getItem('S')

                if (item == null || item.isAir()) {
                    player.sendMessage("§c[系统] 槽位是空的！".colored())
                    return@onClick
                }

                val type = item.getDeepString("类型")

                when {
                    type.contains("Mod", ignoreCase = true) -> {
                        // --- 逻辑：Mod 等级提升 ---
                        // 1. 获取基础配置与 ID
                        val modId = item.getDeepString(RpgDefinitions.ModNBT.MOD_ID)
                        val setting = RpgConfig.getMod(modId)
                            ?: return@onClick player.sendMessage("§c[系统] 找不到该 Mod 的配置信息。".colored())

                        // 2. 获取当前等级与上限
                        val currentLevel = item.getDeepInt(RpgDefinitions.ModNBT.LEVEL, 0)
                        val maxLevel = setting.maxLevel // 从配置读取最大等级更安全

                        // 校验：是否已满级
                        if (currentLevel >= maxLevel) {
                            player.sendMessage("§c[系统] 此 Mod 已经达到最高等级 (§fLv.$maxLevel§c)".colored())
                            return@onClick
                        }

                        // 3. 计算消耗
                        val cost = modCalculation(item)
                        val balance = player.getBalance()
                        val psi = getBalance(player, "灵能核心")

                        // --- 资源预检 ---
                        if (balance < cost.credits) {
                            player.sendMessage("§c[系统] 金币不足！需要 ${cost.credits} (当前: ${balance.toInt()})".colored())
                            return@onClick
                        }
                        if (psi < cost.endo) {
                            player.sendMessage("§c[系统] 灵能核心不足！需要 ${cost.endo} (当前: ${psi.toInt()})".colored())
                            return@onClick
                        }

                        // 4. 确认资源充足，开始扣费
                        player.withdrawBalance(cost.credits.toDouble())
                        takeBalance(player, "灵能核心", cost.endo.toDouble())

                        // 5. 执行 NBT 更新
                        val itemTag = item.getItemTag()

                        // 更新等级
                        itemTag.putDeep(RpgDefinitions.ModNBT.LEVEL, cost.nextLevel)

                        // --- 动态属性成长逻辑 ---
                        // 根据 RpgConfig 里定义的 attributes 增量表，自动增加所有对应属性
                        setting.attributes.forEach { (attrName, perLevelAdd) ->
                            // 目标路径：Mod属性.基础伤害 等
                            val path = "${RpgDefinitions.ModNBT.ROOT_MOD}.$attrName"
                            // 使用 addDeep 扩展函数进行累加
                            itemTag.addDeep(path, perLevelAdd)
                        }

                        itemTag.saveTo(item)

                        // 6. 反馈效果
                        player.playSound(player.location, Sound.BLOCK_ANVIL_USE, 1f, 1.2f)
                        player.sendMessage("&a&l⭐ 升级成功！ &7Mod等级已提升至 &fLv.${cost.nextLevel}".colored())


                    }


                    // 2. 完全匹配：必须完全等于 "技能书"
                    type == "技能书" -> {
                        // --- 逻辑：技能书专属强化 ---
                        val cost = calculateSkillBookCost(item)
                        val level = item.getDeepInt(RpgDefinitions.SkillBookNBT.LEVEL,0)
                        val skillId = item.getSkillId()
                        val setting = RpgConfig.getSkill(skillId) ?: return@onClick

                        //校验等级
                        if (level >= setting.maxLevel){
                            player.sendMessage("§c[系统] 技能${skillId}已经满级".colored())
                            return@onClick
                        }

                        if (!takeStarPoints(player, cost.starpotions)) {
                            val currentStar = getStarPoints(player)
                            player.sendMessage("§c[系统] 星源点不足！需要 §f${cost.starpotions} §c(当前: §f$currentStar§c)".colored())
                            return@onClick
                        }

                        val itemTag = item.getItemTag()
                        itemTag.putDeep(RpgDefinitions.SkillBookNBT.LEVEL, cost.nextLevel)
                        itemTag.addDeep("${RpgDefinitions.SkillBookNBT.ROOT_MODIFIER}.技能倍率",setting.enhanceMultiplier)
                        itemTag.saveTo(item)


                        player.sendMessage("&a&l强化成功！ &7技能等级已提升至 &fLv.${cost.nextLevel},提升倍率${setting.enhanceMultiplier}".colored())
                    }

                    // 3. 其他情况
                    else -> {
                        player.sendMessage("§c[系统] 该物品无法在强化槽内处理。".colored())
                    }
                }

            }

            // 5. 关闭时归还物品
            onClose { event ->
                event.returnItems(getSlots('S'))
            }
        }

    }
}