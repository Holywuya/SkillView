package com.skillview.core.rpg

import com.skillview.core.mod.PlayerModLogic
import com.skillview.core.skill.SkillPacketSender
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object RpgRuntime {

    private val cooldowns = ConcurrentHashMap<UUID, MutableMap<String, Long>>()
    private val manaMap = ConcurrentHashMap<UUID, Double>()
    private const val DEFAULT_MANA = 100.0

    // 回蓝任务
    @Awake(LifeCycle.ACTIVE)
    private fun startManaRegenTask() {
        submit(period = 20L) {  // 1 秒
            Bukkit.getOnlinePlayers().forEach { player ->
                regenMana(player)
            }
        }
    }

    /**
     * 每秒为玩家恢复蓝量
     * 恢复量 = 基础回蓝 + AttributePlus 的“魔力恢复”属性值
     */
    private fun regenMana(player: Player) {
        val modStats = PlayerModLogic.getStats(player)

        val baseRegen = 2.0

        // 从 Mod 获取额外回蓝
        val modRegen = modStats.manaRegen

        val totalRegen = baseRegen + modRegen
        if (totalRegen <= 0) return

        val current = getMana(player)
        val max = getMaxMana(player)

        if (current < max) {
            val newMana = (current + totalRegen).coerceAtMost(max)
            manaMap[player.uniqueId] = newMana

        }
    }

    @SubscribeEvent
    fun onJoin(e: PlayerJoinEvent) {
        val uuid = e.player.uniqueId
        cooldowns[uuid] = ConcurrentHashMap()

        submit(delay = 100L) {
            val player = Bukkit.getPlayer(uuid) ?: return@submit
            if (player.isOnline) {
                val maxMana = getMaxMana(player)
                manaMap[uuid] = maxMana
                SkillPacketSender.sendSkillIdPacket(player)
            }
        }
    }

    @SubscribeEvent
    fun onQuit(e: PlayerQuitEvent) {
        manaMap.remove(e.player.uniqueId)
        cooldowns.remove(e.player.uniqueId)
    }



    // ==========================================
    //          冷却管理
    // ==========================================

    fun checkCooldown(player: Player, skillId: String): Double {
        val map = cooldowns[player.uniqueId] ?: return 0.0
        val expire = map[skillId] ?: return 0.0
        val left = expire - System.currentTimeMillis()
        return if (left <= 0) {
            map.remove(skillId)
            0.0
        } else left / 1000.0
    }

    fun setCooldown(player: Player, skillId: String, baseSeconds: Double, cdReduction: Double) {
        if (baseSeconds <= 0) return
        val finalSeconds = baseSeconds * (1 - cdReduction / 100.0)
        val map = cooldowns.computeIfAbsent(player.uniqueId) { ConcurrentHashMap() }
        map[skillId] = System.currentTimeMillis() + (finalSeconds * 1000).toLong()
    }

    // ==========================================
    //          蓝量管理
    // ==========================================

    fun getMaxMana(player: Player): Double = DEFAULT_MANA + PlayerModLogic.getStats(player).extraMana

    fun getMana(player: Player): Double {
        return manaMap[player.uniqueId] ?: getMaxMana(player).also { manaMap[player.uniqueId] = it }
    }

    fun takeMana(player: Player, baseAmount: Int, efficiency: Double): Boolean {
        if (baseAmount <= 0) return true

        val finalAmount = (baseAmount * (1 - efficiency / 100.0)).toInt().coerceAtLeast(0)
        val current = getMana(player)

        return if (current >= finalAmount) {
            manaMap[player.uniqueId] = current - finalAmount
            true
        } else false
    }

    fun giveMana(player: Player, baseAmount: Int) {
        if (baseAmount <= 0) return

        val current = getMana(player)
        val max = getMaxMana(player)
        manaMap[player.uniqueId] = (current + baseAmount).coerceAtMost(max)
    }
}