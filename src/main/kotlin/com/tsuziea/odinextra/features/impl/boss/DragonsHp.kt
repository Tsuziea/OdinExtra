package com.tsuziea.odinextra.features.impl.boss

import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.render.drawText
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.M7Phases
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.util.Mth.lerp
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import java.util.Locale
import java.util.UUID

object DragonsHp : Module(
    name = "Dragons HP",
    description = "Smoother.") {
    private var dragons = emptySet<EnderDragon>()
    private val dragonHealthMap = mutableMapOf<UUID, Float>()

    init {
        on<PacketEvent.Receive> {
            if (!enabled) return@on
            if (!DungeonUtils.inBoss || DungeonUtils.getF7Phase() != M7Phases.P5) return@on

            val packet = packet as? ClientboundSetEntityDataPacket ?: return@on
            val entity = mc.level?.getEntity(packet.id) as? EnderDragon ?: return@on
            val dragonHealth = (packet.packedItems.find { it.id == 9 }?.value as? Float) ?: return@on

            dragonHealthMap[entity.uuid] = dragonHealth
        }

        on<TickEvent.End> {
            if (!enabled) return@on
            if (!DungeonUtils.inBoss || DungeonUtils.getF7Phase() != M7Phases.P5) {
                dragons = emptySet()
                dragonHealthMap.clear()
                return@on
            }

            dragons = mc.level
                ?.entitiesForRendering()
                ?.filterIsInstance<EnderDragon>()
                ?.filter { it.isAlive }
                ?.toSet()
                .orEmpty()

            val alive = dragons.map { it.uuid }.toSet()
            dragonHealthMap.keys.removeIf { it !in alive }
        }

        on<RenderEvent.Extract> {
            if (!enabled) return@on
            if (!DungeonUtils.inBoss || DungeonUtils.getF7Phase() != M7Phases.P5) return@on

            dragons.forEach { dragon ->
                val t = mc.gameRenderer.mainCamera.partialTickTime.toDouble()
                val pos = lerp(t, dragon.oldPosition(), dragon.position())

                val dist = mc.player?.distanceTo(dragon) ?: 0f
                val scale = (dist / 5f).coerceIn(4f, 6f)
                val hp = dragonHealthMap[dragon.uuid] ?: dragon.health
                drawText(colorHealth(hp), pos, scale, false)
            }
        }

        on<WorldEvent.Load> {
            dragons = emptySet()
            dragonHealthMap.clear()
        }
    }

    private fun colorHealth(hp: Float): String = when {
        hp >= 750_000_000f -> "§a${formatHealth(hp)}"
        hp >= 500_000_000f -> "§e${formatHealth(hp)}"
        hp >= 250_000_000f -> "§6${formatHealth(hp)}"
        else -> "§c${formatHealth(hp)}"
    }

    private fun formatHealth(hp: Float): String = when {
        hp >= 1_000_000_000f -> "${oneDecimal(hp / 1_000_000_000f)}b"
        hp >= 1_000_000f -> "${oneDecimal(hp / 1_000_000f)}m"
        hp >= 1_000f -> "${oneDecimal(hp / 1_000f)}k"
        else -> hp.toInt().toString()
    }

    private fun oneDecimal(value: Float): String = String.format(Locale.US, "%.1f", value)
}