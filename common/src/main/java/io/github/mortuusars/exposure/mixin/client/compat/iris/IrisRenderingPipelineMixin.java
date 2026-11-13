package io.github.mortuusars.exposure.mixin.client.compat.iris;

import io.github.mortuusars.exposure.integration.iris.IrisCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.renderer.LevelRenderer;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.pipeline.IrisRenderingPipeline", remap = false)
public abstract class IrisRenderingPipelineMixin {
    @Shadow(remap = false)
    private boolean initializedBlockIds;

    @Inject(method = "beginLevelRendering", at = @At("HEAD"), remap = false)
    private void exposure$reuseWorldBlockMappings(CallbackInfo ci) {
        if (IrisCompat.canReuseWorldBlockMappings()) {
            this.initializedBlockIds = true;
        } else if (IrisCompat.consumePendingMainBlockMappingsInitialization()) {
            this.initializedBlockIds = true;
        }
    }

    @Redirect(method = "beginLevelRendering", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;allChanged()V"), remap = false)
    private void exposure$skipChunkReloadForCapture(LevelRenderer levelRenderer) {
        if (!IrisCompat.shouldSkipCaptureChunkReload()) {
            levelRenderer.allChanged();
        }
    }
}
