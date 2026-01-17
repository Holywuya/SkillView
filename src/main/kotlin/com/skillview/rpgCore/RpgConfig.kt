package com.skillview.rpgCore

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import java.util.concurrent.ConcurrentHashMap



object RpgConfig {

    @Config("skills.yml")
    lateinit var conf: Configuration

    @Config("mods.yml")
    lateinit var modConf: Configuration

    @Config("config.yml")
    lateinit var con: Configuration

    // 缓存容器
    private val skills = ConcurrentHashMap<String, SkillSetting>()
    private val mods = ConcurrentHashMap<String, ModSetting>()

    @Awake(LifeCycle.ACTIVE)
    fun load() {
        loadSkills()
        loadMods()
    }

    /**
     * 加载技能书配置
     */
    private fun loadSkills() {
        skills.clear()
        conf.getKeys(false).forEach { id ->
            val section = conf.getConfigurationSection(id) ?: return@forEach
            skills[id] = SkillSetting(
                mmSkill = section.getString("mm-skill") ?: "ExampleSkill",
                cooldown = section.getDouble("cooldown", 0.0),
                mana = section.getInt("mana", 0),
                baseMultiplier = section.getDouble("BaseMultiplier", 0.0),
                maxLevel = section.getInt("LevelUP.MaxLevel", 0),
                reduceMana = section.getDouble("LevelUP.ReduceMana", 0.0),
                reduceCd = section.getDouble("LevelUP.ReduceCd", 0.0),
                multiplierUp = section.getDouble("LevelUP.MultiplierUp", 0.0),
                enhanceMultiplier = section.getDouble("enhance-multiplier", 0.0),
                tags = section.getStringList("tags")
            )
        }
        println("§a[SkillView] 已成功加载 ${skills.size} 个技能配置")
    }

    /**
     * 加载 MOD/灵石配置
     */
    private fun loadMods() {
        mods.clear()
        modConf.getKeys(false).forEach { id ->
            val section = modConf.getConfigurationSection(id) ?: return@forEach

            // 解析属性增量 Map
            val attrSection = section.getConfigurationSection("attributes")
            val attrMap = mutableMapOf<String, Double>()
            attrSection?.getKeys(false)?.forEach { attrKey ->
                attrMap[attrKey] = attrSection.getDouble(attrKey)
            }

            mods[id] = ModSetting(
                rarity = section.getString("rarity") ?: "普通",
                polarity = section.getString("polarity") ?: "无",
                baseDrain = section.getInt("base-drain", 0),
                drainStep = section.getInt("drain-step", 0),
                maxLevel = section.getInt("max-level", 0),
                attributes = attrMap,
                tags = section.getStringList("tags")
            )
        }
        println("§a[SkillView] 已成功加载 ${mods.size} 个 MOD 配置")
    }

    // --- 技能书相关 API ---
    fun getSkill(id: String): SkillSetting? = skills[id]
    fun getAllSkillIds(): List<String> = skills.keys.toList()

    // --- MOD 相关 API ---
    fun getMod(id: String): ModSetting? = mods[id]
    fun getAllModIds(): List<String> = mods.keys.toList()
}