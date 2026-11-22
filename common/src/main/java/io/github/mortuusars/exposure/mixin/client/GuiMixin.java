package io.github.mortuusars.exposure.mixin.client;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.StackedPhotographsItem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Inject(method = "render", at = @At(value = "HEAD"), cancellable = true)
    private void renderGui(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (CameraClient.viewfinder() != null && CameraClient.viewfinder().isLookingThrough()) {
            CameraClient.viewfinder().overlay().render(guiGraphics, deltaTracker);
            ci.cancel();
        }
    }

    @Inject(method = "renderCrosshair", at = @At(value = "HEAD"), cancellable = true)
    private void renderCrosshair(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        LocalPlayer player = Minecrft.player();
        if (Config.Client.PHOTOGRAPH_IN_HAND_HIDE_CROSSHAIR.get() && player.getXRot() > 25f
                && (player.getMainHandItem().getItem() instanceof PhotographItem || player.getMainHandItem().getItem() instanceof StackedPhotographsItem)
                && player.getOffhandItem().isEmpty())
            ci.cancel();
    }
}
