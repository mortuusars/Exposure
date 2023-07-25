package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.handler.ServerPacketsHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.UUID;
import java.util.function.Supplier;

public record UpdateActiveCameraPacket(UUID playerID, boolean isActive, InteractionHand hand) {
    public static void register(SimpleChannel channel, int id) {
        channel.messageBuilder(UpdateActiveCameraPacket.class, id)
                .encoder(UpdateActiveCameraPacket::toBuffer)
                .decoder(UpdateActiveCameraPacket::fromBuffer)
                .consumerMainThread(UpdateActiveCameraPacket::handle)
                .add();
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerID);
        buffer.writeBoolean(isActive);
        buffer.writeEnum(hand);
    }

    public static UpdateActiveCameraPacket fromBuffer(FriendlyByteBuf buffer) {
        return new UpdateActiveCameraPacket(buffer.readUUID(), buffer.readBoolean(), buffer.readEnum(InteractionHand.class));
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection() == NetworkDirection.PLAY_TO_SERVER)
                ServerPacketsHandler.updateActiveCamera(context, this);
            else if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT)
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketsHandler.updateActiveCamera(context, this));
        });
    }
}
