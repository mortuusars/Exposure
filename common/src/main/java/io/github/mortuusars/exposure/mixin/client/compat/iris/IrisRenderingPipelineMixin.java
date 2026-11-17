package io.github.mortuusars.exposure.mixin.client.compat.iris;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.mortuusars.exposure.integration.iris.IrisCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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

    @WrapOperation(method = "beginLevelRendering", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;allChanged()V"), remap = false)
    private void exposure$skipChunkReloadForCapture(LevelRenderer instance, Operation<Void> original) {
        if (!IrisCompat.shouldSkipCaptureChunkReload()) {
            original.call(instance);
        }
    }
}
