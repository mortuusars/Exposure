package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.PacketDirection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record ExposureDataPartPacket(String id, int width, int height, CompoundTag properties, int offset, byte[] partBytes) implements IPacket<ExposureDataPartPacket> {
    public static final ResourceLocation ID = Exposure.resource("exposure_data_part");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public FriendlyByteBuf toBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(id);
        buffer.writeInt(width);
        buffer.writeInt(height);
        buffer.writeNbt(properties);
        buffer.writeInt(offset);
        buffer.writeByteArray(partBytes);
        return buffer;
    }

    public static ExposureDataPartPacket fromBuffer(FriendlyByteBuf buffer) {
        return new ExposureDataPartPacket(buffer.readUtf(), buffer.readInt(), buffer.readInt(),
                buffer.readAnySizeNbt(), buffer.readInt(), buffer.readByteArray());
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        direction.getExposureReceiver().receivePart(id, width, height, properties, offset, partBytes);
        return true;
    }
}
