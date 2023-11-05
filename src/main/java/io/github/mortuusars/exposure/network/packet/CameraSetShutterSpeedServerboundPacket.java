package io.github.mortuusars.exposure.network.packet;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.camera.infrastructure.ShutterSpeed;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public record CameraSetShutterSpeedServerboundPacket(ShutterSpeed shutterSpeed) {
    public static void register(SimpleChannel channel, int id) {
        channel.messageBuilder(CameraSetShutterSpeedServerboundPacket.class, id, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CameraSetShutterSpeedServerboundPacket::toBuffer)
                .decoder(CameraSetShutterSpeedServerboundPacket::fromBuffer)
                .consumerMainThread(CameraSetShutterSpeedServerboundPacket::handle)
                .add();
    }

    public static void send(ShutterSpeed shutterSpeed) {
        Packets.sendToServer(new CameraSetShutterSpeedServerboundPacket(shutterSpeed));
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        shutterSpeed.toBuffer(buffer);
    }

    public static CameraSetShutterSpeedServerboundPacket fromBuffer(FriendlyByteBuf buffer) {
        return new CameraSetShutterSpeedServerboundPacket(ShutterSpeed.fromBuffer(buffer));
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        @Nullable ServerPlayer player = context.getSender();
        Preconditions.checkState(player != null, "Cannot handle packet: Player was null");

        CameraInHand camera = CameraInHand.getActive(player);
        if (!camera.isEmpty()) {
            camera.getItem().setShutterSpeed(camera.getStack(), shutterSpeed);
        }

        return true;
    }
}
