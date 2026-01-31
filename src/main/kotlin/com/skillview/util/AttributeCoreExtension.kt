package com.skillview.util

import com.attributecore.api.AttributeCoreAPI
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity

object AttributeCoreHelper {
    
    private val isAttributeCoreLoaded: Boolean by lazy {
        Bukkit.getPluginManager().getPlugin("AttributeCore") != null
    }
    
    fun getAttackDamage(entity: LivingEntity): Double {
        return if (isAttributeCoreLoaded) {
            try {
                AttributeCoreAPI.getAttribute(entity, "attack_damage")
            } catch (e: Exception) {
                100.0
            }
        } else {
            100.0
        }
    }
}
