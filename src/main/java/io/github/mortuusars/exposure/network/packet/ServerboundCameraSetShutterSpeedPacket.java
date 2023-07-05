package io.github.mortuusars.exposure.network.packet;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public record ServerboundCameraSetShutterSpeedPacket(ShutterSpeed shutterSpeed) {
    public static void register(SimpleChannel channel, int id) {
        channel.messageBuilder(ServerboundCameraSetShutterSpeedPacket.class, id, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundCameraSetShutterSpeedPacket::toBuffer)
                .decoder(ServerboundCameraSetShutterSpeedPacket::fromBuffer)
                .consumerMainThread(ServerboundCameraSetShutterSpeedPacket::handle)
                .add();
    }

    public static void send(ShutterSpeed shutterSpeed) {
        Packets.sendToServer(new ServerboundCameraSetShutterSpeedPacket(shutterSpeed));
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        shutterSpeed.toBuffer(buffer);
    }

    public static ServerboundCameraSetShutterSpeedPacket fromBuffer(FriendlyByteBuf buffer) {
        return new ServerboundCameraSetShutterSpeedPacket(ShutterSpeed.fromBuffer(buffer));
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        @Nullable ServerPlayer player = context.getSender();
        Preconditions.checkState(player != null, "Cannot handle packet: Player was null");

        CameraInHand camera = Exposure.getCamera().getCameraInHand(player);
        if (!camera.isEmpty()) {
            camera.getItem().setShutterSpeed(camera.getStack(), shutterSpeed);
        }

        return true;
    }
}
