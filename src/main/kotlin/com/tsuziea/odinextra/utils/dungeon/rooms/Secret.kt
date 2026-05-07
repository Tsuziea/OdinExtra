package com.tsuziea.odinextra.utils.dungeon.rooms

import net.minecraft.core.BlockPos

data class Secret(
    var type: String,
    var blockPos: BlockPos = BlockPos(0, 0, 0)
)