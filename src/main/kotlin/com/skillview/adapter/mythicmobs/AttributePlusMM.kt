package com.skillview.adapter.mythicmobs

import com.skillview.util.DamageUtil
import com.skillview.util.addEntityAttribute
import com.skillview.util.getattr
import com.skillview.util.round
import ink.ptms.um.event.MobSkillLoadEvent
import ink.ptms.um.skill.SkillMeta
import ink.ptms.um.skill.type.EntityTargetSkill
import ink.ptms.um.skill.SkillResult
import org.bukkit.entity.LivingEntity
import taboolib.common.platform.event.SubscribeEvent

private const val AttributesSource = "SkillAp"

@SubscribeEvent
fun onSkillLoad(event: MobSkillLoadEvent) {
    if (event.nameIs("damage_ap", "damage", "d")) {
        event.register(object : EntityTargetSkill {

            val attribute = event.config.getPlaceholderString(
                arrayOf("attribute", "attr"),
                ""
            )

            val damage = event.config.getPlaceholderDouble(
                arrayOf("damage", "d"),
                10.0
            )

            override fun cast(meta: SkillMeta, entity: org.bukkit.entity.Entity): SkillResult {
                if (entity !is LivingEntity) return SkillResult.ERROR

                val damager = meta.caster.entity as? LivingEntity
                    ?: return SkillResult.ERROR

                val attribute = attribute.get(meta.caster)
                val attributeValue = getattr(damager, attribute)
                val damageValue = damage[meta.caster]

                addEntityAttribute(damager, AttributesSource, attribute, damageValue)
                DamageUtil.damage(damager, entity, damageValue.round())

                return SkillResult.SUCCESS
            }
        })
    }
}