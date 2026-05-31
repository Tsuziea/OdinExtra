package com.tsuziea.odinextra.utils.dungeon

object ExtraDungeonUtils {
    inline val dungeonStates: ExtraDungeonListener.ExtraDungeonStates
        get() = ExtraDungeonListener.dungeonStates

    inline val keyPicked: Boolean
        get() = dungeonStates.keyPicked


    inline val section: Section
        get() = dungeonStates.section
}
