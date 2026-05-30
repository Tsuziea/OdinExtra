package com.tsuziea.odinextra.features.impl.extra

import com.odtheking.odin.features.Module
import com.tsuziea.odinextra.features.CustomCategory
import net.minecraft.world.entity.decoration.ArmorStand

object HideDamage : Module(
    name = "Hide Damage",
    description = "Hides damage splash.",
    category = CustomCategory.Extra
) {
    private val damageMarkers = setOf('✧', '✯', '❂', '✪', '✦', '✰', '☄', '✫', '➶', '⬥')

    fun shouldHide(armorStand: ArmorStand): Boolean {
        if (!enabled) return false
        if (!armorStand.isInvisible || !armorStand.isCustomNameVisible || !armorStand.hasCustomName()) return false

        val customName = armorStand.customName?.string?.trim() ?: return false
        return isDamageText(customName)
    }

    private fun isDamageText(text: String): Boolean {
        val stripped = text.replace(",", "")
        if (stripped.isEmpty()) return false
        if (!stripped.any(Char::isDigit)) return false

        return stripped.all { it.isDigit() || it in damageMarkers }
    }
}
