package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public record ShowExposureClientboundPacket(String exposureId, boolean negative) {
    public static void register(SimpleChannel channel, int id) {
        channel.messageBuilder(ShowExposureClientboundPacket.class, id, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ShowExposureClientboundPacket::toBuffer)
                .decoder(ShowExposureClientboundPacket::fromBuffer)
                .consumerMainThread(ShowExposureClientboundPacket::handle)
                .add();
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(exposureId);
        buffer.writeBoolean(negative);
    }

    public static ShowExposureClientboundPacket fromBuffer(FriendlyByteBuf buffer) {
        return new ShowExposureClientboundPacket(buffer.readUtf(), buffer.readBoolean());
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                contextSupplier.get().enqueueWork(() -> ClientPacketsHandler.showExposure(this)));

        return true;
    }
}
