package io.github.mortuusars.exposure.network.packet.server;

import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public record DeactivateCamerasInHandServerboundPacket() {
    public static void register(SimpleChannel channel, int id) {
        channel.messageBuilder(DeactivateCamerasInHandServerboundPacket.class, id, NetworkDirection.PLAY_TO_SERVER)
                .encoder(DeactivateCamerasInHandServerboundPacket::toBuffer)
                .decoder(DeactivateCamerasInHandServerboundPacket::fromBuffer)
                .consumerMainThread(DeactivateCamerasInHandServerboundPacket::handle)
                .add();
    }

    public void toBuffer(FriendlyByteBuf friendlyByteBuf) {}

    public static DeactivateCamerasInHandServerboundPacket fromBuffer(FriendlyByteBuf buffer) {
        return new DeactivateCamerasInHandServerboundPacket();
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        @Nullable ServerPlayer player = context.getSender();

        if (player == null)
            throw new IllegalStateException("Cannot handle the packet: Player was null");

        CameraInHand.deactivate(player);

        return true;
    }
}
