package com.skillview.core.skill

import com.skillview.config.RpgConfig
import com.skillview.core.mod.PlayerModLogic
import com.skillview.core.mod.SkillModLogic
import com.skillview.core.rpg.RpgRuntime
import com.skillview.data.RpgDefinitions
import com.skillview.data.SkillStorage
import com.skillview.util.*
import com.skillview.util.mythicmobs.setSkillData
import ink.ptms.um.Mythic
import org.bukkit.entity.Player
import taboolib.common.function.throttle
import taboolib.module.chat.colored
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration

object SkillCaster {

    private const val ROOT_BASIC = RpgDefinitions.SkillBookNBT.ROOT_BASIC
    private const val ROOT_MODIFIER = RpgDefinitions.SkillBookNBT.ROOT_MODIFIER
    @Config("config.yml")
    lateinit var conf: Configuration

    val DEBUG = RpgDefinitions.Config.DEBUG

    private val castThrottle = throttle<Player, String>(200) { player, slotIndexStr ->
        executeInternalCast(player, slotIndexStr)
    }

    @JvmStatic
    fun  skillcast(player: Player, slotIndexStr: String) {
        castThrottle(player, slotIndexStr)
    }

    private fun executeInternalCast(player: Player, slotIndexStr: String) {
        val playerGlobalStats = PlayerModLogic.getStats(player)

        if (DEBUG) println("[Debug] >>> 释放流程启动 | 玩家: ${player.name}")

        /* --- 1. 基础验证 --- */
        val slotIndex = slotIndexStr.toIntOrNull() ?: return
        if (slotIndex !in 0..4) return
        val skillId = SkillStorage.getSkillId(player, slotIndex) ?: return

        /* --- 2. 逻辑冷却拦截 --- */
        val cdLeft = RpgRuntime.checkCooldown(player, skillId)
        if (cdLeft > 0) {
            if (DEBUG) println("[Debug] [拦截] 技能 $skillId 冷却中")
            player.sendMessage("&c技能冷却中: ${String.format("%.1f", cdLeft)}s".colored())
            return
        }

        /* --- 3. 准备基础配置 --- */

        val setting = RpgConfig.getSkill(skillId) ?: return

        /* --- 4. 蓝量与效率预检 --- */

        if (DEBUG) println("[Debug] [计算] 正在读取蓝量消耗与效率...")
        //获取对应技能栏的技能书
        val item = SkillStorage.getSkillItem(player, slotIndex)
        //获取此技能书的MOD属性
        val bookModStats  = SkillModLogic.getSkillBookModStats(item)

        val totalEfficiency = playerGlobalStats.efficiency + bookModStats.efficiency

        val nbtManaReduction = item?.getDeepInt("$ROOT_MODIFIER.魔力减耗", 0) ?: 0

        val finalBaseMana = (setting.mana - nbtManaReduction).coerceAtLeast(0)

        if (!RpgRuntime.takeMana(player, finalBaseMana, totalEfficiency)) {
            if (DEBUG) println("[Debug] [拦截] 蓝量不足")
            player.sendMessage("&b法力不足！".colored())
            return
        }

        /* --- 5. 核心同步（重负载计算） --- */
        if (DEBUG) println("[Debug] [计算] 正在读取 AP(含MOD) 与 技能书NBT 属性...")


        //  读取 玩家MOD 总属性 (全局加成)
        val modPlayerDmgMore    = playerGlobalStats.damageMore
        val modPlayerBonus   = playerGlobalStats.damageBonus
        val modPlayerExtraRange = playerGlobalStats.extraRange
        val modPlayerSkillPower = playerGlobalStats.skillPower

        //  读取 技能书 MOD 属性 (局部加成)
        val modBookDmgMore    = bookModStats.damageMore
        val modBookDmgBonus   = bookModStats.damageBonus
        val modBookExtraRange = bookModStats.extraRange
        val modBookSkillPower = bookModStats.skillPower

        //  读取 技能书 NBT (局部加成)
        val nbtDmgMore   = item?.getDeepDouble("$ROOT_MODIFIER.最终伤害", 0.0) ?: 0.0
        val nbtDmgBonus  = item?.getDeepDouble("$ROOT_MODIFIER.伤害加成", 0.0) ?: 0.0
        val nbtRangeRaw  = item?.getDeepDouble("$ROOT_MODIFIER.额外范围", 0.0) ?: 0.0
        val nbtSkillPower= item?.getDeepDouble("$ROOT_MODIFIER.技能强度",0.0) ?:0.0
        val nbtMultiplier= item?.getDeepDouble("$ROOT_MODIFIER.技能倍率",0.0) ?:0.0
        val nbtLevel     = item?.getDeepInt   ("$ROOT_BASIC.等级", 0) ?: 0

        //  数值合并并转换
        val realDmgMoreMultiplier     = (modPlayerDmgMore + nbtDmgMore + modBookDmgMore).toMultiplier()
        val realDmgBonusMultiplier    = (modPlayerBonus + nbtDmgBonus + modBookDmgBonus).toMultiplier()
        val realExtraRangeMultiplier  = (modPlayerExtraRange + nbtRangeRaw + modBookExtraRange).toMultiplier()
        val realBaseMultiplierPercent = (setting.multiplierUp * nbtLevel + setting.baseMultiplier + nbtMultiplier).toPercent()
        val realSkillPower            = (modPlayerSkillPower + nbtSkillPower + modBookSkillPower).toMultiplier()

        // 同步变量到 MythicMobs
        val skillAttr = conf.getString("BaseAttr") ?:"物理伤害"
        val playerAttr = getattr(player,skillAttr)
        val finalDamage = ((playerAttr * realBaseMultiplierPercent * realSkillPower) * realDmgBonusMultiplier) * realDmgMoreMultiplier
        setSkillData(player, "${skillId}_range", realExtraRangeMultiplier) //额外范围
        setSkillData(player,"${skillId}_damage",finalDamage)

        if (DEBUG) {
            println("""
                [Debug] --- 属性同步详情 ($skillId) ---
                > 最终效率: $totalEfficiency x
                > 基础倍率: $realBaseMultiplierPercent x (等级 $nbtLevel + NBT:$nbtMultiplier)
                > 最终伤害 (More): $realDmgMoreMultiplier x (技能MOD:$modBookDmgMore + 玩家MOD:$modPlayerDmgMore + NBT:$nbtDmgMore)
                > 伤害加成 (Bonus): $realDmgBonusMultiplier x (技能MOD:$modBookDmgBonus + 玩家MOD:$modPlayerBonus + NBT:$nbtDmgBonus)
                > 额外范围: $realExtraRangeMultiplier x
                > 技能强度: $realSkillPower x
            """.trimIndent())
        }

        /* --- 6. 释放技能 --- */
        val success = Mythic.API.castSkill(caster = player, skillName = setting.mmSkill)

        if (success) {
            // 计算最终 CD 缩减 (MOD总和 + 技能书NBT)
            val modCdReductionPlayer = playerGlobalStats.cdReduction
            val modCdReductionSkill = bookModStats.cdReduction

            val nbtCdReduction = item?.getDeepDouble("$ROOT_MODIFIER.冷却缩减", 0.0) ?: 0.0
            val totalCdReduction = (modCdReductionPlayer + nbtCdReduction + modCdReductionSkill).coerceAtLeast(0.0)

            RpgRuntime.setCooldown(player, skillId, setting.cooldown, totalCdReduction)

            if (DEBUG) {
                println("[Debug] [成功] 释放成功 | 最终CD缩减: $totalCdReduction% (技能MOD:$modCdReductionSkill + 玩家MOD:$modCdReductionPlayer + NBT:$nbtCdReduction)")
            }
        } else {
            RpgRuntime.giveMana(player, finalBaseMana)
            player.sendMessage("&c技能触发失败，请联系管理员进行问题解决".colored())
        }
    }
}