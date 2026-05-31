package com.tsuziea.odinextra.mixin;

import com.tsuziea.odinextra.features.impl.extra.Nametags;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStandRenderer.class)
public class ArmorStandRendererMixin {

    @Inject(method = "shouldShowName", at = @At("HEAD"), cancellable = true)
    private void odinextra$hideDamageName(ArmorStand armorStand, double distance, CallbackInfoReturnable<Boolean> cir) {
        if (Nametags.INSTANCE.shouldHide(armorStand)) {
            cir.setReturnValue(false);
        }
    }
}
