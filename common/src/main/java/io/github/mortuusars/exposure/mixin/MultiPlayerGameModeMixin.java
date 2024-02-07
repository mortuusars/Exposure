package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
    @Inject(method = "interactAt", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/phys/EntityHitResult;getLocation()Lnet/minecraft/world/phys/Vec3;"),
            cancellable = true)
    void onInteractAt(Player player, Entity target, EntityHitResult ray, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
        CameraInHand activeCamera = CameraInHand.getActive(player);
        if (gameMode != null && !activeCamera.isEmpty()) {
            gameMode.useItem(player, activeCamera.getHand());
            cir.setReturnValue(InteractionResult.CONSUME);
        }
    }

    @Inject(method = "useItemOn", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;getWorldBorder()Lnet/minecraft/world/level/border/WorldBorder;"),
            cancellable = true)
    void onUseItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult result, CallbackInfoReturnable<InteractionResult> cir) {
        MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
        CameraInHand activeCamera = CameraInHand.getActive(player);
        if (gameMode != null && !activeCamera.isEmpty()) {
            gameMode.useItem(player, activeCamera.getHand());
            cir.setReturnValue(InteractionResult.CONSUME);
        }
    }
}
