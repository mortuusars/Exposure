package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.camera.ExposureCapture;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightTexture.class)
public class ExposureLightMixin {
    @Inject(method = "getBrightness", at = @At(value = "RETURN"), cancellable = true)
    private static void modifyBrightness(DimensionType pDimensionType, int pLightLevel, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(ExposureCapture.getModifiedBrightness(cir.getReturnValue()));
    }
}
