package com.tsuziea.odinextra.mixin.odin;

import com.odtheking.odin.features.impl.dungeon.DungeonMap;
import com.odtheking.odin.features.impl.dungeon.map.MapRoom;
import com.odtheking.odin.utils.skyblock.dungeon.tiles.RoomState;
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils;
import com.tsuziea.odinextra.features.impl.render.esp.Mimic;
import kotlin.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DungeonMap.class)
public class MapRoomMixin {
    @Inject(
            method = "renderDungeonMap(Lnet/minecraft/client/gui/GuiGraphics;)Lkotlin/Pair;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void odinextra$hideMapWhenNotInClear(CallbackInfoReturnable<Pair<Integer, Integer>> cir) {
        if (!DungeonUtils.INSTANCE.getInClear()) {
            cir.setReturnValue(new Pair<>(0, 0)); // thus DungeonMap skips render while in boss.
        }
    }

    @Redirect(
            method = "renderDungeonMap(Lnet/minecraft/client/gui/GuiGraphics;)Lkotlin/Pair;",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/odtheking/odin/features/impl/dungeon/map/MapRoom;getState()Lcom/odtheking/odin/utils/skyblock/dungeon/tiles/RoomState;"
            )
    )
    private RoomState odinextra$allowUnvisitedRooms(MapRoom room) {
        String mimicRoomName = Mimic.INSTANCE.getMimicRoomName();
        if (!DungeonUtils.INSTANCE.getMimicKilled() && mimicRoomName != null && mimicRoomName.equals(room.getData().getName())) {
            return RoomState.FAILED; // thus the map show mimic room in red...
        }

        RoomState state = room.getState();
        if (state == RoomState.UNDISCOVERED || state == RoomState.UNOPENED) {
            return RoomState.DISCOVERED; // thus the map shows all scanned rooms
        }
        return state;
    }
}
