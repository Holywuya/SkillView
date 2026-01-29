package com.skillview

import ink.ptms.um.Mythic
import org.bukkit.Bukkit
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile
import taboolib.platform.compat.isEconomySupported
import taboolib.platform.compat.isPermissionSupported
import top.maplex.arim.Arim


object SkillView : Plugin() {
    @Config(value = "config.yml", migrate = true, autoReload = true)
    lateinit var config: ConfigFile

    override fun onEnable() {
        info("Successfully running Server MmoCore!")

        checkPlugin("PlaceholderAPI", optional = true) { "PlaceholderAPI 扩展已通过动态钩子注册！" }
        checkPlugin("Arcartx", optional = false) { "Arcartx 扩展已注册！" }
        checkPlugin("AttributePlus", optional = true) { "AttributePlus 扩展已注册！" }

        if (Mythic.isLoaded()) {
            val version = if (Mythic.API.isLegacy) "4.X" else "5.X"
            info("MythicMobs $version 扩展已注册！")
        } else {
            error("未检测到 MythicMobs，功能不可用。")
        }

        checkVaultSupport()

        val source = Arim.itemManager.getSource("neigeitems")
        if (source.isLoaded) {
            info("Neigeitems 已加载")
        } else {
            warning("未检测到 Neigeitems，功能不可用。")
        }
    }

    override fun onDisable() {
        info("Successfully disable SkillView For Arcartx!")
    }

    private fun checkPlugin(name: String, optional: Boolean, successMsg: () -> String) {
        if (Bukkit.getPluginManager().getPlugin(name) != null) {
            info(successMsg())
        } else {
            val msg = "未检测到 $name，功能不可用。"
            if (optional) warning(msg) else error(msg)
        }
    }
}

fun checkVaultSupport() {
    // 检查经济系统是否可用
    if (isEconomySupported) {
        info("经济系统已启用")
    } else {
        warning("未检测到经济插件（需要 EssentialsX、CMI 等）")
    }

    // 检查权限系统是否可用
    if (isPermissionSupported) {
        info("权限系统已启用")
    } else {
        warning("未检测到权限插件（需要 LuckPerms、PermissionsEx 等）")
    }
}

