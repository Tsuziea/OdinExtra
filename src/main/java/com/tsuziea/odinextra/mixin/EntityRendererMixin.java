package com.tsuziea.odinextra.mixin;

import com.tsuziea.odinextra.features.impl.extra.Nametags;
import com.tsuziea.odinextra.utils.GlowUtils;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void odinextra$applyGlow(Entity entity, EntityRenderState state, float f, CallbackInfo ci) {
        Integer color = GlowUtils.INSTANCE.getGlowColor(entity);
        if (color != null) {
            state.outlineColor = color;
        }

        if (state.entityType == EntityType.PLAYER && Nametags.INSTANCE.shouldHideDisplayName(Math.sqrt(state.distanceToCameraSq))) {
            state.nameTag = null;
        }
    }
}
