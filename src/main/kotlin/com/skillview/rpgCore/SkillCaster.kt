package com.skillview.rpgCore

import com.skillview.SkillStorage
import com.skillview.SkillStorage.getSkillItem
import com.skillview.expansion.*
import com.skillview.modCore.ModRuntime
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

    private val DEBUG by lazy { conf.getBoolean("DEBUG") }

    private val castThrottle = throttle<Player, String>(200) { player, slotIndexStr ->
        executeInternalCast(player, slotIndexStr)
    }

    @JvmStatic
    fun  skillcast(player: Player, slotIndexStr: String) {
        castThrottle(player, slotIndexStr)
    }

    private fun executeInternalCast(player: Player, slotIndexStr: String) {
        val modStats = ModRuntime.getStats(player)
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
        val item = getSkillItem(player, slotIndex)


        val totalEfficiency = modStats.efficiency
        val nbtManaReduction = item?.getDeepInt("$ROOT_MODIFIER.魔力减耗", 0) ?: 0
        val finalBaseMana = (setting.mana - nbtManaReduction).coerceAtLeast(0)

        if (!RpgRuntime.takeMana(player, finalBaseMana, totalEfficiency)) {
            if (DEBUG) println("[Debug] [拦截] 蓝量不足")
            player.sendMessage("&b法力不足！".colored())
            return
        }

        /* --- 5. 核心同步（重负载计算） --- */
        if (DEBUG) println("[Debug] [计算] 正在读取 AP(含MOD) 与 技能书NBT 属性...")

        // A. 读取 AP 总属性 (玩家基础 + MOD加成)
        val modDmgMore    = modStats.damageMore
        val modDmgBonus   = modStats.damageBonus
        val modExtraRange = modStats.extraRange
        val modSkillPower = modStats.skillPower

        // B. 读取 技能书 NBT (仅该书持有的局部加成)
        val nbtDmgMore   = item?.getDeepDouble("$ROOT_MODIFIER.最终伤害", 0.0) ?: 0.0
        val nbtDmgBonus  = item?.getDeepDouble("$ROOT_MODIFIER.伤害加成", 0.0) ?: 0.0
        val nbtRangeRaw  = item?.getDeepDouble("$ROOT_MODIFIER.额外范围", 0.0) ?: 0.0
        val nbtSkillPower= item?.getDeepDouble("$ROOT_MODIFIER.技能强度",0.0) ?:0.0
        val nbtMultiplier= item?.getDeepDouble("$ROOT_MODIFIER.技能倍率",0.0) ?:0.0
        val nbtLevel     = item?.getDeepInt   ("$ROOT_BASIC.等级", 0) ?: 0

        // C. 数值合并并转换
        val realDmgMoreMultiplier     = (modDmgMore + nbtDmgMore).toMultiplier()
        val realDmgBonusMultiplier    = (modDmgBonus + nbtDmgBonus).toMultiplier()
        val realExtraRangeMultiplier  = (modExtraRange + nbtRangeRaw).toMultiplier()
        val realBaseMultiplierPercent = (setting.multiplierUp * nbtLevel + setting.baseMultiplier + nbtMultiplier).toPercent()
        val realSkillPower            = (modSkillPower + nbtSkillPower).toMultiplier()
        // 同步变量到 MythicMobs
        setSkillData(player, "${skillId}_BaseMultiplier", realBaseMultiplierPercent)
        setSkillData(player, "${skillId}_dmgmore",        realDmgMoreMultiplier)
        setSkillData(player, "${skillId}_range",          realExtraRangeMultiplier)
        setSkillData(player, "${skillId}_dmgbonus",       realDmgBonusMultiplier)
        setSkillData(player, "${skillId}_skillpower",     realSkillPower )
        if (DEBUG) {
            println("""
                [Debug] --- 属性同步详情 ($skillId) ---
                > 最终效率: $totalEfficiency%
                > 基础倍率: $realBaseMultiplierPercent% (等级 $nbtLevel + NBT:$nbtMultiplier)
                > 最终伤害 (More): $realDmgMoreMultiplier x (MOD:$modDmgMore + NBT:$nbtDmgMore)
                > 伤害加成 (Bonus): $realDmgBonusMultiplier x (MOD:$modDmgBonus + NBT:$nbtDmgBonus)
                > 额外范围: $realExtraRangeMultiplier x
                > 技能强度: $realSkillPower x
            """.trimIndent())
        }

        /* --- 6. 释放技能 --- */
        val success = Mythic.API.castSkill(caster = player, skillName = setting.mmSkill)

        if (success) {
            // 计算最终 CD 缩减 (MOD总和 + 技能书NBT)
            val modCdReduction = modStats.cdReduction
            val nbtCdReduction = item?.getDeepDouble("$ROOT_MODIFIER.冷却缩减", 0.0) ?: 0.0
            val totalCdReduction = (modCdReduction + nbtCdReduction).coerceAtLeast(0.0)

            RpgRuntime.setCooldown(player, skillId, setting.cooldown, totalCdReduction)

            if (DEBUG) {
                println("[Debug] [成功] 释放成功 | 最终CD缩减: $totalCdReduction% (MOD:$modCdReduction + NBT:$nbtCdReduction)")
            }
        } else {
            RpgRuntime.giveMana(player, finalBaseMana)
            player.sendMessage("&c技能触发失败，请联系管理员进行问题解决".colored())
        }
    }
}