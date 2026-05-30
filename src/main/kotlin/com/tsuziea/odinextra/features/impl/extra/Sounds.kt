package com.tsuziea.odinextra.features.impl.extra

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
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
    private val hideEnderman by BooleanSetting("Enderman", true, desc = "Hides enderman sounds.")
    private val hideLightning by BooleanSetting("Lightning", true, desc = "Hides thunder sounds.")
    private val hideExperienceOrb by BooleanSetting("Experience Orb", true, "Hides experience orb sounds.")
    private val hideArrowHit by BooleanSetting("Arrow Hit", true, "Hides arrow hit sounds.")
    private val arrowHitPlayer by BooleanSetting("Arrow Hit Player", true, desc = "Replaces arrow hit player sounds.")
    private val arrowHitPlayerSound = createSoundSettings("Arrow Hit Sound", "block.note_block.harp") { arrowHitPlayer }

    init {
        onReceive<ClientboundSoundPacket> {
            when (sound.value()) {
                SoundEvents.ENDERMAN_AMBIENT,
                SoundEvents.ENDERMAN_SCREAM,
                SoundEvents.ENDERMAN_DEATH,
                SoundEvents.ENDERMAN_HURT,
                SoundEvents.ENDERMAN_STARE -> if (hideEnderman) it.cancel()

                SoundEvents.LIGHTNING_BOLT_THUNDER -> if (hideLightning) it.cancel()

                SoundEvents.EXPERIENCE_ORB_PICKUP -> if (hideExperienceOrb) it.cancel()

                SoundEvents.ARROW_HIT -> if (hideArrowHit) it.cancel()
            }

            if (arrowHitPlayer && sound.value() == SoundEvents.ARROW_HIT_PLAYER) {
                playSoundSettings(arrowHitPlayerSound())
                it.cancel()
            }
        }
    }
}
