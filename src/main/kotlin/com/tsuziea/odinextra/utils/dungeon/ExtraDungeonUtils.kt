package com.tsuziea.odinextra.utils.dungeon

import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.Floor
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

object ExtraDungeonUtils {
    inline val dungeonStates: ExtraDungeonListener.ExtraDungeonStates
        get() = ExtraDungeonListener.dungeonStates

    inline val keyPicked: Boolean
        get() = dungeonStates.keyPicked

    inline val section: Section
        get() = dungeonStates.section

    fun getPlayerSection(position: Vec3): Section {
        if (DungeonUtils.floor != Floor.M7 || !DungeonUtils.inBoss) return Section.Unknown
        return sectionBoxes.firstOrNull { (box, _) -> box.contains(position) }?.second ?: Section.Unknown
    }

    private val sectionBoxes = listOf(
        AABB(89.0, 100.0, 27.0, 114.0, 154.0, 122.0) to Section.S1,
        AABB(114.0, 100.0, 19.0, 147.0, 154.0, 122.0) to Section.S2,
        AABB(-4.0, 100.0, 19.0, 147.0, 154.0, 52.0) to Section.S3,
        AABB(-5.0, 100.0, 27.0, 89.0, 154.0, 51.0) to Section.S4,
    )
}
