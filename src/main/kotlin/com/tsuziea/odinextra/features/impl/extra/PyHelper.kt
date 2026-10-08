package com.tsuziea.odinextra.features.impl.extra

import com.mojang.blaze3d.platform.InputConstants
import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.InputEvent
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.MessageEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.alert
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.M7Phases
import com.odtheking.odin.utils.toFixed
import com.tsuziea.odinextra.features.CustomCategory
import com.tsuziea.odinextra.events.TickStart
import com.tsuziea.odinextra.utils.dungeon.ExtraDungeonUtils.getStormSplitTimer
import com.tsuziea.odinextra.utils.isHolding

object PyHelper : Module(
    name = "Py Helper",
    description = "Left click while holding Last Breath in P2 to active.",
    category = CustomCategory.Extra
) {
    private val swap by BooleanSetting("Swap", true, desc = "Whether swap slot after debuff.")
    private val slot by NumberSetting("Hotbar Slot", 1.0, 1.0, 8.0, 1, desc = "Slot to swap to.").withDependency { swap }

    private var state = State.IDLE

    private var stormStuckAt: Long? = null
    private var debuffed: Boolean = false

    init {
        on<LevelEvent.Load> {
            state = State.IDLE
            stormStuckAt = null
            debuffed = false
        }

        on<MessageEvent.Chat> {
            if (message.contains("[BOSS] Storm: Ouch, that hurt!") || message.contains("[BOSS] Storm: Oof")) {
                val stormTimerTicks = getStormSplitTimer() ?: return@on

                modMessage("Storm stuck at §a${(stormTimerTicks / 20.0).toFixed()}s§r")

                stormStuckAt = stormTimerTicks
                return@on
            }

            if (message.contains("Storm is enraged")) {
                val stuckAtTicks = stormStuckAt ?: return@on
                val stormTimerTicks = getStormSplitTimer() ?: return@on

                modMessage("Storm enraged at §a${(stormTimerTicks / 20.0).toFixed()}s§r §7(${((stormTimerTicks - stuckAtTicks) / 20.0).toFixed()}s)")
                schedule(2){ alert("§5${(stormTimerTicks / 20.0).toFixed()}s§r", false) }

                stormStuckAt = null
                return@on
            }
        }

        on<InputEvent> {
            if (state != State.IDLE || mc.screen != null || key.value != InputConstants.MOUSE_BUTTON_LEFT || DungeonUtils.getF7Phase() != M7Phases.P2 || debuffed) return@on

            val stormTimerTicks = getStormSplitTimer() ?: return@on
            if (stormTimerTicks / 20.0 !in 25.00..35.00) return@on

            if (isHolding(lastBreathIds)){
                state = State.PULL
                cancel()
            }
        }

        on<TickStart>{
            val player = mc.player ?: return@on
            val stormTimerTicks = getStormSplitTimer() ?: return@on

            when (state) {
                State.PULL -> {
                    mc.options.keyUse.isDown = true
                    player.drop(false)

                    state = State.SHOOT
                }

                State.SHOOT -> {
                    if ((stormTimerTicks / 20.0) < 32.40) return@on
                    mc.options.keyUse.isDown = false
                    alert("GO!")

                    debuffed = true
                    state = State.SWAP
                }

                State.SWAP -> {
                    if (swap) player.inventory.selectedSlot = (slot - 1).toInt()
                    mc.options.keyUse.isDown = true

                    state = State.IDLE
                }

                else -> Unit
            }
        }
    }

    private enum class State { IDLE, PULL, SHOOT, SWAP }
    private val lastBreathIds = setOf("LAST_BREATH", "STARRED_LAST_BREATH")
}
