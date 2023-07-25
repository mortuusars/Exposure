package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public record LoadExposureCommandClientboundPacket(String id, String path, int size, boolean dither) {
    public static void register(SimpleChannel channel, int id) {
        channel.messageBuilder(LoadExposureCommandClientboundPacket.class, id, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(LoadExposureCommandClientboundPacket::toBuffer)
                .decoder(LoadExposureCommandClientboundPacket::fromBuffer)
                .consumerMainThread(LoadExposureCommandClientboundPacket::handle)
                .add();
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(id);
        buffer.writeUtf(path);
        buffer.writeInt(size);
        buffer.writeBoolean(dither);
    }

    public static LoadExposureCommandClientboundPacket fromBuffer(FriendlyByteBuf buffer) {
        return new LoadExposureCommandClientboundPacket(buffer.readUtf(), buffer.readUtf(), buffer.readInt(), buffer.readBoolean());
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> contextSupplier.get().enqueueWork(
                () -> ClientPacketsHandler.loadExposure(id, path, size, dither)));

        return true;
    }
}
