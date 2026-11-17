package io.github.mortuusars.exposure.mixin.client.compat.iris;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.mortuusars.exposure.integration.iris.IrisCompat;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
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

    @WrapOperation(method = "preparePipeline", at = @At(value = "INVOKE",
            target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;"), remap = false)
    private <T, R> Object exposure$buildCapturePipeline(Function<NamespacedId, WorldRenderingPipeline> instance, T t, Operation<R> original) {
        return original.call(instance, IrisCompat.getPipelineFactoryId((NamespacedId) t));
    }

    @WrapOperation(method = "preparePipeline", at = @At(value = "INVOKE",
            target = "Lnet/irisshaders/iris/shaderpack/materialmap/WorldRenderingSettings;isReloadRequired()Z"), remap = false)
    private boolean exposure$deferChunkReloadDuringCapture(WorldRenderingSettings instance, Operation<Boolean> original) {
        return !IrisCompat.isBackgroundCaptureActive() && original.call(instance);
    }
}
