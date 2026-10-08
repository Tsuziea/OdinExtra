package com.tsuziea.odinextra.features.impl.extra

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.StringSetting
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.render.drawTracer
import com.tsuziea.odinextra.features.CustomCategory
import net.minecraft.world.entity.player.Player

object PlayerTracker : Module(
    name = "Player Tracker",
    description = "Tracks a target player through walls.",
    category = CustomCategory.Extra
) {
    private val trackerEnabled by BooleanSetting("Enabled", true, desc = "Shows tracker overlays.")
    private val targetTracker by BooleanSetting("Target Tracker", true, desc = "Highlights the selected player.")
    private val targetName by StringSetting("Target", "Minikloon", desc = "Name of the target.").withDependency { targetTracker }
    private val color by ColorSetting("Color", Colors.MINECRAFT_AQUA, true, "Color of the outline.").withDependency { targetTracker }

    private var players = emptySet<Player>()

    init {
        on<LevelEvent.Load> {
            players = emptySet()
        }

        on<TickEvent.End> {
            players = mc.level?.players()?.filter { player ->
                player.isAlive && player != mc.player && player.uuid.version() != 2
            }?.toSet().orEmpty()
        }

        on<RenderEvent.Extract> {
            if (!trackerEnabled) return@on

            players.forEach { player ->
                if (targetTracker && player.name.string == targetName) {
                    drawTracer(player.position(), Colors.MINECRAFT_AQUA, false)
                    drawStyledBox(player.boundingBox, color, 2, false)
                }
            }
        }
    }
}
