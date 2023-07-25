package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.client.ClientOnlyLogic;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.UUID;
import java.util.function.Supplier;

public record PlayFilmAdvanceSoundClientboundPacket(UUID sourcePlayerId) {
    public static void register(SimpleChannel channel, int id) {
        channel.messageBuilder(PlayFilmAdvanceSoundClientboundPacket.class, id, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PlayFilmAdvanceSoundClientboundPacket::toBuffer)
                .decoder(PlayFilmAdvanceSoundClientboundPacket::fromBuffer)
                .consumerMainThread(PlayFilmAdvanceSoundClientboundPacket::handle)
                .add();
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeUUID(sourcePlayerId);
    }

    public static PlayFilmAdvanceSoundClientboundPacket fromBuffer(FriendlyByteBuf buffer) {
        return new PlayFilmAdvanceSoundClientboundPacket(buffer.readUUID());
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> contextSupplier.get().enqueueWork(
                () -> ClientPacketsHandler.playFilmAdvanceSound(this)));

        return true;
    }
}
