package com.tsuziea.odinextra.features.impl.extra

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.fillItemFromSack
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.itemId
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.skyblock.KuudraUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.tiles.RoomType
import com.tsuziea.odinextra.utils.CustomCategory

object AutoGFS : Module(
    name = "Auto GFS",
    description = "Automatically refills certain items from your sacks.",
    category = CustomCategory.Extra
) {
    private val inKuudra by BooleanSetting("In Kuudra", true, desc = "Only gfs in Kuudra.")
    private val inDungeon by BooleanSetting("In Dungeon", true, desc = "Only gfs in Dungeons.")
    private val refillOnDungeonStart by BooleanSetting(
        "Refill on Dungeon Start",
        true,
        desc = "Refill when a dungeon starts."
    )
    private val refillLeap by BooleanSetting("Refill Leaps", false, desc = "Refill spirit leaps.")
    private val refillPearl by BooleanSetting("Refill Pearl", false, desc = "Refill ender pearls.")
    private val refillJerry by BooleanSetting("Refill Jerry", false, desc = "Refill inflatable jerrys.")
    private val refillTNT by BooleanSetting("Refill TNT", false, desc = "Refill superboom tnt.")
    private val refillOnTimer by BooleanSetting("Refill on Timer", true, desc = "Refills on a timed interval.")
    private val interval by NumberSetting(
        "Interval",
        5L,
        1,
        60,
        desc = "The interval in which to refill.",
        unit = "s"
    ).withDependency { refillOnTimer }
    private val autoDraft by BooleanSetting(
        "Auto Draft",
        true,
        desc = "Automatically get draft from the sack when puzzle failed."
    )

    private val puzzleFailRegex = Regex("^PUZZLE FAIL! (\\w{1,16}) .+$|^\\[STATUE] Oruo the Omniscient: (\\w{1,16}) chose the wrong answer! I shall never forget this moment of misrememberance\\.$")
    private val dungeonStartRegex = Regex("\\[NPC] Mort: Here, I found this map when I first entered the dungeon\\.|\\[NPC] Mort: Right-click the Orb for spells, and Left-click \\(or Drop\\) to use your Ultimate!")

    private var last = 0

    init {
        on<TickEvent.Start> {
            if (refillOnTimer) return@on
            if (++last < interval * 20) return@on

            last = 0
            refill()
        }

        on<ChatPacketEvent> {
            if (dungeonStartRegex.containsMatchIn(value)) {
                if (refillOnDungeonStart) refill()
            }

            if (puzzleFailRegex.containsMatchIn(value)) {
                if (!autoDraft || DungeonUtils.currentRoom?.data?.type != RoomType.PUZZLE) return@on
                schedule(30) {
                    modMessage("§7Fetching Draft from sack...")
                    sendCommand("gfs architect's first draft 1")
                }
            }
        }
    }

    private fun refill() {
        val inventory = mc.player?.inventory?.nonEquipmentItems ?: return
        if (
            inventory.any { it?.itemId == "HAUNT" } ||
            mc.screen != null ||
            !(inKuudra && KuudraUtils.inKuudra) && !(inDungeon && DungeonUtils.inDungeons)
        ) return

        if (refillLeap && inventory.any { it?.itemId == "SPIRIT_LEAP" }) {
            fillItemFromSack(16, "SPIRIT_LEAP", "spirit_leap", false)
        }
        if (refillPearl && inventory.any { it?.itemId == "ENDER_PEARL" }) {
            fillItemFromSack(16, "ENDER_PEARL", "ender_pearl", false)
        }
        if (refillJerry && inventory.any { it?.itemId == "INFLATABLE_JERRY" }) {
            fillItemFromSack(64, "INFLATABLE_JERRY", "inflatable_jerry", false)
        }
        if (refillTNT && inventory.any { it?.itemId == "SUPERBOOM_TNT" }) {
            fillItemFromSack(64, "SUPERBOOM_TNT", "superboom_tnt", false)
        }
    }
}