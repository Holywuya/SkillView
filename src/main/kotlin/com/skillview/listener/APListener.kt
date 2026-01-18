package com.skillview.listener

/**
object APListener {

    // 统一定义 AP 属性来源
    private const val ATTR_SOURCE = "SkillView_MOD"

    @Config("config.yml")
    lateinit var conf: Configuration
    @SubscribeEvent
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player
        submit(delay = 100L) {
            if (player.isOnline) {
                syncModStatsToAP(player)
            }
        }
    }

    fun syncModStatsToAP(player: Player) {
        recalculate(player)
        // 1. 获取 MOD 数据
        val modStats = ModRuntime.getStats(player)

        // 2. 获取 AP 数据对象
        val attrData = AttributeAPI.getAttrData(player)

        // 3. 【修改处】构建 List<String> 而不是 Map
        // AP 的标准格式通常是: "属性名: 数值" (中间有冒号和空格)
        val attributeList = ArrayList<String>()

        // --- 基础生存与消耗 ---
        if (modStats.extraMana != 0.0)   attributeList.add("魔力上限:${modStats.extraMana}")
        if (modStats.maxHp != 0.0)       attributeList.add("生命力:${modStats.maxHp}")
        if (modStats.efficiency != 0.0)  attributeList.add("技能效率:${modStats.efficiency}")
        if (modStats.cdReduction != 0.0) attributeList.add("冷却缩减:${modStats.cdReduction}")

        // --- 伤害与战斗加成 ---
        if (modStats.damageBonus != 0.0) attributeList.add("伤害加成:${modStats.damageBonus}")
        if (modStats.damageMore != 0.0)  attributeList.add("最终伤害:${modStats.damageMore}")
        if (modStats.extraRange != 0.0)  attributeList.add("额外范围:${modStats.extraRange}")

        // --- 暴击系统 ---
        if (modStats.critRate != 0.0)    attributeList.add("暴击几率:${modStats.critRate}")
        if (modStats.critDamage != 0.0)  attributeList.add("暴伤倍率:${modStats.critDamage}")

        // 4. 调用 AP 接口
        // 如果 List 为空，AP 同样会清理该 Source 的属性
        AttributeAPI.addPersistentSourceAttribute(
            attrData,
            ATTR_SOURCE,
            attributeList, // 这里传入 List<String>
            -1.0           // 【修改处】这里传入 Double 类型 (-1.0)
        )

        if (conf.getBoolean("DEBUG")) {
            println("[Debug] 已同步玩家 ${player.name} 的 MOD 属性至 AP (共 ${attributeList.size} 条词条)")
        }
    }
}
**/