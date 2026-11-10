package io.github.mortuusars.exposure.network.packet.serverbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import io.github.mortuusars.mortaar.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record ServerboundExposureDataPacket(String id, ExposureData exposure) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("exposure_data");
    public static final Type<ServerboundExposureDataPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ServerboundExposureDataPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ServerboundExposureDataPacket::id,
            ExposureData.STREAM_CODEC, ServerboundExposureDataPacket::exposure,
            ServerboundExposureDataPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ExposureServer.exposureRepository().receiveClientUpload(((ServerPlayer) player), id, exposure);
        return true;
    }
}
