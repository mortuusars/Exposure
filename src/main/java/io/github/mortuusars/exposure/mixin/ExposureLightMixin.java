package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.camera.CameraCapture;
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
        if (!CameraCapture.isCapturing())
            return;

        cir.setReturnValue(CameraCapture.modifyBrightness(cir.getReturnValue()));
    }
}
