package io.github.mortuusars.exposure.fabric.mixin;

import io.github.mortuusars.exposure.camera.infrastructure.ZoomDirection;
import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderClient;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerFabricMixin {
    @SuppressWarnings("InvalidInjectorMethodSignature") // It's valid. I checked. It works.
    @Inject(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"),
            cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    void onScroll(long windowPointer, double xOffset, double yOffset, CallbackInfo ci, double d, int i) {
        if (i != 0 && ViewfinderClient.handleMouseScroll(i > 0d ? ZoomDirection.IN : ZoomDirection.OUT))
            ci.cancel();
    }

    @Inject(method = "onPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getOverlay()Lnet/minecraft/client/gui/screens/Overlay;",
            ordinal = 0), cancellable = true)
    void onScroll(long windowPointer, int button, int action, int modifiers, CallbackInfo ci) {
        if (io.github.mortuusars.exposure.client.MouseHandler.handleMouseButtonPress(button, action, modifiers))
            ci.cancel();
    }
}
