package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.storage.ExposureSavedData;
import io.github.mortuusars.exposure.storage.ExposureStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

public record ServerboundSaveExposurePacket(String id, ExposureSavedData exposureData) {
    public void toBuffer(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(id);
        friendlyByteBuf.writeNbt(exposureData.save(new CompoundTag()));
    }

    public static ServerboundSaveExposurePacket fromBuffer(FriendlyByteBuf buffer) {
        return new ServerboundSaveExposurePacket(buffer.readUtf(), ExposureSavedData.load(Objects.requireNonNull(buffer.readNbt())));
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        @Nullable ServerPlayer player = context.getSender();

        if (player == null)
            throw new IllegalStateException("Cannot handle ExposureSavePacket: Player was null");

        exposureData.setDirty();
        Objects.requireNonNull(player.getServer()).overworld().getDataStorage().set(ExposureStorage.getSaveNameFromId(id), exposureData);

        return true;
    }
}
