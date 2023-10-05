package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.camera.CameraHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Supplier;

public record DeactivateCameraServerboundPacket(UUID playerUUID) {
    public static void register(SimpleChannel channel, int id) {
        channel.messageBuilder(DeactivateCameraServerboundPacket.class, id, NetworkDirection.PLAY_TO_SERVER)
                .encoder(DeactivateCameraServerboundPacket::toBuffer)
                .decoder(DeactivateCameraServerboundPacket::fromBuffer)
                .consumerMainThread(DeactivateCameraServerboundPacket::handle)
                .add();
    }

    public void toBuffer(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUUID(playerUUID);
    }

    public static DeactivateCameraServerboundPacket fromBuffer(FriendlyByteBuf buffer) {
        return new DeactivateCameraServerboundPacket(buffer.readUUID());
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        @Nullable ServerPlayer player = context.getSender();

        if (player == null)
            throw new IllegalStateException("Cannot handle the packet: Player was null");

        CameraHelper.deactivate(player, false);

        return true;
    }
}
