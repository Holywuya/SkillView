package com.skillview.data

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.module.configuration.Configuration

/**
 * RPG 系统常量与定义
 * 集中管理 NBT 键名、属性列表、路径常量
 * 所有魔法值均在此处定义，避免散落
 */
object RpgDefinitions {

    @taboolib.module.configuration.Config("config.yml")
    lateinit var con: Configuration

    object Config {

        var DEBUG = false

        @Awake(LifeCycle.ENABLE)
        fun onEnable() {
            DEBUG = con.getBoolean("DEBUG", false)
        }
    }

    // ==========================================
    //          NBT 路径常量（分层管理）
    // ==========================================

    /**
     * 技能书专属 NBT 常量
     */
    object SkillBookNBT {
        const val TYPE = NbtPaths.SkillBook.TYPE
        const val RARITY = NbtPaths.SkillBook.RARITY
        const val ROOT_BASIC = NbtPaths.SkillBook.ROOT_BASIC
        const val ROOT_MODIFIER = NbtPaths.SkillBook.ROOT_MODIFIER
        const val MOD_SLOTS = NbtPaths.SkillBook.MOD_SLOTS
        const val SKILL_ID = NbtPaths.SkillBook.SKILL_ID
        const val LEVEL = NbtPaths.SkillBook.LEVEL
    }

    /**
     * MOD（角色Mod & 技能Mod/强化石）专属 NBT 常量
     */
    object ModNBT {
        const val TYPE = NbtPaths.Mod.TYPE
        const val RARITY = NbtPaths.Mod.RARITY
        const val LEVEL = NbtPaths.Mod.LEVEL
        const val MOD_ID = NbtPaths.Mod.MOD_ID
        const val ROOT_MOD = NbtPaths.Mod.ROOT_MOD
        const val COST = NbtPaths.Mod.COST
        const val POLARITY = NbtPaths.Mod.POLARITY
    }

    // ==========================================
    //          属性白名单（用于强化逻辑）
    // ==========================================

    /**
     * 可强化属性白名单
     */
    val SkillMod_ATTRIBUTES = RpgConstants.AttributeLists.SKILL_MOD_ATTRIBUTES

    /**
     * 角色Mod 提供的全局属性列表
     */
    val PlayerMod_ATTRIBUTES = RpgConstants.AttributeLists.PLAYER_MOD_ATTRIBUTES

    // ==========================================
    //          AttributePlus 属性名常量
    // ==========================================

    /**
     * AttributePlus 注册的自定义属性名
     * 统一在此管理，避免字符串散落
     */
    object Attributes {
        const val COOLDOWN = RpgConstants.AttributeNames.COOLDOWN
        const val EFFICIENCY = RpgConstants.AttributeNames.EFFICIENCY
        const val MANA_MAX = RpgConstants.AttributeNames.MANA_MAX
        const val MANA_REGEN = RpgConstants.AttributeNames.MANA_REGEN
        const val DAMAGE_BONUS = RpgConstants.AttributeNames.DAMAGE_BONUS
        const val DAMAGE_MORE = RpgConstants.AttributeNames.DAMAGE_MORE
        const val EXTRA_RANGE = RpgConstants.AttributeNames.EXTRA_RANGE
        const val SKILL_POWER = RpgConstants.AttributeNames.SKILL_POWER
        const val CRIT_RATE = RpgConstants.AttributeNames.CRIT_RATE
        const val CRIT_DAMAGE = RpgConstants.AttributeNames.CRIT_DAMAGE
        const val MAX_HP = RpgConstants.AttributeNames.MAX_HP
    }
}
