package com.tsuziea.odinextra.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.*
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.noControlCodes
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.ClickType

object AutoSell : Module(
    name = "Auto Sell",
    description = "Automatically sell items in trades and cookie menus.",
) {
    private val delay by NumberSetting("Delay", 6, 2, 10, 1, desc = "The delay between each click.", unit = " ticks")
    private val randomization by NumberSetting("Randomization", 1, 0, 5, 1, desc = "Random delay variance", unit = " ticks")

    private var last = 0L
    private var next = 0L

    init {
        on<TickEvent.Start> {
            val player = mc.player ?: return@on
            val screen = mc.screen as? AbstractContainerScreen<*> ?: return@on
            if (!screen.title.string.noControlCodes.equalsOneOf("Trades", "Booster Cookie", "Farm Merchant", "Ophelia")) return@on

            val now = System.currentTimeMillis()
            if (now - last < next) return@on

            screen.menu.slots.forEachIndexed { index, slot ->
                val name = slot.item.hoverName.string.noControlCodes.lowercase()

                if (slot.item.isEmpty || blacklist.any { name.contains(it, true) }) return@forEachIndexed
                if (!sellList.any { name.contains(it, true) }) return@forEachIndexed

                last = now
                next = ((delay + (0..randomization).random()) * 50).toLong()

                mc.gameMode?.handleInventoryMouseClick(screen.menu.containerId, index, 0, ClickType.CLONE, player)
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