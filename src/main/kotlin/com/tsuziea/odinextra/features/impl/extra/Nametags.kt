package com.tsuziea.odinextra.features.impl.extra

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.StringSetting
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.render.drawText
import com.tsuziea.odinextra.features.CustomCategory
import net.minecraft.util.Mth.lerp
import net.minecraft.world.entity.player.Player

object Nametags : Module(
    name = "Nametags",
    description = "Renders player names through blocks.",
    category = CustomCategory.Extra
) {
    private const val NAME_RENDER_DISTANCE = 10.0

    private val playerTracker by BooleanSetting("Player Tracker", true, desc = "Draws bounding box for the selected player.")
    private val targetName by StringSetting("Target", "Minikloon", desc = "Name of the target.").withDependency { playerTracker }
    private val color by ColorSetting("Color", Colors.MINECRAFT_AQUA, true, "Color of the outline.").withDependency { playerTracker }

    private var players = emptySet<Player>()

    init {
        on<WorldEvent.Load> {
            players = emptySet()
        }

        on<TickEvent.End> {
            players = mc.level?.players()?.filter { player ->
                player.isAlive && player != mc.player && player.uuid.version() != 2
            }?.toSet().orEmpty()
        }

        on<RenderEvent.Extract> {
            val t = mc.gameRenderer.mainCamera.partialTickTime.toDouble()

            players.forEach { player ->
                val dist = mc.player?.distanceTo(player) ?: 0f

                if (dist > NAME_RENDER_DISTANCE) {
                    val pos = lerp(t, player.oldPosition(), player.position()).add(0.0, player.bbHeight.toDouble() + 0.5, 0.0)
                    val scale = (dist / 12.0f).coerceIn(1.0f, 4.0f)

                    drawText(player.displayName.string, pos, scale, false)
                }

                if (playerTracker && player.name.string == targetName) drawStyledBox(player.boundingBox, color, 2, false)
            }
        }
    }
}


