package com.tsuziea.odinextra.features.impl.render.esp

import com.odtheking.odin.features.impl.dungeon.map.MapScanner
import com.odtheking.odin.features.impl.dungeon.map.Door as MapDoor
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.tiles.RoomState


object Door {
    val doors = mutableListOf<MapDoor>()

    fun scanDoors() {
        doors.clear()
        if (!DungeonUtils.inClear) return

        MapScanner.doors.forEach { door ->
            if (door.isConnected() && !door.isOpened()) doors.add(door)
        }
    }

    private fun MapDoor.isConnected(): Boolean {
        val currentRoom = DungeonUtils.currentRoom ?: return false
        return rooms.any { it.owner.data.name == currentRoom.data.name }
    }

    private fun MapDoor.isOpened(): Boolean {
        return rooms.none { it.owner.state == RoomState.UNDISCOVERED || it.owner.state == RoomState.UNOPENED }
    }
}
