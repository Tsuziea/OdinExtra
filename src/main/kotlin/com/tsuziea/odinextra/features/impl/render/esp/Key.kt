package com.tsuziea.odinextra.features.impl.render.esp

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.world.entity.decoration.ArmorStand

object Key {
    enum class KeyType(val displayName: String) {
        Wither("Wither Key"),
        Blood("Blood Key")
    }

    val keys = mutableMapOf<KeyType, ArmorStand>()

    fun onEntityData(entityId: Int) {
        if (!DungeonUtils.inClear) return
        val stand = mc.level?.getEntity(entityId) as? ArmorStand ?: return
        val keyType = KeyType.entries.find { it.displayName == stand.name.string } ?: return
        keys[keyType] = stand
    }
}
