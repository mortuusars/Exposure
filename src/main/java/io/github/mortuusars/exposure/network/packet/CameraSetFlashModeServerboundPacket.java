package io.github.mortuusars.exposure.network.packet;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.camera.component.FlashMode;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public record CameraSetFlashModeServerboundPacket(FlashMode flashMode) {
    public static void register(SimpleChannel channel, int id) {
        channel.messageBuilder(CameraSetFlashModeServerboundPacket.class, id, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CameraSetFlashModeServerboundPacket::toBuffer)
                .decoder(CameraSetFlashModeServerboundPacket::fromBuffer)
                .consumerMainThread(CameraSetFlashModeServerboundPacket::handle)
                .add();
    }

    public static void send(FlashMode flashMode) {
        Packets.sendToServer(new CameraSetFlashModeServerboundPacket(flashMode));
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        flashMode.toBuffer(buffer);
    }

    public static CameraSetFlashModeServerboundPacket fromBuffer(FriendlyByteBuf buffer) {
        return new CameraSetFlashModeServerboundPacket(FlashMode.fromBuffer(buffer));
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        @Nullable ServerPlayer player = context.getSender();
        Preconditions.checkState(player != null, "Cannot handle packet: Player was null");

        CameraInHand camera = CameraInHand.ofPlayer(player);
        if (!camera.isEmpty()) {
            camera.getItem().setFlashMode(camera.getStack(), flashMode);
        }

        return true;
    }
}
