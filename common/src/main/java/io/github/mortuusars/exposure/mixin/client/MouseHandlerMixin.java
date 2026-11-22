package io.github.mortuusars.exposure.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.mortuusars.exposure.client.input.MouseHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.MouseHandler.class)
public class MouseHandlerMixin {
    @Shadow
    private double xpos;

    @Shadow
    private double ypos;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"),
            cancellable = true)
    private void onScroll(long windowPointer, double xOffset, double yOffset, CallbackInfo ci,
                  @Local(ordinal = 4 /* Magic number that corresponds to yScroll variable*/) double yScroll) {
        if (yScroll != 0 && MouseHandler.scrolled(yScroll)) {
            ci.cancel();
        }
    }

    @Inject(method = "onButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getOverlay()Lnet/minecraft/client/gui/screens/Overlay;",
            ordinal = 0), cancellable = true)
    private void onPress(long window, MouseButtonInfo buttonInfo, int action, CallbackInfo ci) {
        double d = net.minecraft.client.MouseHandler.getScaledXPos(this.minecraft.getWindow(), this.xpos);
        double e = net.minecraft.client.MouseHandler.getScaledXPos(this.minecraft.getWindow(), this.ypos);
        MouseButtonEvent mouseButtonEvent = new MouseButtonEvent(d, e, buttonInfo);
        if (MouseHandler.buttonPressed(buttonInfo.button(), action, buttonInfo.modifiers(), mouseButtonEvent))
            ci.cancel();
    }

    @ModifyVariable(method = "turnPlayer", at = @At(value = "STORE"), ordinal = 3)
    private double modifySensitivity(double sensitivity) {
        return MouseHandler.modifySensitivity(sensitivity);
    }
}
