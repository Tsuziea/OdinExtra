package com.tsuziea.odinextra.utils

import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import net.minecraft.world.entity.Entity

object GlowUtils {
    private val entityGlowColors = mutableMapOf<Int, Int>()

    fun Entity.setGlow(enabled: Boolean, color: Color = Colors.WHITE) {
        if (enabled) {
            entityGlowColors[this.id] = color.rgba
        } else {
            entityGlowColors.remove(this.id)
        }
    }

    fun getGlowColor(entity: Entity): Int? = entityGlowColors[entity.id]
}