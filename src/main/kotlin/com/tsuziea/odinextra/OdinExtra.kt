package com.tsuziea.odinextra

import com.odtheking.odin.config.ModuleConfig
import com.odtheking.odin.events.core.EventBus
import com.odtheking.odin.features.ModuleManager
import com.tsuziea.odinextra.events.dispatcher.FabricEventDispatcher
import com.tsuziea.odinextra.features.impl.extra.AutoBone
import com.tsuziea.odinextra.features.impl.extra.AutoClicker
import com.tsuziea.odinextra.features.impl.extra.AutoDance
import com.tsuziea.odinextra.features.impl.extra.AutoExperiments
import com.tsuziea.odinextra.features.impl.extra.AutoHarp
import com.tsuziea.odinextra.features.impl.extra.FastLeap
import com.tsuziea.odinextra.features.impl.extra.AutoSell
import com.tsuziea.odinextra.features.impl.extra.AutoBoom
import com.tsuziea.odinextra.features.impl.extra.Ability
import com.tsuziea.odinextra.features.impl.extra.AutoCrit
import com.tsuziea.odinextra.features.impl.extra.NameChanger
import com.tsuziea.odinextra.features.impl.extra.PyHelper
import com.tsuziea.odinextra.features.impl.extra.PlayerTracker
import com.tsuziea.odinextra.features.impl.extra.TriggerBot
import com.tsuziea.odinextra.utils.dungeon.ExtraDungeonListener
import net.fabricmc.api.ClientModInitializer

@Suppress("unused")
object OdinExtra : ClientModInitializer {

    override fun onInitializeClient() {
        listOf(this, FabricEventDispatcher, ExtraDungeonListener).forEach { EventBus.subscribe(it) }

        ModuleManager.registerModules(ModuleConfig("OdinExtra.json"),
            FastLeap, AutoSell, AutoBoom, TriggerBot, NameChanger,
            AutoClicker, AutoHarp, AutoExperiments, AutoDance,
            AutoBone, AutoCrit, PyHelper, Ability, PlayerTracker
        )
    }
}
