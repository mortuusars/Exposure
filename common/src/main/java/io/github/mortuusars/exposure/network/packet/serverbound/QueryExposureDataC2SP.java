package io.github.mortuusars.exposure.network.packet.serverbound;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record QueryExposureDataC2SP(ExposureIdentifier identifier) implements Packet {
    public static final Identifier ID = Exposure.resource("query_exposure_data");
    public static final CustomPacketPayload.Type<QueryExposureDataC2SP> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, QueryExposureDataC2SP> STREAM_CODEC = StreamCodec.composite(
            ExposureIdentifier.STREAM_CODEC, QueryExposureDataC2SP::identifier,
            QueryExposureDataC2SP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        Preconditions.checkArgument(player instanceof ServerPlayer, "Cannot handle packet: Player was is not available.");
//        PalettedExposure palettedExposure = ExposureServer.getExposure(identifier);
//        ExposureServer.exposureSender().sendTo(identifier, palettedExposure, ((ServerPlayer) player));
        return true;
    }
}
