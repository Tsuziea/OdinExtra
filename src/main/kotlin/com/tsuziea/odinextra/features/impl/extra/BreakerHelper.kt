package com.tsuziea.odinextra.features.impl.extra

import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.tsuziea.odinextra.events.InteractEvent
import com.tsuziea.odinextra.utils.CustomCategory

object BreakerHelper : Module(
    name = "Breaker Helper",
    description = "Prevents you from mining blocks classified as secrets.",
    category = CustomCategory.Extra
) {
    init {
        on<InteractEvent.HitBlock> {
            if (!DungeonUtils.inClear) return@on
            val state = mc.level?.getBlockState(pos) ?: return@on
            if (DungeonUtils.isSecret(state, pos)) cancel()
        }
    }
}