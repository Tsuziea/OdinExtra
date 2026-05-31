package com.tsuziea.odinextra.features.impl.extra

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.alert
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.sendChatMessage
import com.odtheking.odin.utils.skyblock.SplitsManager.currentSplits
import com.odtheking.odin.utils.skyblock.SplitsManager.getAndUpdateSplitsTimes
import com.odtheking.odin.utils.toFixed
import com.tsuziea.odinextra.features.CustomCategory

object Notify : Module(
    name = "Notify",
    description = "Tells when storm stuck/enrage in tick time.",
    category = CustomCategory.Extra
) {
    private val stormEnrage by BooleanSetting("Storm Enrage", true, desc = "Pings storm enraged time.")
    private val rag by BooleanSetting("Ragnarock", true, desc = "Pings when you should use rag.")
    private var stormStuckAt: Long? = null

    init {
        on<ChatPacketEvent> {
            if (value.contains("[BOSS] Storm: Ouch, that hurt!") || value.contains("[BOSS] Storm: Oof")) {
                val stormTimerTicks = getStormSplitTimer() ?: return@on
                stormStuckAt = stormTimerTicks
                return@on
            }

            if (stormEnrage && value.contains("Storm is enraged")) {
                val stuckAtTicks = stormStuckAt ?: return@on
                val stormTimerTicks = getStormSplitTimer() ?: return@on

                schedule(10){
                    alert("§a${(stormTimerTicks / 20.0).toFixed()}s§r", false)
                    sendChatMessage("Storm killed at ${(stormTimerTicks / 20.0).toFixed()}s (${(stormTimerTicks - stuckAtTicks)} ticks)")
                }
                stormStuckAt = null
                return@on
            }

            if (rag && value.contains("[BOSS] Wither King: I no longer wish to fight, but I know that will not stop you.")) {
                schedule(20, true) { alert("§aRag now!") }
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