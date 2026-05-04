package com.tsuziea.odinextra.features.impl.boss

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.ScreenEvent
import com.odtheking.odin.events.TerminalEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.M7Phases
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalUtils.currentTerm
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import java.util.ArrayDeque

object AutoTerms : Module (
    name = "Auto Terms",
    description = "Automatically solves terminals."
) {
    private val delay by NumberSetting("Delay", 10L, 0, 50, 10, unit = "ms", desc = "Delay after container updates.")
    private val clickDelay by NumberSetting("Click Delay", 200L, 200, 300, 10, unit = "ms", desc = "Delay between clicks.")
    private val firstClickDelay by NumberSetting("First Click Delay", 350L, 250, 500, 10, unit = "ms", desc = "Delay before first click.")
    private val skipMelody by BooleanSetting("Skip Melody", true, desc = "Skipping delay checks for Melody.")
    private var lastClickTime = 0L
    private var firstClick = true
    private var containerUpdated = false
    private var lastContainerUpdate = 0L
    private val queue = ArrayDeque<Int>()
    private var queued = false

    init {
        on<ScreenEvent.Render> {
            with(currentTerm ?: return@on) {
                if (solution.isEmpty()) return@on

                val now = System.currentTimeMillis()
                if (firstClick && (now - lastClickTime < firstClickDelay)) return@on

                if (skipMelody && (type == TerminalTypes.MELODY)) {
                    click(solution.find { it % 9 == 7 } ?: return@on, 2, false)
                    return@on
                }

                if (!queued) queueClicks()

                if (type != TerminalTypes.MELODY && !containerUpdated) return@on
                if (now - lastClickTime < clickDelay) return@on
                if (now - lastContainerUpdate < delay) return@on

                firstClick = false
                lastClickTime = now
                containerUpdated = false

                val slotIndex = solution.firstOrNull() ?: return@on
                val queueIndex = queue.pollFirst() ?: return@on

                when (type) {
                    TerminalTypes.MELODY -> click(solution.find { it % 9 == 7 } ?: return@on, 2, false)
                    TerminalTypes.RUBIX -> click(
                        queueIndex,
                        if (solution.count { it == slotIndex } >= 3) 1 else 2,
                        false)

                    else -> click(queueIndex, 2, false)
                }
            }
        }

        on<TerminalEvent.Open> {
            lastClickTime = System.currentTimeMillis()
            firstClick = true
            queued = false
        }

        onReceive<ClientboundOpenScreenPacket> {
            if (DungeonUtils.getF7Phase() != M7Phases.P3) return@onReceive
            containerUpdated = true
            lastContainerUpdate = System.currentTimeMillis()
        }
    }

    private fun queueClicks() {
        val term = currentTerm ?: return
        queue.clear()

        val humanClickOrder = when (term.type) {
            TerminalTypes.PANES,
            TerminalTypes.STARTS_WITH,
            TerminalTypes.SELECT -> {
                term.solution.sortedWith(
                    compareBy<Int> { it / 9 }.thenBy { slot ->
                        val row = slot / 9
                        val col = slot % 9
                        if (row == 2 || row == 4) -col else col
                    }
                )
            }

            TerminalTypes.RUBIX -> {
                term.solution.groupingBy { it }.eachCount().flatMap { (v, c) ->
                    when (c) {
                        3 -> listOf(v, v)
                        4 -> listOf(v)
                        else -> List(c) { v }
                    }
                }
            }

            else -> term.solution
        }

        queue.addAll(humanClickOrder)
        queued = true
    }
}