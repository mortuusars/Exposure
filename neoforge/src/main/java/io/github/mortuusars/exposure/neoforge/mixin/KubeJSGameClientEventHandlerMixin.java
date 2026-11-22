package io.github.mortuusars.exposure.neoforge.mixin;

import dev.latvian.mods.kubejs.client.KubeJSGameClientEventHandler;
import io.github.mortuusars.exposure.client.capture.task.BackgroundScreenshotCaptureTask;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fixes only sky rendering with KubeJS installed.
 */
@Mixin(KubeJSGameClientEventHandler.class)
public class KubeJSGameClientEventHandlerMixin {
    @Inject(method = "worldRender", at = @At("RETURN"))
    private static void onWorldRender(RenderLevelStageEvent event, CallbackInfo ci) {
        if (BackgroundScreenshotCaptureTask.capturing && BackgroundScreenshotCaptureTask.renderTarget != null) {
            BackgroundScreenshotCaptureTask.renderTarget.bindWrite(false);
        }
    }
}
