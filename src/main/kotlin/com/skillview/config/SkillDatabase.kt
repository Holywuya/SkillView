package com.skillview.config

import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.LifeCycle
import taboolib.common.io.newFile
import taboolib.common.platform.Awake
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.disablePlugin
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.submit
import taboolib.expansion.releasePlayerDataContainer
import taboolib.expansion.setupDataContainer
import taboolib.expansion.setupPlayerDatabase
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration

object SkillDatabase {

    @SubscribeEvent
    fun onJoin(e: PlayerJoinEvent) {
        submit(async = true) { e.player.setupDataContainer() }
    }

    @SubscribeEvent
    fun onQuit(e: PlayerQuitEvent) {
        e.player.uniqueId.releasePlayerDataContainer()
    }

    @Config("config.yml")
    lateinit var conf: Configuration

    @Awake(LifeCycle.ENABLE)
    fun init() {
        try {
            // 根据配置决定是连 MySQL 还是创建本地 SQLite 文件
            if (conf.getBoolean("Database.enable")) {
                val host = conf.getString("Database.host", "localhost")!!
                val port = conf.getInt("Database.port", 3306)
                val user = conf.getString("Database.user", "root")!!
                val password = conf.getString("Database.password", "")!!
                val database = conf.getString("Database.database", "minecraft")!!
                val table = conf.getString("Database.table", "player_skills_kv")!!
                setupPlayerDatabase(host, port, user, password, database, table)
            } else {
                setupPlayerDatabase(newFile(getDataFolder(), "data.db"))
            }
            println("数据库初始化成功！")
        } catch (ex: Throwable) {
            ex.printStackTrace()
            println("数据库初始化失败，插件即将关闭！")
            disablePlugin()
        }
    }
}