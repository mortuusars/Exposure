package io.github.mortuusars.exposure.mixin.client.compat.iris;

import io.github.mortuusars.exposure.integration.iris.IrisCompat;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Function;

/** Selects an independent Iris pipeline while rendering an Exposure camera feed. */
@Pseudo
@Mixin(targets = "net.irisshaders.iris.pipeline.PipelineManager", remap = false)
public abstract class PipelineManagerMixin {
    @ModifyVariable(method = "preparePipeline", at = @At("HEAD"), argsOnly = true, ordinal = 0, remap = false)
    private NamespacedId exposure$useCapturePipeline(NamespacedId worldId) {
        return IrisCompat.getCapturePipelineId(worldId);
    }

    @SuppressWarnings("unchecked")
    @Redirect(method = "preparePipeline", at = @At(value = "INVOKE",
            target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;"), remap = false)
    private Object exposure$buildCapturePipeline(Function<NamespacedId, ?> factory, Object pipelineKey) {
        NamespacedId shaderWorldId = IrisCompat.getPipelineFactoryId((NamespacedId) pipelineKey);
        return factory.apply(shaderWorldId);
    }

    @Redirect(method = "preparePipeline", at = @At(value = "INVOKE",
            target = "Lnet/irisshaders/iris/shaderpack/materialmap/WorldRenderingSettings;isReloadRequired()Z"), remap = false)
    private boolean exposure$deferChunkReloadDuringCapture(WorldRenderingSettings settings) {
        return !IrisCompat.isBackgroundCaptureActive() && settings.isReloadRequired();
    }
}
