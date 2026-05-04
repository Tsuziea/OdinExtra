package com.tsuziea.odinextra.features.impl.skyblock

import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.noControlCodes
import com.odtheking.odin.utils.skyblock.LocationUtils
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.BlockItem
import net.minecraft.world.level.block.Blocks

object AutoHarp : Module(
    name = "Auto Harp",
    description = "Automatically plays Melody's Harp."
) {
    private val targetSlots = IntArray(7) { 37 + it }
    private val lastQuartzState = BooleanArray(7)

    init {
        on<TickEvent.Start> {
            if (!LocationUtils.isInSkyblock) return@on

            val screen = mc.screen as? AbstractContainerScreen<*>?: return@on
            if (!screen.title.string.noControlCodes.contains("Harp -")) {
                lastQuartzState.fill(false)
                return@on
            }

            val slots = screen.menu.slots
            val player = mc.player ?: return@on

            targetSlots.forEachIndexed{i, slotIndex ->
                val block = slots[slotIndex].item.item as? BlockItem ?: return@forEachIndexed
                val isQuartz = block.block == Blocks.QUARTZ_BLOCK

                if (isQuartz && !lastQuartzState[i]) mc.gameMode?.handleInventoryMouseClick(screen.menu.containerId, slotIndex, 0, ClickType.CLONE, player)
                lastQuartzState[i] = isQuartz
            }
        }
    }
}
