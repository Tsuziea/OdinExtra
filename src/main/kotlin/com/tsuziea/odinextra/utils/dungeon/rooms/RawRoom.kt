package com.tsuziea.odinextra.utils.dungeon.rooms

data class RawRoomData(
    val name: String,
    val secretCoords: RawSecretCoords = RawSecretCoords()
)

data class RawSecretCoords(
    val redstoneKey: List<RawPos> = emptyList(),
    val wither: List<RawPos> = emptyList(),
    val bat: List<RawPos> = emptyList(),
    val item: List<RawPos> = emptyList(),
    val chest: List<RawPos> = emptyList()
)

data class RawPos(
    val x: Int,
    val y: Int,
    val z: Int
)
