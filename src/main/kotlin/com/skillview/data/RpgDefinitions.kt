package com.skillview.data

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
        val DEBUG by lazy { con.getBoolean("DEBUG") }
    }

    // ==========================================
    //          NBT 路径常量（分层管理）
    // ==========================================

    /**
     * 技能书专属 NBT 常量
     */
    object SkillBookNBT {
        const val TYPE = "类型"
        const val RARITY = "品质"
        const val ROOT_BASIC = "技能书基础属性"               // 技能书基础节点
        const val ROOT_MODIFIER = "技能书属性强化"            // 强化/升级后属性节点（Mod属性叠加）
        const val ROOT_MOD = "Mod属性"                       // 全局Mod属性根节点（与角色Mod共享）

        const val SKILL_ID = "$ROOT_BASIC.技能id"            // 技能ID（如 "fireball"）
        const val LEVEL = "$ROOT_BASIC.等级"                 // 当前等级（升级时写入）

    }

    /**
     * MOD（角色Mod & 技能Mod/强化石）专属 NBT 常量
     */
    object ModNBT {
        const val TYPE = "类型"                              // "角色Mod" 或 "技能Mod"
        const val RARITY = "品质"                            // "普通"、"精良" 等
        const val LEVEL = "Mod.等级"                         // 当前强化等级
        const val MOD_ID = "Mod.id"

        const val ROOT_MOD = "Mod属性"                       // 角色Mod属性根节点
        const val COST = "$ROOT_MOD.消耗"                    // 容量消耗
        const val POLARITY = "$ROOT_MOD.极性"                // 极性（如 "V"、"D"）

        // 强化石专属：用于镶嵌到技能书的属性节点
        const val ROOT_STONE = "强化石属性强化"
        const val STONE_DAMAGE_MORE = "$ROOT_STONE.最终伤害"
        const val STONE_EXTRA_RANGE = "$ROOT_STONE.额外范围"
        const val STONE_COOLDOWN = "$ROOT_STONE.冷却缩减"
        const val STONE_MANA_MAX = "$ROOT_STONE.魔力上限"
        const val STONE_DAMAGE_BONUS = "$ROOT_STONE.伤害加成"
        const val STONE_EFFICIENCY = "$ROOT_STONE.技能效率"
        const val STONE_MULTIPLIER = "$ROOT_STONE.技能倍率"
        const val STONE_MANA_REDUCE = "$ROOT_STONE.魔力减耗"
    }

    // ==========================================
    //          属性白名单（用于强化逻辑）
    // ==========================================

    /**
     * 可强化属性白名单
     * 用于强化石镶嵌/拆解、ModRuntime 统计等
     */
    val UPGRADEABLE_ATTRIBUTES = listOf(
        "最终伤害",
        "额外范围",
        "冷却缩减",
        "魔力上限",
        "伤害加成",
        "技能效率",
        "技能倍率",
        "魔力减耗",
        "魔力恢复"
        // 如需扩展，直接在此添加
        // "暴击几率",
        // "破甲攻击"
    )

    /**
     * 角色Mod 提供的全局属性列表
     * 用于 ModRuntime.recalculate 时遍历统计
     */
    val MOD_GLOBAL_ATTRIBUTES = listOf(
        // 伤害类
        "伤害加成",
        "最终伤害",
        "暴击几率",
        "暴伤倍率",
        "技能强度",
        // 生存类
        "生命力",
        // 功能类
        "额外范围",
        "冷却缩减",
        "魔力恢复",
        "魔力上限",
        "技能效率"
    )

    // ==========================================
    //          AttributePlus 属性名常量
    // ==========================================

    /**
     * AttributePlus 注册的自定义属性名
     * 统一在此管理，避免字符串散落
     */
    object Attributes {
        const val COOLDOWN = "冷却缩减"
        const val EFFICIENCY = "技能效率"
        const val MANA_MAX = "魔力上限"
        const val MANA_REGEN = "魔力恢复"
        const val DAMAGE_BONUS = "伤害加成"
        const val DAMAGE_MORE = "最终伤害"
        const val EXTRA_RANGE = "额外范围"
        const val SKILL_POWER = "技能强度"
        const val CRIT_RATE = "暴击几率"
        const val CRIT_DAMAGE = "暴伤倍率"
        const val MAX_HP = "生命力"
    }
}

// ==========================================
//           数据类定义 (Data Classes)
// ==========================================

/**
 * 技能配置结构 (对应 skills.yml)
 */
data class SkillSetting(
    val mmSkill: String,        // MythicMobs 里的技能名
    val cooldown: Double,       // 基础冷却
    val mana: Int,              // 基础耗蓝
    val baseMultiplier: Double, // 基础倍率
    val maxLevel: Int,          // 最大等级
    val reduceMana: Double,     // 升级减少魔力消耗
    val reduceCd: Double,       // 升级减少冷却时间
    val multiplierUp: Double,   // 升级提升技能倍率

    // --- 新增内容 ---
    val enhanceMultiplier: Double, // 每次“强化/镶嵌”额外增加的倍率
    val tags: List<String>         // 技能标签 (例如: ["伤害", "范围", "火"])
)

/**
 * 职业定义 (对应 classes.yml)
 */
data class RpgClass(
    val baseHealth: Double,
    val baseMana: Double,
    val manaRegen: Double       // 每秒回蓝
)

/**
 * MOD/灵石的配置数据类
 */
data class ModSetting(
    val rarity: String,         // 稀有度
    val polarity: String,       // 极性 (V, D, -, =)
    val baseDrain: Int,         // 基础消耗
    val drainStep: Int,         // 等级成长消耗
    val maxLevel: Int,          // 最大等级
    val attributes: Map<String, Double>, // 属性增量表 (属性名 to 每级增量)
    val tags: List<String>      // 标签
)