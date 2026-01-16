package com.skillview.expansion

import org.bukkit.entity.Player
import su.nightexpress.coinsengine.api.CoinsEngineAPI


/**
 * 设置玩家指定货币的余额
 * @param amount 目标金额 (会自动拦截负数，强制最小为 0.0)
 */
fun setBalance(player: Player, currencyId: String, amount: Double) {
    // 1. 获取货币对象
    val currency = CoinsEngineAPI.getCurrency(currencyId) ?: return

    // 2. 数值安全检查：确保不会将余额设为负数
    val safeAmount = amount.coerceAtLeast(0.0)

    // 3. 执行设置操作
    CoinsEngineAPI.setBalance(player, currency, safeAmount)
}

/**
 * 获取玩家指定货币的余额
 */
fun getBalance(player: Player, currencyId: String): Double {
    // 1. 获取货币对象，将参数名改为 currencyId 以避免与变量名冲突
    val currency = CoinsEngineAPI.getCurrency(currencyId) ?: return 0.0

    // 2. 直接返回查询结果，省略中间变量和分号
    return CoinsEngineAPI.getBalance(player, currency)
}

/**
 * 扣除玩家特定货币的余额
 * @param player 玩家对象
 * @param currencyId 货币的字符串ID
 * @param amount 扣除数量
 * @return Boolean 是否扣除成功（余额不足或货币不存在返回 false）
 */
fun takeBalance(player: Player, currencyId: String, amount: Double): Boolean {
    // 1. 安全检查：如果扣除数量小于等于0，通常直接视为成功（或视业务需求拦截）
    if (amount <= 0.0) return true

    // 2. 获取货币对象（解决命名冲突，改用 currencyObj）
    val currencyObj = CoinsEngineAPI.getCurrency(currencyId) ?: return false

    // 3. 获取当前余额
    val currentBalance = CoinsEngineAPI.getBalance(player, currencyObj)

    // 4. 检查余额是否充足
    if (currentBalance < amount) {
        return false
    }

    // 5. 执行实际扣除操作 (关键：之前的代码漏掉了这一步)
    // 根据 CoinsEngine API，通常使用 removeBalance 或 setBalance
    return try {
        CoinsEngineAPI.removeBalance(player, currencyObj, amount)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}