package com.skillview.util

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

object DamageUtil {

    /**
     * 模拟一次真实攻击，触发服务端完整的伤害计算流程
     *
     * @param caster 施法者 (属性来源)
     * @param target 受击目标
     * @param amount 技能的基础伤害值 (将作为本次攻击的 Base Damage)
     * @param ignoreImmunity 是否无视无敌帧 (多段伤害技能通常需要设为 true)
     */
    fun damage(caster: LivingEntity, target: LivingEntity, amount: Double, ignoreImmunity: Boolean = true) {
        // 1. 基础校验
        if (target.isDead || !target.isValid) return
        if (target == caster) return // 防止自己打自己

        // 2. 处理无敌帧 (No Damage Ticks)
        // RPG 技能通常频率高，默认的 0.5秒无敌帧会导致伤害丢失
        if (ignoreImmunity) {
            target.noDamageTicks = 0
        }

        // 3. 核心：调用 Bukkit 原生伤害方法
        // 这一步会自动完成你注释里描述的所有工作：
        //   - 系统创建 EntityDamageByEntityEvent
        //   - 广播事件 -> AttributePlus 监听到 -> 计算防御/暴击/增伤 -> 修改最终伤害
        //   - 扣除血量
        //   - 播放受击动画 (变红/击退)
        target.damage(amount, caster)
    }
}