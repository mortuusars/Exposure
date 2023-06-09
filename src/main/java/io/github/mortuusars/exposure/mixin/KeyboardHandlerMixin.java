package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At(value = "HEAD"), cancellable = true)
    private void keyPress(long pWindowPointer, int pKey, int pScanCode, int pAction, int pModifiers, CallbackInfo ci) {
        if (KeyboardHandler.handleKeyPress(pWindowPointer, pKey, pScanCode, pAction, pModifiers))
            ci.cancel();
    }
}
