package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.mortaar.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record ClientboundExposureDataChangedPacket(String id) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("exposure_data_changed");
    public static final CustomPacketPayload.Type<ClientboundExposureDataChangedPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ClientboundExposureDataChangedPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ClientboundExposureDataChangedPacket::id,
            ClientboundExposureDataChangedPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        Client.exposureDataChanged(id);
        return true;
    }

    public static class Client {
        public static void exposureDataChanged(String id) {
            ExposureClient.exposureStore().refresh(id);
            ExposureClient.imageRenderer().clearCacheOf(id);
            ExposureClient.renderedExposures().clearCacheOf(id);
        }
    }
}
