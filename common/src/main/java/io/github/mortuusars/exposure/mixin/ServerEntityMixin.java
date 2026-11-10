package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.mortaar.network.Packets;
import io.github.mortuusars.exposure.network.packet.clientbound.CameraStandSetRotationsS2CP;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerEntity.class)
public class ServerEntityMixin {
    @Shadow @Final private Entity entity;

    // We need to send stand's full rotation values, because vanilla packets have limited data (bytes as rotations, for example).

    @Inject(method = "addPairing", at = @At("RETURN"))
    private void onAddPairing(ServerPlayer player, CallbackInfo ci) {
        if (this.entity instanceof CameraStandEntity stand) {
            Packets.sendToClient(new CameraStandSetRotationsS2CP(stand.getId(), stand.getYRot(), stand.getXRot()), player);
        }
    }

    @Inject(method = "sendChanges", at = @At("RETURN"))
    private void onSendChanges(CallbackInfo ci) {
        if (this.entity instanceof CameraStandEntity stand) {
            stand.syncRotationToClientsIfNeeded();
        }
    }
}
