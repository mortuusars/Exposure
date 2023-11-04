package io.github.mortuusars.exposure.network.packet;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.camera.infrastructure.CompositionGuide;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public record CameraSetCompositionGuideServerboundPacket(CompositionGuide guide) {
    public static void register(SimpleChannel channel, int id) {
        channel.messageBuilder(CameraSetCompositionGuideServerboundPacket.class, id, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CameraSetCompositionGuideServerboundPacket::toBuffer)
                .decoder(CameraSetCompositionGuideServerboundPacket::fromBuffer)
                .consumerMainThread(CameraSetCompositionGuideServerboundPacket::handle)
                .add();
    }

    public static void send(CompositionGuide guide) {
        Packets.sendToServer(new CameraSetCompositionGuideServerboundPacket(guide));
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        guide.toBuffer(buffer);
    }

    public static CameraSetCompositionGuideServerboundPacket fromBuffer(FriendlyByteBuf buffer) {
        return new CameraSetCompositionGuideServerboundPacket(CompositionGuide.fromBuffer(buffer));
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        @Nullable ServerPlayer player = context.getSender();
        Preconditions.checkState(player != null, "Cannot handle packet: Player was null");

        CameraInHand camera = CameraInHand.ofPlayer(player);
        if (!camera.isEmpty()) {
            camera.getItem().setCompositionGuide(camera.getStack(), guide);
        }

        return true;
    }
}
