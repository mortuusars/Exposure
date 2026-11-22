package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.world.level.storage.RequestedPalettedExposure;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record ExposureDataResponseS2CP(String id, RequestedPalettedExposure result) implements Packet {
    public static final Identifier ID = Exposure.resource("exposure_data_response");
    public static final Type<ExposureDataResponseS2CP> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ExposureDataResponseS2CP> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ExposureDataResponseS2CP::id,
            RequestedPalettedExposure.STREAM_CODEC, ExposureDataResponseS2CP::result,
            ExposureDataResponseS2CP::new
    );

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ExposureClient.exposureStore().receive(id, result);
        return true;
    }
}
