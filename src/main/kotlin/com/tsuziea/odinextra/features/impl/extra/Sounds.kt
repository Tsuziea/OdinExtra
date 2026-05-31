package com.tsuziea.odinextra.features.impl.extra

import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.createSoundSettings
import com.odtheking.odin.utils.playSoundSettings
import com.tsuziea.odinextra.features.CustomCategory
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.sounds.SoundEvents


object Sounds : Module(
    name = "Sounds",
    description = "Allows you to customize sounds.",
    category = CustomCategory.Extra
) {
    private val arrowHitSound = createSoundSettings("Arrow Hit Sound", "block.note_block.harp") { true }

    init {
        onReceive<ClientboundSoundPacket> {
            if (sound.value() == SoundEvents.ARROW_HIT_PLAYER) {
                playSoundSettings(arrowHitSound())
                it.cancel()
            }
        }
    }
}
