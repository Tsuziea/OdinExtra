package com.tsuziea.odinextra.mixin;

import com.tsuziea.odinextra.features.impl.render.NameChanger;
import net.minecraft.util.StringDecomposer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.regex.Matcher;

@Mixin(StringDecomposer.class)
public class StringDecomposerMixin {

    @ModifyArg(
            method = "iterateFormatted(Ljava/lang/String;Lnet/minecraft/network/chat/Style;Lnet/minecraft/util/FormattedCharSink;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/StringDecomposer;iterateFormatted(Ljava/lang/String;ILnet/minecraft/network/chat/Style;Lnet/minecraft/util/FormattedCharSink;)Z",
                    ordinal = 0
            ),
            index = 0
    )
    private static String hideName(String text) {
        if (!NameChanger.INSTANCE.getEnabled())
            return text;

        String replacement = NameChanger.INSTANCE.hideName();
        if (replacement.isEmpty())
            return text;

        return NameChanger.INSTANCE.getPattern()
                .matcher(text)
                .replaceAll(Matcher.quoteReplacement(replacement));
    }
}
