package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.ExposureSender;
import io.github.mortuusars.exposure.storage.ExposureSavedData;
import io.github.mortuusars.exposure.storage.ServersideExposureStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

public record ServerboundQueryExposureDataPacket(String id) {
    public void toBuffer(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(id);
    }

    public static ServerboundQueryExposureDataPacket fromBuffer(FriendlyByteBuf buffer) {
        return new ServerboundQueryExposureDataPacket(buffer.readUtf());
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        @Nullable ServerPlayer player = context.getSender();

        if (player == null)
            throw new IllegalStateException("Cannot handle QueryExposureDataPacket: Player was null");

        Optional<ExposureSavedData> exposureSavedData = new ServersideExposureStorage().getOrQuery(id);

        if (exposureSavedData.isEmpty())
            Exposure.LOGGER.error("Cannot get exposure data with an id '" + id + "'. Result is null.");
        else {
            ExposureSender.sendToClient(id, exposureSavedData.get(), player);
        }

        return true;
    }
}
