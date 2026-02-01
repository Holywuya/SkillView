package com.skillview.data

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.module.configuration.Configuration

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

    object SkillBookNBT {
        const val TYPE = NbtPaths.SkillBook.TYPE
        const val RARITY = NbtPaths.SkillBook.RARITY
        const val ROOT_BASIC = NbtPaths.SkillBook.ROOT_BASIC
        const val ROOT_MODIFIER = NbtPaths.SkillBook.ROOT_MODIFIER
        const val MOD_SLOTS = NbtPaths.SkillBook.MOD_SLOTS
        const val MOD_SLOT_FORMAT = NbtPaths.SkillBook.MOD_SLOT_FORMAT
        const val SKILL_ID = NbtPaths.SkillBook.SKILL_ID
        const val LEVEL = NbtPaths.SkillBook.LEVEL
    }

     object ModNBT {
         const val TYPE = NbtPaths.Mod.TYPE
         const val RARITY = NbtPaths.Mod.RARITY
         const val LEVEL = NbtPaths.Mod.LEVEL
         const val MOD_ID = NbtPaths.Mod.MOD_ID
         const val ROOT_MOD = NbtPaths.Mod.ROOT_MOD
         const val COST = NbtPaths.Mod.COST
     }

    val SkillMod_ATTRIBUTES = RpgConstants.AttributeLists.SKILL_MOD_ATTRIBUTES

    val PlayerMod_ATTRIBUTES = RpgConstants.AttributeLists.PLAYER_MOD_ATTRIBUTES

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
