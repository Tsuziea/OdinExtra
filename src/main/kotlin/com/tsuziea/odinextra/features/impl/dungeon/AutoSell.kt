package com.tsuziea.odinextra.features.impl.dungeon

import com.odtheking.odin.events.*
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.lore
import com.odtheking.odin.utils.noControlCodes
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.Items

object AutoSell : Module(
    name = "Auto Sell",
    description = "Insta sell all items.",
) {
    init {
        on<TickEvent.Start> {
            val player = mc.player ?: return@on
            val screen = mc.screen as? AbstractContainerScreen<*> ?: return@on
            val menu = screen.menu

            val hasSellSlot = listOf(31, 49).any { slotIndex ->
                val stack = menu.slots.getOrNull(slotIndex)?.item ?: return@any false

                val name = stack.hoverName.string.noControlCodes
                val isSell = stack.item == Items.HOPPER && name.contains("sell item", true)
                val isBuyback = stack.lore.lastOrNull()?.string?.noControlCodes?.contains("click to buyback", true) == true

                isSell || isBuyback
            }

            if (!hasSellSlot) return@on

            menu.slots.forEachIndexed { index, slot ->
                if (slot.container !is Inventory) return@forEachIndexed
                val name = slot.item.hoverName.string.noControlCodes

                if (blacklist.any { name.contains(it, true) }) return@forEachIndexed
                if (!sellList.any { name.contains(it, true) }) return@forEachIndexed

                mc.gameMode?.handleInventoryMouseClick(menu.containerId, index, 0, ClickType.CLONE, player)
                return@forEachIndexed
            }
        }
    }

    private val sellList = arrayOf(
        "enchanted ice", "superboom tnt", "rotten", "skeleton master", "skeleton grunt", "cutlass",
        "skeleton soldier", "zombie soldier", "zombie knight", "zombie commander", "skeletor",
        "super heavy", "heavy", "sniper helmet", "dreadlord", "earth shard", "zombie commander whip",
        "machine gun", "sniper bow", "soulstealer bow", "silent death", "training weight",
        "beating heart", "premium flesh", "mimic fragment", "enchanted rotten flesh", "sign",
        "enchanted bone", "defuse kit", "optical lens", "tripwire hook", "button", "carpet", "lever",
        "diamond atom", "healing viii splash potion", "candycomb", "revive stone", "vitamin death"
    )

    private val blacklist = listOf("skeleton master chestplate")
}