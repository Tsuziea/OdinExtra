package com.tsuziea.odinextra.features.impl.extra

import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.impl.dungeon.map.MapScanner
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.drawWireFrameBox
import com.odtheking.odin.features.impl.dungeon.map.Door as MapDoor
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.tiles.RoomState
import com.tsuziea.odinextra.features.CustomCategory
import com.tsuziea.odinextra.utils.dungeon.ExtraDungeonUtils
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB


object DoorESP : Module(
    name = "Door ESP",
    description = "Shows Doors through wall.",
    category = CustomCategory.Extra
) {
    val doors = mutableListOf<MapDoor>()

    init {
        on<TickEvent.Start> {
            scanDoors()
            replaceDoorBlocks()
        }

        on<RenderEvent.Extract>{
            doors.forEach { door ->
                val color = when (door.type) {
                    MapDoor.Type.NORMAL -> Colors.MINECRAFT_AQUA
                    MapDoor.Type.WITHER, MapDoor.Type.BLOOD -> if (ExtraDungeonUtils.keyPicked) Colors.MINECRAFT_GREEN else Colors.MINECRAFT_RED
                }

                val aabb = AABB(door.pos.x - 1.0, 69.0, door.pos.z - 1.0, door.pos.x + 2.0, 73.0, door.pos.z + 2.0)
                drawWireFrameBox(aabb, color)
            }
        }
    }

    private fun scanDoors() {
        doors.clear()
        if (!DungeonUtils.inClear) return

        MapScanner.doors.forEach { door ->
            if (door.isConnected() && !door.isOpened()) doors.add(door)
        }
    }

    private fun replaceDoorBlocks() {
        val level = mc.level ?: return

        for (door in doors) {
            val doorBlock = when (door.type) {
                MapDoor.Type.NORMAL -> Blocks.INFESTED_CHISELED_STONE_BRICKS
                MapDoor.Type.WITHER -> Blocks.COAL_BLOCK
                MapDoor.Type.BLOOD -> Blocks.RED_TERRACOTTA
            }

            val newState = when (door.type) {
                MapDoor.Type.NORMAL -> Blocks.GLASS.defaultBlockState()
                MapDoor.Type.WITHER -> Blocks.BLACK_STAINED_GLASS.defaultBlockState()
                MapDoor.Type.BLOOD -> Blocks.RED_STAINED_GLASS.defaultBlockState()
            }

            for (pos in BlockPos.betweenClosed(door.pos.x - 1, 69, door.pos.z - 1, door.pos.x + 1, 72, door.pos.z + 1)) {
                val state = level.getBlockState(pos)

                if (state.block == doorBlock) {
                    level.setBlock(pos, newState, 3)
                }
            }
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
