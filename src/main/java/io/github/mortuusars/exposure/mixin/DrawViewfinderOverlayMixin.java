package io.github.mortuusars.exposure.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.client.ViewfinderRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(GameRenderer.class)
public class DrawViewfinderOverlayMixin {
    @Shadow @Nullable private PostChain postEffect;
    private static final PoseStack POSE_STACK = new PoseStack();

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getProfiler()Lnet/minecraft/util/profiling/ProfilerFiller;", ordinal = 1))
    private void renderViewfinder(float pPartialTicks, long pNanoTime, boolean pRenderLevel, CallbackInfo ci) {
        if (ViewfinderRenderer.shouldRender())
            ViewfinderRenderer.render();
    }
}
