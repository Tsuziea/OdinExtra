package com.tsuziea.odinextra.features.impl.extra

import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.noControlCodes
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.tsuziea.odinextra.features.CustomCategory
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket

object CloseChest : Module(
    name = "Close Chest",
    description = "Allows you to instantly close chests automatically.",
    category = CustomCategory.Extra
) {
    init {
        onReceive<ClientboundOpenScreenPacket> {
            if (!DungeonUtils.inDungeons) return@onReceive

            if (title.string.noControlCodes.equalsOneOf("Chest", "Large Chest")) {
                mc.connection?.send(ServerboundContainerClosePacket(containerId))
                it.cancel()
            }
        }
    }
}