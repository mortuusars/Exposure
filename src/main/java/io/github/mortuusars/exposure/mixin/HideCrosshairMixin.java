package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.item.StackedPhotographsItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class HideCrosshairMixin {
    @Inject(method = "renderCrosshair", at = @At(value = "HEAD"), cancellable = true)
    private void renderCrosshair(GuiGraphics guiGraphics, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (Config.Client.PHOTOGRAPH_IN_HAND_HIDE_CROSSHAIR.get() && mc.player != null && mc.player.getXRot() > 25f
                && (mc.player.getMainHandItem().getItem() instanceof PhotographItem || mc.player.getMainHandItem().getItem() instanceof StackedPhotographsItem)
                && mc.player.getOffhandItem().isEmpty())
            ci.cancel();
    }
}
