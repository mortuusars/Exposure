package io.github.mortuusars.exposure.mixin.client.capture;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.mortuusars.exposure.client.render.GammaModifier;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightTexture.class)
public abstract class LightTextureMixin {
    @ModifyReturnValue(method = "getBrightness", at = @At(value = "RETURN"))
    private static float onGetBrightness(float original) {
        return GammaModifier.getModifiedValue(original);
    }
}
