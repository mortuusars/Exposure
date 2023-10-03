package io.github.mortuusars.exposure.network.packet;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public record CameraSetZoomServerboundPacket(float focalLength) {
    public static void register(SimpleChannel channel, int id) {
        channel.messageBuilder(CameraSetZoomServerboundPacket.class, id, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CameraSetZoomServerboundPacket::toBuffer)
                .decoder(CameraSetZoomServerboundPacket::fromBuffer)
                .consumerMainThread(CameraSetZoomServerboundPacket::handle)
                .add();
    }

    public static void send(float focalLength) {
        Packets.sendToServer(new CameraSetZoomServerboundPacket(focalLength));
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeFloat(focalLength);
    }

    public static CameraSetZoomServerboundPacket fromBuffer(FriendlyByteBuf buffer) {
        return new CameraSetZoomServerboundPacket(buffer.readFloat());
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        @Nullable ServerPlayer player = context.getSender();
        Preconditions.checkState(player != null, "Cannot handle packet: Player was null");

        CameraInHand camera = CameraInHand.ofPlayer(player);
        if (!camera.isEmpty()) {
            camera.getItem().setZoom(camera.getStack(), focalLength);
        }

        return true;
    }
}
