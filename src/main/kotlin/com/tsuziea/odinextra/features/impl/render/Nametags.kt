package com.tsuziea.odinextra.features.impl.render

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.render.drawText
import com.odtheking.odin.utils.skyblock.dungeon.DungeonClass
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.util.Mth.lerp
import net.minecraft.world.entity.player.Player

object Nametags : Module(
    name = "Nametags",
    description = "Render player names through walls."
) {
    private val showDistance by BooleanSetting("Distance", true, desc = "Show distance in meters.")
    private val showClass by BooleanSetting("Class", true, desc = "Show class in dungeons.")
    private var players = emptySet<Player>()
    private var classByPlayerName = emptyMap<String, DungeonClass>()

    init {
        on<WorldEvent.Load> {
            players = emptySet()
            classByPlayerName = emptyMap()
        }

        on<RenderEvent.Extract> {
            players = mc.level?.players()?.filter { player ->
                player != mc.player && player.uuid.version() != 2 && player.isAlive
            }?.toSet().orEmpty()

            classByPlayerName = if (showClass && DungeonUtils.inDungeons) {
                DungeonUtils.dungeonTeammates.associate { teammate -> teammate.name.lowercase() to teammate.clazz }
            } else emptyMap()

            players.forEach { player ->
                if (player.isInvisible) return@forEach
                val name = player.displayName.string
                val t = mc.gameRenderer.mainCamera.partialTickTime.toDouble()

                val dist = mc.player?.distanceTo(player) ?: 0f
                if (dist <= 10) return@forEach

                val distanceText = if (showDistance) "${dist.toInt()}m" else ""
                val text = "§f${getClassTag(player)}§r$name${if (distanceText.isNotEmpty()) " §7$distanceText" else ""}§r"
                val pos = lerp(t, player.oldPosition(), player.position()).add(0.0, 0.5, 0.0)
                val scale = (dist / 12.0f).coerceIn(1.0f, 4.0f)

                drawText(text, pos, scale, false)
            }
        }
    }

    private fun getClassTag(player: Player): String {
        if (!showClass || !DungeonUtils.inDungeons) return ""
        val clazz = classByPlayerName[player.name.string.lowercase()] ?: DungeonClass.Unknown
        return when (clazz) {
            DungeonClass.Healer -> "[H] "
            DungeonClass.Mage -> "[M] "
            DungeonClass.Berserk -> "[B] "
            DungeonClass.Archer -> "[A] "
            DungeonClass.Tank -> "[T] "
            DungeonClass.Unknown -> ""
        }
    }
}