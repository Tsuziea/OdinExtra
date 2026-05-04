package com.tsuziea.odinextra.mixin.odin;

import com.odtheking.odin.features.Module;
import com.odtheking.odin.features.ModuleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ModuleManager.class)
public class ModuleManagerMixin {
    @Redirect(
            method = "registerModules",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/odtheking/odin/features/Module;isDevModule()Z"
            )
    )
    private boolean odinextra$allowDevModules(Module module)
    {
        return false;
    }
}
