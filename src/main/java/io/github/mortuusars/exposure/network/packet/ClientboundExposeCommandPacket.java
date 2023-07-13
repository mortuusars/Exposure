package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public record ClientboundExposeCommandPacket(int width, int height, boolean storeOnServer) {
    public static void register(SimpleChannel channel, int id) {
        channel.messageBuilder(ClientboundExposeCommandPacket.class, id, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundExposeCommandPacket::toBuffer)
                .decoder(ClientboundExposeCommandPacket::fromBuffer)
                .consumerMainThread(ClientboundExposeCommandPacket::handle)
                .add();
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(width);
        buffer.writeInt(height);
        buffer.writeBoolean(storeOnServer);
    }

    public static ClientboundExposeCommandPacket fromBuffer(FriendlyByteBuf buffer) {
        return new ClientboundExposeCommandPacket(buffer.readInt(), buffer.readInt(), buffer.readBoolean());
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> contextSupplier.get().enqueueWork(
                () -> ClientPacketsHandler.exposeScreenshot(width, height, storeOnServer)));

        return true;
    }
}
