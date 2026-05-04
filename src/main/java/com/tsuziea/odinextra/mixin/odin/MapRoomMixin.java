package com.tsuziea.odinextra.mixin.odin;

import com.odtheking.odin.features.impl.dungeon.DungeonMap;
import com.odtheking.odin.features.impl.dungeon.map.MapRoom;
import com.odtheking.odin.utils.skyblock.dungeon.tiles.RoomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DungeonMap.class)
public class MapRoomMixin {
    @Redirect(
            method = "renderDungeonMap(Lnet/minecraft/client/gui/GuiGraphics;)Lkotlin/Pair;",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/odtheking/odin/features/impl/dungeon/map/MapRoom;getState()Lcom/odtheking/odin/utils/skyblock/dungeon/tiles/RoomState;"
            )
    )
    private RoomState odinextra$allowUnvisitedRooms(MapRoom room) {
        RoomState state = room.getState();
        if (state == RoomState.UNDISCOVERED || state == RoomState.UNOPENED) {
            return RoomState.DISCOVERED;
        }
        return state;
    }
}
