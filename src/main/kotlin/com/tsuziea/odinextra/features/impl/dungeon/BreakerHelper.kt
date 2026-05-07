package com.tsuziea.odinextra.features.impl.dungeon

import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.itemId
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.tsuziea.odinextra.events.InteractEvent

object BreakerHelper : Module(
    name = "Breaker Helper",
    description = "Prevents you from mining blocks classified as secrets."
) {
    init {
        on<InteractEvent.HitBlock> {
            if (!DungeonUtils.inDungeons) return@on
            if (item.itemId != "DUNGEONBREAKER") return@on
            val state = mc.level?.getBlockState(pos) ?: return@on
            if (!DungeonUtils.isSecret(state, pos)) return@on

            cancel()
        }
    }
}