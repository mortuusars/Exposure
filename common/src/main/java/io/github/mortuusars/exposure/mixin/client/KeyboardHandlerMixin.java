package io.github.mortuusars.exposure.mixin.client;

import io.github.mortuusars.exposure.client.input.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At(value = "HEAD"), cancellable = true)
    private void keyPress(long window, int action, KeyEvent event, CallbackInfo ci) {
        if (KeyboardHandler.handleKeyPress(window, event.key(), event.scancode(), action, event.modifiers()))
            ci.cancel();
    }
}
