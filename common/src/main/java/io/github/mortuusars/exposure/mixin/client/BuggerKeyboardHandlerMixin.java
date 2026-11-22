package io.github.mortuusars.exposure.mixin.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.PlatformHelper;
import io.github.mortuusars.exposure.client.util.bugger.Bugger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.KeyboardHandler.class)
public class BuggerKeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At(value = "RETURN"), cancellable = true)
    private void keyPress(long windowPointer, int action, KeyEvent event, CallbackInfo ci) {
        if (!PlatformHelper.isInDevEnv()) return;
        if (!Minecraft.getInstance().gui.getDebugOverlay().showDebugScreen()) return;
        if (Minecraft.getInstance().screen != null) return;
        if (action == InputConstants.PRESS && Bugger.onKeyPress(event.key(), event.scancode(), event.modifiers())) ci.cancel();
        if (action == InputConstants.REPEAT && Bugger.onKeyRepeat(event.key(), event.scancode(), event.modifiers())) ci.cancel();
        if (action == InputConstants.RELEASE && Bugger.onKeyRelease(event.key(), event.scancode(), event.modifiers())) ci.cancel();
    }
}
