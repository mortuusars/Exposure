package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.storage.ExposureSavedData;
import io.github.mortuusars.exposure.storage.ExposureStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public record ClientboundLoadExposureDataPacket(String id, ExposureSavedData exposureData) {
    public void toBuffer(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(id);
        friendlyByteBuf.writeNbt(exposureData.save(new CompoundTag()));
    }

    public static ClientboundLoadExposureDataPacket fromBuffer(FriendlyByteBuf buffer) {
        return new ClientboundLoadExposureDataPacket(buffer.readUtf(), ExposureSavedData.load(Objects.requireNonNull(buffer.readNbt())));
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean handle(Supplier<NetworkEvent.Context> ignoredContextSupplier) {
        ExposureStorage.set(id, exposureData);
        return true;
    }
}
