package io.github.mortuusars.exposure.mixin.client.capture;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.client.capture.action.HideGuiAndOverlaysAction;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                          double camX, double camY, double camZ, CallbackInfo ci) {
        if (HideGuiAndOverlaysAction.shouldBlockDebugRenderer) {
            ci.cancel();
        }
    }
}
