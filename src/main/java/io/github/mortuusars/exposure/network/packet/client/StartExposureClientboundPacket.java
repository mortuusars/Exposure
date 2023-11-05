package io.github.mortuusars.exposure.network.packet.client;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public record StartExposureClientboundPacket(@NotNull String exposureId, @NotNull InteractionHand activeHand, boolean flashHasFired) {
    public static void register(SimpleChannel channel, int id) {
        channel.messageBuilder(StartExposureClientboundPacket.class, id, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(StartExposureClientboundPacket::toBuffer)
                .decoder(StartExposureClientboundPacket::fromBuffer)
                .consumerMainThread(StartExposureClientboundPacket::handle)
                .add();
    }

    public static void send(ServerPlayer player, @NotNull String exposureId, @NotNull InteractionHand activeHand, boolean flashHasFired) {
        Packets.sendToClient(new StartExposureClientboundPacket(exposureId, activeHand, flashHasFired), player);
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        Preconditions.checkState(exposureId.length() > 0, "exposureId cannot be empty.");

        buffer.writeUtf(exposureId);
        buffer.writeEnum(activeHand);
        buffer.writeBoolean(flashHasFired);
    }

    public static StartExposureClientboundPacket fromBuffer(FriendlyByteBuf buffer) {
        return new StartExposureClientboundPacket(buffer.readUtf(), buffer.readEnum(InteractionHand.class), buffer.readBoolean());
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> contextSupplier.get().enqueueWork(() -> {
            ClientPacketsHandler.startExposure(this);
        }));

        return true;
    }
}
