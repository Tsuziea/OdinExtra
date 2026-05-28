package com.tsuziea.odinextra.features.impl.extra

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.noControlCodes
import com.odtheking.odin.utils.render.drawTracer
import com.odtheking.odin.utils.render.getStringWidth
import com.odtheking.odin.utils.render.text
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.toFixed
import com.tsuziea.odinextra.utils.CustomCategory
import net.minecraft.world.entity.decoration.ArmorStand

object TrevorHelper : Module(
    name = "Trevor Helper",
    description = "Helps with trevors's quest",
    category = CustomCategory.Extra
) {
    private val hud by HUD("Cooldown", "Shows the cooldown for trevor quest.") { example ->

        val totalWidth = getStringWidth("Trevor Cooldown: 20s")

        if (example) {
            text("Trevor Cooldown: 20s", 0, 0)
            return@HUD totalWidth to 9
        }

        val now = System.currentTimeMillis()
        val elapsed = (now - lastAcceptAt)
        if (elapsed > 20000) return@HUD 0 to 0

        val cd = ((20000 - elapsed).coerceAtLeast(0L) / 1000.0).toFixed()

        text("Trevor Cooldown: ${cd}s", 0, 0)
        totalWidth to 9
    }
    private val drawTracer by BooleanSetting("Draw Tracer", true, "Draws a tracer to pelt animal.")
    private val autoAccept by BooleanSetting("Auto Accept", false, "Auto runs /call trevor and accepts his quest on kill. Requires cookie buff, abiphone and Trevor's contact")

    private var killed = false
    private var received = false
    private var lastAcceptAt = 0L
    private var lastYesChatprompt: String? = null

    private val tiers = listOf("Trackable", "Untrackable", "Undetected", "Endangered", "Elusive")
    private val yesChatpromptRegex = Regex("""command=/?(chatprompt\s+\S+\s+YES)\b""", RegexOption.IGNORE_CASE)

    init {
        on<WorldEvent.Load> {
            killed = false
            received = false
            lastAcceptAt = 0L
            lastYesChatprompt = null
        }

        on<TickEvent.Start> {
            val now = System.currentTimeMillis()
            if (now - lastAcceptAt < 20000) return@on

            if (killed) {
                sendCommand("call trevor")
                killed = false
            }

            if (received) {
                sendCommand(lastYesChatprompt ?: return@on)
                received = false
                lastAcceptAt = now
            }
        }

        on<ChatPacketEvent> {
            if (!autoAccept) return@on
            val message = value.noControlCodes

            if (message.contains("Killing the animal rewarded you")) killed = true

            if (message.contains("[YES]") && message.contains("[NO]")) {
                val raw = component.toString()
                val cmd = yesChatpromptRegex.find(raw)?.groupValues?.get(1)

                if (cmd != null) {
                    lastYesChatprompt = cmd
                    received = true
                }
            }
        }

        on<RenderEvent.Extract> {
            if (!drawTracer) return@on
            val level = mc.level ?: return@on

            level.entitiesForRendering().forEach { entity ->
                val stand = entity as? ArmorStand ?: return@forEach
                if (!stand.isAlive) return@forEach

                val name = stand.name.string
                if (!tiers.any { tier -> name.contains(tier) }) return@forEach

                drawTracer(stand.position().add(0.0, -1.0, 0.0), Colors.WHITE, false, 2f)
            }
        }
    }
}
