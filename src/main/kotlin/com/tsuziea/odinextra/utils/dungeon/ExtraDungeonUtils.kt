package com.tsuziea.odinextra.utils.dungeon

import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.ScanUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils.getRealCoords
import com.odtheking.odin.utils.skyblock.dungeon.tiles.Room as DungeonRoom
import net.minecraft.core.BlockPos
object ExtraDungeonUtils {
    inline val dungeonStates: ExtraDungeonListener.ExtraDungeonStates
        get() = ExtraDungeonListener.dungeonStates

    inline val keyPicked: Boolean
        get() = dungeonStates.keyPicked

    inline val keyPicker: String?
        get() = dungeonStates.keyPicker

    inline val section: Section
        get() = dungeonStates.section

    fun getRoom(blockPos: BlockPos): DungeonRoom? {
        val roomCenter = ScanUtils.getRoomCenter(blockPos.x, blockPos.z)
        return DungeonUtils.passedRooms.firstOrNull { room ->
            room.roomComponents.any { it.vec2 == roomCenter }
        } ?: ScanUtils.scanRoom(roomCenter)
    }

    fun isSecret(blockPos: BlockPos): Boolean {
        val room = getRoom(blockPos) ?: return false
        val extraRoom = ExtraRoomUtils.matchRoom(room) ?: return false
        return extraRoom.secrets.any { secret ->
            room.getRealCoords(secret.blockPos) == blockPos
        }
    }
}
