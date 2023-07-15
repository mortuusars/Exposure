package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.client.render.ViewfinderRenderer;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class DrawViewfinderOverlayMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getProfiler()Lnet/minecraft/util/profiling/ProfilerFiller;", ordinal = 1))
    private void renderViewfinder(float pPartialTicks, long pNanoTime, boolean pRenderLevel, CallbackInfo ci) {
        if (ViewfinderRenderer.shouldRender())
            ViewfinderRenderer.render();
    }
}
