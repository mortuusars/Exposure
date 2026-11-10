package io.github.mortuusars.exposure.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.pipeline.RenderTarget;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.camera.viewfinder.ViewfinderCameraControlsScreen;
import io.github.mortuusars.exposure.client.capture.task.BackgroundScreenshotCaptureTask;
import io.github.mortuusars.exposure.event.ClientEvents;
import io.github.mortuusars.mortaar.network.Packets;
import io.github.mortuusars.exposure.network.packet.serverbound.ActiveCameraReleaseC2SP;
import io.github.mortuusars.exposure.world.camera.CameraOnStand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
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
            Packets.sendToServer(ActiveCameraReleaseC2SP.INSTANCE);
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
    void onSetScreen(Screen screen, CallbackInfo ci) {
        if (player != null && CameraClient.isActive() && !(screen instanceof ViewfinderCameraControlsScreen)) {
            CameraClient.deactivate();
        }
    }

    @Inject(method = "setLevel", at = @At("HEAD"))
    void onLevelUnload(ClientLevel newLevel, ReceivingLevelScreen.Reason reason, CallbackInfo ci) {
        if (level != null) {
            ClientEvents.levelUnloaded();
        }
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;resetData()V", shift = At.Shift.AFTER))
    void disconnect(Screen nextScreen, boolean keepResourcePacks, CallbackInfo ci) {
        ClientEvents.disconnect();
    }

    /**
     * Fixes incompatibility with Iris and Distant Horizons (and potentially others).
     * But this is not working properly if Distant Horizons and Iris are both installed and shaders are used - LODs render weirdly on top.
     * (this issue is also fixes itself when LODs are toggled off and back on again). I don't know how to fix it, so Background capture is disabled if those mods are both detected.
     * If those mods are used by themselves all is ok.
     */
    @ModifyReturnValue(method = "getMainRenderTarget", at = @At("RETURN"))
    RenderTarget onGetMainRenderTarget(RenderTarget original) {
        if (BackgroundScreenshotCaptureTask.isCapturing()) {
            return BackgroundScreenshotCaptureTask.getRenderTarget();
        }
        return original;
    }
}
