package com.tsuziea.odinextra

import com.odtheking.odin.config.ModuleConfig
import com.odtheking.odin.events.core.EventBus
import com.odtheking.odin.features.ModuleManager
import com.tsuziea.odinextra.features.impl.boss.AutoTerms
import net.fabricmc.api.ClientModInitializer

@Suppress("unused")
object OdinExtra : ClientModInitializer {

    override fun onInitializeClient() {
        listOf(this).forEach { EventBus.subscribe(it) }

        ModuleManager.registerModules(ModuleConfig("OdinExtra.json"),
            AutoTerms
        )
    }
}
