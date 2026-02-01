package com.skillview.core.skill

import com.skillview.config.RpgConfig
import com.skillview.core.mod.PlayerModLogic
import com.skillview.core.mod.SkillModLogic
import com.skillview.core.rpg.RpgRuntime
import com.skillview.data.NbtPaths
import com.skillview.data.RpgDefinitions
import com.skillview.data.SkillStorage
import com.skillview.util.*
import com.skillview.util.mythicmobs.setSkillData
import ink.ptms.um.Mythic
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.function.throttle
import taboolib.module.chat.colored
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration

object SkillCaster {

    private const val ROOT_BASIC = RpgDefinitions.SkillBookNBT.ROOT_BASIC
    private const val ROOT_MODIFIER = RpgDefinitions.SkillBookNBT.ROOT_MODIFIER
    @Config("config.yml")
    lateinit var conf: Configuration

    private val DEBUG get() = RpgDefinitions.Config.DEBUG

    private val castThrottle = throttle<Player, String>(200) { player, slotIndexStr ->
        executeInternalCast(player, slotIndexStr)
    }

    @JvmStatic
    fun skillcast(player: Player, slotIndexStr: String) {
        castThrottle(player, slotIndexStr)
    }

    private data class SkillBookNBT(
        val manaReduction: Int,
        val dmgMore: Double,
        val dmgBonus: Double,
        val rangeRaw: Double,
        val skillPower: Double,
        val multiplier: Double,
        val level: Int,
        val cdReduction: Double
    )

    private fun readSkillBookNBT(item: ItemStack?): SkillBookNBT {
        return SkillBookNBT(
            manaReduction = item?.getDeepInt(NbtPaths.SkillBook.MANA_REDUCTION, 0) ?: 0,
            dmgMore = item?.getDeepDouble(NbtPaths.SkillBook.DAMAGE_MORE, 0.0) ?: 0.0,
            dmgBonus = item?.getDeepDouble(NbtPaths.SkillBook.DAMAGE_BONUS, 0.0) ?: 0.0,
            rangeRaw = item?.getDeepDouble(NbtPaths.SkillBook.EXTRA_RANGE, 0.0) ?: 0.0,
            skillPower = item?.getDeepDouble(NbtPaths.SkillBook.SKILL_POWER, 0.0) ?: 0.0,
            multiplier = item?.getDeepDouble(NbtPaths.SkillBook.MULTIPLIER, 0.0) ?: 0.0,
            level = item?.getDeepInt(NbtPaths.SkillBook.LEVEL, 0) ?: 0,
            cdReduction = item?.getDeepDouble(NbtPaths.SkillBook.COOLDOWN_REDUCTION, 0.0) ?: 0.0
        )
    }

    private fun executeInternalCast(player: Player, slotIndexStr: String) {
        val playerGlobalStats = PlayerModLogic.getStats(player)

        if (DEBUG) println("[Debug] >>> 释放流程启动 | 玩家: ${player.name}")

        val slotIndex = slotIndexStr.toIntOrNull() ?: return
        if (slotIndex !in 0..4) return
        val skillId = SkillStorage.getSkillId(player, slotIndex) ?: return

        val cdLeft = RpgRuntime.checkCooldown(player, skillId)
        if (cdLeft > 0) {
            if (DEBUG) println("[Debug] [拦截] 技能 $skillId 冷却中")
            player.sendMessage("&c技能冷却中: ${String.format("%.1f", cdLeft)}s".colored())
            return
        }

        val setting = RpgConfig.getSkill(skillId) ?: return
        val item = SkillStorage.getSkillItem(player, slotIndex)
        val bookModStats = SkillModLogic.getSkillBookModStats(item)
        val nbt = readSkillBookNBT(item)

        val totalEfficiency = playerGlobalStats.efficiency + bookModStats.efficiency
        val finalBaseMana = (setting.mana - nbt.manaReduction).coerceAtLeast(0)

        if (!RpgRuntime.takeMana(player, finalBaseMana, totalEfficiency)) {
            if (DEBUG) println("[Debug] [拦截] 蓝量不足")
            player.sendMessage("&b法力不足！".colored())
            return
        }

        if (DEBUG) println("[Debug] [计算] 正在读取技能书NBT与MOD属性...")

        val realDmgMoreMultiplier = (playerGlobalStats.damageMore + nbt.dmgMore + bookModStats.damageMore).toMultiplier()
        val realDmgBonusMultiplier = (playerGlobalStats.damageBonus + nbt.dmgBonus + bookModStats.damageBonus).toMultiplier()
        val realExtraRangeMultiplier = (playerGlobalStats.extraRange + nbt.rangeRaw + bookModStats.extraRange).toMultiplier()
        val realBaseMultiplierPercent = (setting.multiplierUp * nbt.level + setting.baseMultiplier + nbt.multiplier).toPercent()
        val realSkillPower = (playerGlobalStats.skillPower + nbt.skillPower + bookModStats.skillPower).toMultiplier()

        val basePlayerDamage = AttributeCoreHelper.getAttackDamage(player)
        val finalDamage = (((basePlayerDamage * realBaseMultiplierPercent * realSkillPower) * realDmgBonusMultiplier) * realDmgMoreMultiplier).round()
        
        setSkillData(player, "${skillId}_range", realExtraRangeMultiplier)
        setSkillData(player, "${skillId}_damage", finalDamage)

        if (DEBUG) {
            println("""
                [Debug] --- 属性同步详情 ($skillId) ---
                > 最终效率: $totalEfficiency x
                > 基础倍率: $realBaseMultiplierPercent x (等级 ${nbt.level} + NBT:${nbt.multiplier})
                > 最终伤害 (More): $realDmgMoreMultiplier x (技能MOD:${bookModStats.damageMore} + 玩家MOD:${playerGlobalStats.damageMore} + NBT:${nbt.dmgMore})
                > 伤害加成 (Bonus): $realDmgBonusMultiplier x (技能MOD:${bookModStats.damageBonus} + 玩家MOD:${playerGlobalStats.damageBonus} + NBT:${nbt.dmgBonus})
                > 额外范围: $realExtraRangeMultiplier x
                > 技能强度: $realSkillPower x
            """.trimIndent())
        }

        val success = Mythic.API.castSkill(caster = player, skillName = setting.mmSkill)

        if (success) {
            val totalCdReduction = (playerGlobalStats.cdReduction + nbt.cdReduction + bookModStats.cdReduction).coerceAtLeast(0.0)
            RpgRuntime.setCooldown(player, skillId, setting.cooldown, totalCdReduction)

            if (DEBUG) {
                println("[Debug] [成功] 释放成功 | 最终CD缩减: $totalCdReduction% (技能MOD:${bookModStats.cdReduction} + 玩家MOD:${playerGlobalStats.cdReduction} + NBT:${nbt.cdReduction})")
            }
        } else {
            RpgRuntime.giveMana(player, finalBaseMana)
            player.sendMessage("&c技能触发失败，请联系管理员进行问题解决".colored())
        }
    }
}
