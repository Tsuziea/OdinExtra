package com.tsuziea.odinextra.utils

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.itemId
import net.minecraft.client.KeyMapping

fun leftClick() {
    KeyMapping.set(mc.options.keyAttack.defaultKey, true)
    KeyMapping.click(mc.options.keyAttack.defaultKey)
    KeyMapping.set(mc.options.keyAttack.defaultKey, false)
}

fun rightClick() {
    KeyMapping.set(mc.options.keyUse.defaultKey, true)
    KeyMapping.click(mc.options.keyUse.defaultKey)
    KeyMapping.set(mc.options.keyUse.defaultKey, false)
}

fun isHolding(itemId: String): Boolean {
    return mc.player?.mainHandItem?.itemId == itemId
}
