package com.tsuziea.odinextra.utils.dungeon

import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.ScanUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils.getRealCoords
import com.odtheking.odin.utils.skyblock.dungeon.M7Phases
import com.odtheking.odin.utils.skyblock.dungeon.tiles.Room as DungeonRoom
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

object ExtraDungeonUtils {
    inline val dungeonStates: ExtraDungeonListener.ExtraDungeonStates
        get() = ExtraDungeonListener.dungeonStates

    inline val keyPicked: Boolean
        get() = dungeonStates.keyPicked

    inline val keyPicker: String?
        get() = dungeonStates.keyPicker

    inline val section: Section
        get() = dungeonStates.section

    inline val witherBoss: WitherBoss
        get() = dungeonStates.witherBoss

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

    fun getPlayerSection(player: Player): Section? {
        if (DungeonUtils.getF7Phase() != M7Phases.P3) return null

        val s1 = AABB(Vec3(101.0, 100.0, 56.0), Vec3(90.0, 155.0, 120.0))
        val s2 = AABB(Vec3(88.0, 100.0, 142.0), Vec3(20.0, 155.0, 122.0))
        val s3 = AABB(Vec3(-1.0, 100.0, 120.0), Vec3(18.0, 155.0, 52.0))
        val s4 = AABB(Vec3(20.0, 100.0, 30.0), Vec3(58.0, 155.0, 96.0))
        val core = AABB(Vec3(50.0, 100.0, 56.0), Vec3(58.0, 155.0, 96.0))

        val position = player.position()

        return when {
            core.contains(position) -> Section.CORE
            s1.contains(position) -> Section.S1
            s2.contains(position) -> Section.S2
            s3.contains(position) -> Section.S3
            s4.contains(position) -> Section.S4
            else -> null
        }
    }
}
