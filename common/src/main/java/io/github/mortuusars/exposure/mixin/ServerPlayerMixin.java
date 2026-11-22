package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.world.entity.CameraOperator;

import com.mojang.authlib.GameProfile;
import io.github.mortuusars.exposure.event.ServerEvents;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.clientbound.ActiveCameraRemoveS2CP;
import io.github.mortuusars.exposure.world.camera.Camera;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements CameraOperator {
    public ServerPlayerMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Inject(method = "drop(Z)V", at = @At(value = "HEAD"))
    void onDrop(boolean dropStack, CallbackInfo ci) {
        ServerEvents.itemDrop(((ServerPlayer) (Object) this));
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        ServerEvents.playerTick(((ServerPlayer) (Object) this));
    }

    @Override
    public void onExposureCameraSet(Camera camera) {
        Packets.sendToAllClients(camera.createSyncPacket());
    }

    @Override
    public void onExposureCameraRemoved() {
        Packets.sendToAllClients(new ActiveCameraRemoveS2CP(getId()));
    }
}
