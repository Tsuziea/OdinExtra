package com.tsuziea.odinextra

import com.odtheking.odin.config.ModuleConfig
import com.odtheking.odin.events.core.EventBus
import com.odtheking.odin.features.ModuleManager
import com.tsuziea.odinextra.features.impl.boss.AutoTerms
import com.tsuziea.odinextra.features.impl.boss.DragonsHp
import com.tsuziea.odinextra.features.impl.boss.FuckDiorite
import com.tsuziea.odinextra.features.impl.dungeon.AutoGFS
import com.tsuziea.odinextra.features.impl.dungeon.AutoLeap
import com.tsuziea.odinextra.features.impl.dungeon.AutoSell
import com.tsuziea.odinextra.features.impl.dungeon.AutoSuperboom
import com.tsuziea.odinextra.features.impl.dungeon.BreakerHelper
import com.tsuziea.odinextra.features.impl.dungeon.CloseChest
import com.tsuziea.odinextra.features.impl.dungeon.DungeonAbilities
import com.tsuziea.odinextra.features.impl.dungeon.TriggerBot
import com.tsuziea.odinextra.features.impl.render.CameraClip
import com.tsuziea.odinextra.features.impl.render.DungeonESP
import com.tsuziea.odinextra.features.impl.render.Fullbright
import com.tsuziea.odinextra.features.impl.render.NameChanger
import com.tsuziea.odinextra.features.impl.skyblock.AutoClicker
import com.tsuziea.odinextra.features.impl.skyblock.AutoExperiments
import com.tsuziea.odinextra.features.impl.skyblock.AutoHarp
import com.tsuziea.odinextra.utils.dungeon.ExtraDungeonListener
import net.fabricmc.api.ClientModInitializer

@Suppress("unused")
object OdinExtra : ClientModInitializer {

    override fun onInitializeClient() {
        listOf(this, ExtraDungeonListener).forEach { EventBus.subscribe(it) }

        ModuleManager.registerModules(ModuleConfig("OdinExtra.json"),
            // boss
            AutoTerms, DragonsHp, FuckDiorite,
            // dungeon
            AutoGFS, AutoLeap, AutoSell, AutoSuperboom, BreakerHelper, CloseChest, DungeonAbilities, TriggerBot,
            // render
            CameraClip, DungeonESP, Fullbright, NameChanger,
            // skyblock
            AutoClicker, AutoHarp, AutoExperiments
        )
    }
}
