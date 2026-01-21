package com.servercore.util

import com.servercore.data.SkillStorage
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import taboolib.platform.compat.PlaceholderExpansion

/**
 * SkillView 变量扩展
 * 使用方式: %skillview_star_points%
 */

object SkillPapiHook : PlaceholderExpansion {

    // 变量前缀：使用 %sv_参数%
    override val identifier: String = "sv"

    // 自动重载：当 PAPI 重载时自动重新注册
    override val autoReload: Boolean = true

    /**
     * 处理在线玩家变量请求
     */
    override fun onPlaceholderRequest(player: Player?, args: String): String {
        if (player == null) return "0"

        return when (args.lowercase()) {
            "star_points" -> SkillStorage.getStarPoints(player).toString()

            else -> "未知变量"
        }
    }

    /**
     * 处理离线玩家变量请求
     * 注意：SkillStorage 使用的是 DataContainer，它通常能通过离线玩家 UUID 获取数据
     */
    override fun onPlaceholderRequest(player: OfflinePlayer?, args: String): String {
        if (player == null) return "0"

        // 如果玩家当前在线，直接复用在线逻辑
        if (player.isOnline) {
            return onPlaceholderRequest(player.player, args)
        }

        // 处理离线逻辑（如果你的 SkillStorage 支持 OfflinePlayer）
        return when (args.lowercase()) {
            "star_points" -> {
                // 如果你的 getDataContainer 支持 OfflinePlayer，可以直接读取
                // 这里我们假设 DataContainer 必须是在线玩家，如果离线则显示 0
                "0"
            }
            else -> "未知变量"
        }
    }
}