package com.tsuziea.odinextra

import com.odtheking.odin.config.ModuleConfig
import com.odtheking.odin.events.core.EventBus
import com.odtheking.odin.features.ModuleManager
import com.tsuziea.odinextra.features.impl.boss.AutoTerms
import com.tsuziea.odinextra.features.impl.boss.DragonsHp
import com.tsuziea.odinextra.features.impl.boss.FuckDiorite
import com.tsuziea.odinextra.features.impl.dungeon.AutoGFS
import com.tsuziea.odinextra.features.impl.render.Fullbright
import com.tsuziea.odinextra.features.impl.render.NameChanger
import com.tsuziea.odinextra.features.impl.skyblock.AutoClicker
import com.tsuziea.odinextra.features.impl.skyblock.AutoExperiments
import com.tsuziea.odinextra.features.impl.skyblock.AutoHarp
import net.fabricmc.api.ClientModInitializer

@Suppress("unused")
object OdinExtra : ClientModInitializer {

    override fun onInitializeClient() {
        listOf(this).forEach { EventBus.subscribe(it) }

        ModuleManager.registerModules(ModuleConfig("OdinExtra.json"),
            // boss
            AutoTerms, DragonsHp, FuckDiorite,
            // render
            Fullbright, NameChanger, AutoGFS,
            // skyblock
            AutoClicker, AutoHarp, AutoExperiments
        )
    }
}
