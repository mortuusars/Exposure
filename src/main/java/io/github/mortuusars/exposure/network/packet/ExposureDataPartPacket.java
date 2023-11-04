package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.camera.infrastructure.FilmType;
import io.github.mortuusars.exposure.network.ExposureReceiver;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public record ExposureDataPartPacket(String id, int width, int height, FilmType filmType, int offset, byte[] partBytes) {
    public static void register(SimpleChannel channel, int id) {
        channel.messageBuilder(ExposureDataPartPacket.class, id)
                .encoder(ExposureDataPartPacket::toBuffer)
                .decoder(ExposureDataPartPacket::fromBuffer)
                .consumerMainThread(ExposureDataPartPacket::handle)
                .add();
    }
    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(id);
        buffer.writeInt(width);
        buffer.writeInt(height);
        buffer.writeEnum(filmType);
        buffer.writeInt(offset);
        buffer.writeByteArray(partBytes);
    }

    public static ExposureDataPartPacket fromBuffer(FriendlyByteBuf buffer) {
        return new ExposureDataPartPacket(buffer.readUtf(), buffer.readInt(), buffer.readInt(), buffer.readEnum(FilmType.class), buffer.readInt(), buffer.readByteArray());
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        ExposureReceiver.receivePart(id, width, height, filmType, offset, partBytes);
        return true;
    }
}
