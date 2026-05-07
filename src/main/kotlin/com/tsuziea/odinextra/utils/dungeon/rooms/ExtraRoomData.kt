package com.tsuziea.odinextra.utils.dungeon.rooms

data class ExtraRoomData(
    val name: String,
    val secrets: MutableSet<Secret> = mutableSetOf()
)