package io.github.mortuusars.exposure.fabric.mixin;

import io.github.mortuusars.exposure.camera.capture.CaptureManager;
import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderClient;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererFabricMixin {
    @Inject(method = "render", at = @At(value = "RETURN"))
    void render(float partialTicks, long nanoTime, boolean renderLevel, CallbackInfo ci) {
        CaptureManager.onRenderTickEnd();
    }

    @Inject(method = "getFov", at = @At(value = "RETURN"), cancellable = true)
    void getFov(Camera activeRenderInfo, float partialTicks, boolean useFOVSetting, CallbackInfoReturnable<Double> cir) {
        if (!useFOVSetting)
            return;

        double prevFov = cir.getReturnValue();
        double modifiedFov = ViewfinderClient.modifyFov(prevFov);
        if (prevFov != modifiedFov)
            cir.setReturnValue(modifiedFov);
    }
}
