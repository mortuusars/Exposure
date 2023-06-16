package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.network.ExposureReceiver;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ExposureDataPartPacket(String id, int width, int height, int offset, byte[] partBytes) {
    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(id);
        buffer.writeInt(width);
        buffer.writeInt(height);
        buffer.writeInt(offset);
        buffer.writeByteArray(partBytes);
    }

    public static ExposureDataPartPacket fromBuffer(FriendlyByteBuf buffer) {
        return new ExposureDataPartPacket(buffer.readUtf(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readByteArray());
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        ExposureReceiver.receivePart(id, width, height, offset, partBytes);
        return true;
    }
}
