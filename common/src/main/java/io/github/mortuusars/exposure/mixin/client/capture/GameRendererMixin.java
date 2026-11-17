package io.github.mortuusars.exposure.mixin.client.capture;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.render.FovModifier;
import io.github.mortuusars.exposure.client.util.Shader;
import io.github.mortuusars.exposure.event.ClientEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GameRenderer.class, priority = 500)
public abstract class GameRendererMixin {
    @Inject(method = "render", at = @At(value = "INVOKE",
          target = "Lnet/minecraft/client/Minecraft;getMainRenderTarget()Lcom/mojang/blaze3d/pipeline/RenderTarget;"))
    void onRender(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        // Processing viewfinder shader should be done before capturing
        // otherwise Direct capture method will not be affected by it.
        Shader.processForGameRenderer();
        ExposureClient.cycles().tick();
    }

    @Inject(method = "resize", at = @At(value = "HEAD"))
    void onResize(int width, int height, CallbackInfo ci) {
        Shader.resize(width, height);
    }

    @ModifyReturnValue(method = "getFov", at = @At(value = "RETURN", ordinal = 1))
    private double modifyFov(double original, @Local(argsOnly = true) boolean useFOVSetting) {
        return useFOVSetting ? FovModifier.modify(original) : original;
    }

    @Inject(method = "getFov", at = @At(value = "RETURN"), cancellable = true)
    void getFov(Camera activeRenderInfo, float partialTicks, boolean useFOVSetting, CallbackInfoReturnable<Double> cir) {
        if (useFOVSetting && FovModifier.shouldOverride()) {
            cir.setReturnValue(FovModifier.modify(cir.getReturnValue()));
        }
    }
}
