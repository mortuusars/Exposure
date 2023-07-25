package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.ExposureSender;
import io.github.mortuusars.exposure.storage.ExposureSavedData;
import io.github.mortuusars.exposure.storage.ServersideExposureStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

public record QueryExposureDataServerboundPacket(String id) {
    public static void register(SimpleChannel channel, int id) {
        channel.messageBuilder(QueryExposureDataServerboundPacket.class, id, NetworkDirection.PLAY_TO_SERVER)
                .encoder(QueryExposureDataServerboundPacket::toBuffer)
                .decoder(QueryExposureDataServerboundPacket::fromBuffer)
                .consumerMainThread(QueryExposureDataServerboundPacket::handle)
                .add();
    }

    public void toBuffer(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(id);
    }

    public static QueryExposureDataServerboundPacket fromBuffer(FriendlyByteBuf buffer) {
        return new QueryExposureDataServerboundPacket(buffer.readUtf());
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
