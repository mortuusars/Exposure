package io.github.mortuusars.exposure.network.packet;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.ExposureSender;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.storage.ExposureSavedData;
import io.github.mortuusars.exposure.storage.ServersideExposureStorage;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public record ServerboundCameraSetZoomPacket(float focalLength) {
    public static void register(SimpleChannel channel, int id) {
        channel.messageBuilder(ServerboundCameraSetZoomPacket.class, id, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundCameraSetZoomPacket::toBuffer)
                .decoder(ServerboundCameraSetZoomPacket::fromBuffer)
                .consumerMainThread(ServerboundCameraSetZoomPacket::handle)
                .add();
    }

    public static void send(float focalLength) {
        Packets.sendToServer(new ServerboundCameraSetZoomPacket(focalLength));
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeFloat(focalLength);
    }

    public static ServerboundCameraSetZoomPacket fromBuffer(FriendlyByteBuf buffer) {
        return new ServerboundCameraSetZoomPacket(buffer.readFloat());
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        @Nullable ServerPlayer player = context.getSender();
        Preconditions.checkState(player != null, "Cannot handle packet: Player was null");

        CameraInHand camera = Exposure.getCamera().getCameraInHand(player);
        if (!camera.isEmpty()) {
            camera.getItem().setZoom(camera.getStack(), focalLength);
        }

        return true;
    }
}
