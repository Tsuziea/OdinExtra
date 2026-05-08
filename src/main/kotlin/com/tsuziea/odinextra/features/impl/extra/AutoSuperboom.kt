package com.tsuziea.odinextra.features.impl.extra

import com.mojang.blaze3d.platform.InputConstants
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.InputEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.itemId
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.tsuziea.odinextra.utils.CustomCategory
import com.tsuziea.odinextra.utils.leftClick
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.BlockHitResult

object AutoSuperboom : Module(
    name = "Auto superboom",
    description = "Automatically swaps to superboom when you click a breakable wall!",
    category = CustomCategory.Extra
) {
    private val delay by NumberSetting("Delay", 50, 50, 200, 50, "Delay between each action.", "ms")

    private var lastAction = 0L
    private var originalSlot = -1
    private var swapPhase = 0
    private var superboomSlot = -1

    private val superboomItemIds = setOf("SUPERBOOM_TNT", "INFINITE_SUPERBOOM_TNT")

    init {
        on<InputEvent>{
            if (originalSlot != -1) return@on
            if (mc.screen != null || !DungeonUtils.inClear) return@on

            val player = mc.player ?: return@on
            val hit = mc.hitResult as? BlockHitResult ?: return@on
            val hitBlock = hit.blockPos
            val state = mc.level?.getBlockState(hitBlock) ?: return@on

            if (key.value == InputConstants.MOUSE_BUTTON_LEFT && state.block == Blocks.CRACKED_STONE_BRICKS) {
                val selectedSlot = player.inventory.selectedSlot

                for (i in 0..8) {
                    val item = player.inventory.getItem(i)

                    if (item.itemId in superboomItemIds) {
                        superboomSlot = i
                        originalSlot = selectedSlot
                        break
                    }
                }
                cancel()
            }
        }

        on<TickEvent.Start> {
            val player = mc.player ?: return@on
            if (superboomSlot == -1) return@on

            val now = System.currentTimeMillis()
            if (now - lastAction < delay) return@on

            when (swapPhase) {
                0 -> {
                    if (originalSlot == superboomSlot) {
                        swapPhase = 1
                        lastAction = now
                        return@on
                    }

                    player.inventory.selectedSlot = superboomSlot

                    swapPhase = 1
                    lastAction = now
                }

                1 -> {
                    leftClick()

                    swapPhase = 2
                    lastAction = now
                }

                2 -> {
                    player.inventory.selectedSlot = originalSlot

                    originalSlot = -1
                    superboomSlot = -1
                    swapPhase = 0
                    lastAction = now
                }
            }
        }
    }
}