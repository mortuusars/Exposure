package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.world.level.storage.RequestedPalettedExposure;
import io.github.mortuusars.mortaar.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record ClientboundExposureDataResponsePacket(String id, RequestedPalettedExposure result) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("exposure_data_response");
    public static final Type<ClientboundExposureDataResponsePacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ClientboundExposureDataResponsePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ClientboundExposureDataResponsePacket::id,
            RequestedPalettedExposure.STREAM_CODEC, ClientboundExposureDataResponsePacket::result,
            ClientboundExposureDataResponsePacket::new
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
