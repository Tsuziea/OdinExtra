package com.tsuziea.odinextra.features.impl.extra

import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.TerminalEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.M7Phases
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalUtils
import com.tsuziea.odinextra.utils.CustomCategory
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket

object AutoTerms : Module(
    name = "Auto Terms",
    description = "Automatically solves terminals.",
    category = CustomCategory.Extra
) {
    private val delay by NumberSetting("Delay", 200L, 100, 300, 50, "Delay between each click.", "ms")
    private val firstClickDelay by NumberSetting("First Click Delay", 300L, 200, 500, 50, "Delay before first click.", "ms")

    private var firstClick = true
    private var lastClickAt = 0L
    private var containerUpdated = false
    private var lastContainerUpdateAt = 0L

    init {
        on<TickEvent.Start> {
            with(TerminalUtils.currentTerm ?: return@on) {
                if (solution.isEmpty()) return@on

                val now = System.currentTimeMillis()
                if (firstClick && (now - lastClickAt < firstClickDelay)) return@on

                if (type == TerminalTypes.MELODY) {
                    click(solution.find { it % 9 == 7 } ?: return@on, 2, false)
                    firstClick = false
                    return@on
                }

                if (now - lastClickAt < delay) return@on
                if (!containerUpdated) return@on

                firstClick = false
                lastClickAt = now
                containerUpdated = false

                val slotIndex = solution.firstOrNull() ?: return@on

                when (type) {
                    TerminalTypes.RUBIX -> click(
                        slotIndex,
                        if (solution.count { it == slotIndex } >= 3) 1 else 2,
                        false)

                    else -> click(slotIndex, 2, false)
                }
            }
        }

        on<TerminalEvent.Open> {
            firstClick = true
            lastClickAt= System.currentTimeMillis()
        }

        onReceive<ClientboundOpenScreenPacket> {
            if (DungeonUtils.getF7Phase() != M7Phases.P3) return@onReceive
            containerUpdated = true
            lastContainerUpdateAt = System.currentTimeMillis()
        }
    }
}