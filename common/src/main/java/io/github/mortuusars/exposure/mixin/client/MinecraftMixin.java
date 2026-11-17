package io.github.mortuusars.exposure.mixin.client;

import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.camera.viewfinder.ViewfinderCameraControlsScreen;
import io.github.mortuusars.mortaar.network.Packets;
import io.github.mortuusars.exposure.network.packet.serverbound.ServerboundActiveCameraReleasePacket;
import io.github.mortuusars.exposure.world.camera.CameraOnStand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow @Nullable public LocalPlayer player;
    @Shadow @Nullable public ClientLevel level;
    @Shadow @Nullable public Screen screen;

    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isHandsBusy()Z"), cancellable = true)
    void onStartUseItem(CallbackInfo ci) {
        if (player == null || player.isHandsBusy()) return;
        if (screen instanceof ViewfinderCameraControlsScreen) return; // Screen handles right click.

        if (player.getActiveExposureCamera() instanceof CameraOnStand cameraOnStand && cameraOnStand.isActive()) {
            cameraOnStand.release();
            Packets.sendToServer(ServerboundActiveCameraReleasePacket.INSTANCE);
            ci.cancel();
        }
    }

    @Inject(method = "startAttack", at = @At(value = "HEAD"), cancellable = true)
    void onStartAttack(CallbackInfoReturnable<Boolean> cir) {
        if (player != null && player.getActiveExposureCamera() instanceof CameraOnStand) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;added()V"))
    void onSetScreen(Screen guiScreen, CallbackInfo ci) {
        if (player != null && CameraClient.isActive() && !(guiScreen instanceof ViewfinderCameraControlsScreen)) {
            CameraClient.deactivate();
        }
    }
}
