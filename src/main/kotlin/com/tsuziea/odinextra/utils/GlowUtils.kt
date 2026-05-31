package com.tsuziea.odinextra.utils

import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import net.minecraft.world.entity.Entity
import java.util.UUID

object GlowUtils {
    private data class GlowEntry(val color: Int, val expiresAt: Long)

    private val entityGlowColors = mutableMapOf<UUID, GlowEntry>()

    init {
        on<WorldEvent.Load> {
            entityGlowColors.clear()
        }

        on<TickEvent.End> {
            cleanupExpired()
        }
    }

    fun Entity.setGlow(durationMs: Long, color: Color = Colors.WHITE) {
        entityGlowColors[uuid] = GlowEntry(color.rgba, System.currentTimeMillis() + durationMs)
    }

    fun Entity.clearGlow() {
        entityGlowColors.remove(uuid)
    }

    fun getGlowColor(entity: Entity): Int? {
        val entry = entityGlowColors[entity.uuid] ?: return null
        val now = System.currentTimeMillis()
        if (now >= entry.expiresAt) {
            entityGlowColors.remove(entity.uuid)
            return null
        }

        return entry.color
    }

    fun cleanupExpired() {
        val now = System.currentTimeMillis()
        entityGlowColors.entries.removeIf { it.value.expiresAt <= now }
    }
}
