package com.tsuziea.odinextra.features.impl.extra

import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.skyblock.SplitsManager.currentSplits
import com.odtheking.odin.utils.skyblock.SplitsManager.getAndUpdateSplitsTimes
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.M7Phases
import com.odtheking.odin.utils.toFixed
import com.tsuziea.odinextra.features.CustomCategory

object StormTime : Module(
    name = "Storm Time",
    description = "Tells when storm stuck/enrage in tick time.",
    category = CustomCategory.Extra
) {
    private var stormStuckAt: Long? = null

    init {
        on<WorldEvent.Load> {
            stormStuckAt = null
        }

        on<ChatPacketEvent> {
            if (DungeonUtils.getF7Phase() != M7Phases.P2) return@on

            if (value.contains("[BOSS] Storm: Ouch, that hurt!") || value.contains("[BOSS] Storm: Oof")) {
                val stormTimerTicks = getStormSplitTimer() ?: return@on

                modMessage("§8[§bStormTime§8] §rStorm stuck at §a${(stormTimerTicks / 20.0).toFixed()}s§r")
                stormStuckAt = stormTimerTicks
                return@on
            }

            if (value.contains("Storm is enraged")) {
                val stuckAtTicks = stormStuckAt ?: return@on
                val stormTimerTicks = getStormSplitTimer() ?: return@on

                modMessage("§8[§bStormTime§8] §rStorm enraged at §a${(stormTimerTicks / 20.0).toFixed()}s§r §7(${((stormTimerTicks - stuckAtTicks) / 20.0).toFixed()}s)§r")
                stormStuckAt = null
                return@on
            }
        }
    }

    private fun getStormSplitTimer(): Long? {
        val (_, tickTimes, _) = getAndUpdateSplitsTimes(currentSplits)
        val stormIndex = currentSplits.splits.indexOfFirst { it.name.contains("Storm") }
        if (stormIndex < 0) return null

        return tickTimes.getOrNull(stormIndex)
    }
}
