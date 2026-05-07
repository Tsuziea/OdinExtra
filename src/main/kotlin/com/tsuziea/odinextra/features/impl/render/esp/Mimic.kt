package com.tsuziea.odinextra.features.impl.render.esp

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.alert
import com.tsuziea.odinextra.utils.dungeon.ExtraDungeonUtils
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.ChestBlockEntity
import net.minecraft.world.level.chunk.LevelChunk

object Mimic {
    var mimic: BlockPos? = null
    var mimicRoomName: String? = null

    private var announced = false

    fun reset() {
        mimic = null
        mimicRoomName = null
        announced = false
    }

    fun scanMimic(chunk: LevelChunk) {
        val level = mc.level ?: return

        for (pos in chunk.blockEntitiesPos) {
            val chest = level.getBlockEntity(pos) as? ChestBlockEntity ?: continue
            if (chest.blockState.block != Blocks.TRAPPED_CHEST) continue
            if (!ExtraDungeonUtils.isSecret(pos)) continue

            mimic = pos
            mimicRoomName = ExtraDungeonUtils.getRoom(pos)?.data?.name

            if (!announced) {
                announced = true
                alert("Mimic Found!", true)
            }
            return
        }
    }
}
