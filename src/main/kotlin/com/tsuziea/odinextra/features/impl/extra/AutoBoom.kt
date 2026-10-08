package com.tsuziea.odinextra.features.impl.extra

import com.mojang.blaze3d.platform.InputConstants
import com.odtheking.odin.events.InputEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.itemId
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.tsuziea.odinextra.features.CustomCategory
import com.tsuziea.odinextra.events.TickStart
import com.tsuziea.odinextra.utils.isHolding
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.BlockHitResult

object AutoBoom : Module(
    name = "Auto Boom",
    description = "Swaps to superboom when you click a breakable wall.",
    category = CustomCategory.Extra
) {
    private var tntSlot = -1

    private val tntIds = setOf("SUPERBOOM_TNT", "INFINITE_SUPERBOOM_TNT")
    private val blacklistIds = setOf("DUNGEONBREAKER", "TERMINATOR", "DARK_CLAYMORE")

    init {
        on<InputEvent> {
            val player = mc.player ?: return@on
            if (mc.screen != null || key.value != InputConstants.MOUSE_BUTTON_LEFT || !DungeonUtils.inClear || isHolding(tntIds + blacklistIds)) return@on

            val hit = mc.hitResult as? BlockHitResult ?: return@on
            val hitBlock = hit.blockPos
            val state = mc.level?.getBlockState(hitBlock) ?: return@on
            if (state.block != Blocks.CRACKED_STONE_BRICKS) return@on

            tntSlot = (0..8).firstOrNull { i ->
                player.inventory.getItem(i).itemId in tntIds
            } ?: -1
        }

        on<TickStart> {
            val player = mc.player ?: return@on
            if (tntSlot == -1) return@on

            player.inventory.selectedSlot = tntSlot
            tntSlot = -1
        }
    }
}
