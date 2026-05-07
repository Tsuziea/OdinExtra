package com.tsuziea.odinextra.utils.dungeon

import com.odtheking.odin.utils.JsonResourceLoader
import com.odtheking.odin.utils.skyblock.dungeon.tiles.Room
import com.tsuziea.odinextra.utils.dungeon.rooms.ExtraRoomData
import com.tsuziea.odinextra.utils.dungeon.rooms.RawPos
import com.tsuziea.odinextra.utils.dungeon.rooms.RawRoomData
import com.tsuziea.odinextra.utils.dungeon.rooms.RawSecretCoords
import com.tsuziea.odinextra.utils.dungeon.rooms.Secret
import net.minecraft.core.BlockPos

object ExtraRoomUtils {
    val roomList: Set<ExtraRoomData> = JsonResourceLoader
        .loadJson("/assets/odinextra/rooms.json", listOf<RawRoomData>())
        .map { raw ->
            ExtraRoomData(
                name = raw.name,
                secrets = raw.secretCoords.toSecrets().toMutableSet()
            )
        }
        .toSet()

    fun matchRoom(room: Room): ExtraRoomData? {
        return roomList.firstOrNull { it.name == room.data.name }
    }

    private fun RawSecretCoords.toSecrets(): List<Secret> = buildList {
        redstoneKey.forEach { add(Secret("redstoneKey", it.toBlockPos())) }
        wither.forEach { add(Secret("wither", it.toBlockPos())) }
        bat.forEach { add(Secret("bat", it.toBlockPos())) }
        item.forEach { add(Secret("item", it.toBlockPos())) }
        chest.forEach { add(Secret("chest", it.toBlockPos())) }
    }

    private fun RawPos.toBlockPos(): BlockPos = BlockPos(x, y, z)
}
