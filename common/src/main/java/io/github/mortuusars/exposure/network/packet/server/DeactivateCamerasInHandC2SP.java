package io.github.mortuusars.exposure.network.packet.server;

import io.github.mortuusars.exposure.network.PacketDirection;
import io.github.mortuusars.exposure.network.packet.IPacket;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record DeactivateCamerasInHandC2SP() implements IPacket<DeactivateCamerasInHandC2SP> {

    public void toBuffer(FriendlyByteBuf friendlyByteBuf) {}

    public static DeactivateCamerasInHandC2SP fromBuffer(FriendlyByteBuf buffer) {
        return new DeactivateCamerasInHandC2SP();
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        if (player == null)
            throw new IllegalStateException("Cannot handle the packet: Player was null");

        CameraInHand.deactivate(player);
        return true;
    }
}
