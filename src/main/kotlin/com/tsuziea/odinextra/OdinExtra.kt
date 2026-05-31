package com.tsuziea.odinextra

import com.odtheking.odin.config.ModuleConfig
import com.odtheking.odin.events.core.EventBus
import com.odtheking.odin.features.ModuleManager
import com.tsuziea.odinextra.features.impl.extra.AutoClicker
import com.tsuziea.odinextra.features.impl.extra.AutoDance
import com.tsuziea.odinextra.features.impl.extra.Sounds
import com.tsuziea.odinextra.features.impl.extra.AutoExperiments
import com.tsuziea.odinextra.features.impl.extra.AutoGFS
import com.tsuziea.odinextra.features.impl.extra.AutoHarp
import com.tsuziea.odinextra.features.impl.extra.AutoLeap
import com.tsuziea.odinextra.features.impl.extra.AutoSell
import com.tsuziea.odinextra.features.impl.extra.AutoSuperboom
import com.tsuziea.odinextra.features.impl.extra.AutoTerms
import com.tsuziea.odinextra.features.impl.extra.BreakerHelper
import com.tsuziea.odinextra.features.impl.extra.CameraClip
import com.tsuziea.odinextra.features.impl.extra.CloseChest
import com.tsuziea.odinextra.features.impl.extra.DungeonAbilities
import com.tsuziea.odinextra.features.impl.extra.DungeonESP
import com.tsuziea.odinextra.features.impl.extra.FuckDiorite
import com.tsuziea.odinextra.features.impl.extra.Fullbright
import com.tsuziea.odinextra.features.impl.extra.NameChanger
import com.tsuziea.odinextra.features.impl.extra.Nametags
import com.tsuziea.odinextra.features.impl.extra.Notify
import com.tsuziea.odinextra.features.impl.extra.TrevorHelper
import com.tsuziea.odinextra.features.impl.extra.TriggerBot
import com.tsuziea.odinextra.utils.GlowUtils
import com.tsuziea.odinextra.utils.dungeon.ExtraDungeonListener
import net.fabricmc.api.ClientModInitializer

@Suppress("unused")
object OdinExtra : ClientModInitializer {

    override fun onInitializeClient() {
        listOf(this, ExtraDungeonListener, GlowUtils).forEach { EventBus.subscribe(it) }

        ModuleManager.registerModules(ModuleConfig("OdinExtra.json"),
            AutoTerms, Sounds, FuckDiorite, AutoGFS, AutoLeap, AutoSell,
            AutoSuperboom, BreakerHelper, CloseChest, DungeonAbilities, TriggerBot,
            CameraClip, DungeonESP, Fullbright, NameChanger, AutoClicker, AutoHarp,
            AutoExperiments, TrevorHelper, Notify, AutoDance, Nametags
        )
    }
}
