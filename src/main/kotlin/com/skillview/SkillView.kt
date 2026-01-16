package com.skillview

import org.bukkit.Bukkit
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.disablePlugin
import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile
import ink.ptms.um.Mythic
import taboolib.platform.compat.isEconomySupported
import taboolib.platform.compat.isPermissionSupported
import top.maplex.arim.Arim


object SkillView : Plugin() {
    @Config(value = "config.yml", migrate = true, autoReload = true)
    lateinit var config: ConfigFile


    override fun onEnable() {
        info("Successfully running SkillView For Arcartx!")

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            info("PlaceholderAPI 扩展已通过动态钩子注册！")
        } else {
            warning("未检测到 PlaceholderAPI，变量功能不可用。")
        }
        if (Bukkit.getPluginManager().getPlugin("Arcartx") != null) {
            info("Arcartx 扩展已注册！")
        } else {
            error("未检测到 Arcartx，功能不可用。")
        }
        if (Bukkit.getPluginManager().getPlugin("AttributePlus") != null) {
            info("AttributePlus 扩展已注册！")
        } else {
            error("未检测到 AttributePlus，功能不可用。")
        }

        if (Mythic.isLoaded()) {
            if (Mythic.API.isLegacy) {
                info("MythicMobs 4.X 扩展已注册！")
            } else {
                info("MythicMobs 5.X 扩展已注册！") }
        } else {
                error("未检测到 MythicMobs，功能不可用。")
        }

        checkVaultSupport()

        val source = Arim.itemManager.getSource("neigeitems")
        if (source.isLoaded) {
            info("Neigeitems 已加载")
        }else{
            warning("未检测到 Neigeitems，功能不可用。")
        }
    }

    override fun onDisable() {
        info("Successfully disable SkillView For Arcartx!")
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

