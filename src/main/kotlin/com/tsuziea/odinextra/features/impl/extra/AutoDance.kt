package com.tsuziea.odinextra.features.impl.extra

import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.alert
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.noControlCodes
import com.tsuziea.odinextra.features.CustomCategory
import com.tsuziea.odinextra.utils.leftClick
import net.minecraft.client.KeyMapping
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.sounds.SoundEvents

object AutoDance : Module(
    name = "Auto Dance",
    description = "Automatically handles the Mirrorverse dance room actions.",
    category = CustomCategory.Extra
) {
    private var beats = -1
    private var handled = false
    private var isActive = false

    init {
        on<WorldEvent.Load> {
            reset()
        }

        on<TickEvent.Start> {
            if (mc.player == null || !isActive) return@on
            if (!handled) handleBeat()
        }

        onReceive<ClientboundSetSubtitleTextPacket> {
            if (isActive) return@onReceive
            if (text.string.noControlCodes.trim() == "Move!") isActive = true
        }

        onReceive<ClientboundSoundPacket> {
            if (!isActive) return@onReceive

            if (sound.value() == SoundEvents.PLAYER_BURP) {
                reset()
                return@onReceive
            }

            if (sound.value() == SoundEvents.NOTE_BLOCK_BASS.value()) {
                if (pitch in listOf(0.7936508f, 1.0158731f, 1.1904762f, 1.3492063f, 1.4920635f)) return@onReceive
                beats++
                handled = false
                return@onReceive
            }
        }
    }

    private fun handleBeat() {
        if (beats > 95) {
            alert("§aDance completed!")
            reset()
            return
        }

        if (beats >= 8 && beats % 4 == 0) {
            setSneak(true)
        } else if (beats >= 8 && beats % 4 == 1) {
            setSneak(false)
        }

        if (beats >= 24 && (beats % 8 == 0 || beats % 8 == 2)) {
            schedule(10) { setJump(true) }
            schedule(12) { setJump(false) }
        }

        if (beats >= 64 && beats % 2 == 0) {
            schedule(16) { leftClick() }
        }

        handled = true
    }

    private fun setSneak(state: Boolean) {
        KeyMapping.set(mc.options.keyShift.defaultKey, state)
    }

    private fun setJump(state: Boolean) {
        KeyMapping.set(mc.options.keyJump.defaultKey, state)
    }

    private fun reset() {
        isActive = false
        beats = -1
        handled = false
        setSneak(false)
        setJump(false)
    }
}
