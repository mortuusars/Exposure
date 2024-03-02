package io.github.mortuusars.exposure.network.packet.server;

import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.data.storage.ExposureSavedData;
import io.github.mortuusars.exposure.network.PacketDirection;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record QueryExposureDataC2SP(String id) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("query_exposure_data");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public FriendlyByteBuf toBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(id);
        return buffer;
    }

    public static QueryExposureDataC2SP fromBuffer(FriendlyByteBuf buffer) {
        return new QueryExposureDataC2SP(buffer.readUtf());
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        Preconditions.checkArgument(player != null, "Cannot handle QueryExposureDataPacket: Player was null");

        Optional<ExposureSavedData> exposureSavedData = ExposureServer.getExposureStorage().getOrQuery(id);

        if (exposureSavedData.isEmpty())
            LogUtils.getLogger().error("Cannot get exposure data with an id '" + id + "'. Result is null.");
        else {
            ExposureServer.getExposureSender().sendTo(player, id, exposureSavedData.get());
        }

        return true;
    }
}
