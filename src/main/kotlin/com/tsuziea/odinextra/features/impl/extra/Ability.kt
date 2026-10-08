package com.tsuziea.odinextra.features.impl.extra

import com.odtheking.odin.events.MessageEvent
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.render.getStringWidth
import com.odtheking.odin.utils.render.text
import com.odtheking.odin.utils.skyblock.dungeon.DungeonClass
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.toFixed
import com.tsuziea.odinextra.features.CustomCategory
import com.tsuziea.odinextra.events.TickStart
import com.tsuziea.odinextra.utils.isHolding
import org.lwjgl.glfw.GLFW

object Ability : Module(
    name = "Abilities",
    description = "Uses your ability on left click terminator.",
    category = CustomCategory.Extra
) {
    private val hud by HUD("Cooldown", "Shows the cooldown for your dungeon ability.") { example ->

        val totalWidth = getStringWidth("Ability: 20s")

        if (example) {
            text("Ability: 20s", 0, 0)
            return@HUD totalWidth to 9
        }

        if (!started || !DungeonUtils.inDungeons || DungeonUtils.currentDungeonPlayer.clazz != DungeonClass.ARCHER)return@HUD 0 to 0

        val now = System.currentTimeMillis()
        val elapsed = (now - lastUsedAt)

        val cd = ((20000 - elapsed).coerceAtLeast(0L) / 1000.0).toFixed()

        if (elapsed > 20000) {
            text("Ability: §aReady", 0, 0)
        } else { text("Ability: §c${cd}s", 0, 0) }
        totalWidth to 9
    }

    private var started = false
    private var lastUsedAt = 0L

    private val MORT_REGEX = Regex("\\[NPC] Mort: Here, I found this map when I first entered the dungeon\\.|\\[NPC] Mort: Right-click the Orb for spells, and Left-click \\(or Drop\\) to use your Ultimate!")

    init {
        on<LevelEvent.Load>{
            started = false
            lastUsedAt = 0L
        }

        on<TickStart> {
            val now = System.currentTimeMillis()
            if (!DungeonUtils.inDungeons || !DungeonUtils.inClear || DungeonUtils.currentDungeonPlayer.clazz != DungeonClass.ARCHER) return@on
            if (!started || !isHolding("TERMINATOR") || mc.screen != null || now - lastUsedAt < 20000L) return@on

            if (GLFW.glfwGetMouseButton(mc.window.handle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS) mc.player?.drop(true)
        }

        on<MessageEvent.Chat> {
            if (message.matches(MORT_REGEX))  started = true
            if (message.contains("Used Explosive Shot!")) lastUsedAt = System.currentTimeMillis()
        }
    }
}
