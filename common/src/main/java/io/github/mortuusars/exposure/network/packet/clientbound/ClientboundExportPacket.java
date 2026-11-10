package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.data.export.ExportLook;
import io.github.mortuusars.exposure.data.export.ExportSize;
import io.github.mortuusars.exposure.client.task.ExportExposuresTask;
import io.github.mortuusars.mortaar.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ClientboundExportPacket(List<String> ids, ExportSize size, ExportLook look) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("export");
    public static final Type<ClientboundExportPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, ClientboundExportPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), ClientboundExportPacket::ids,
            ByteBufCodecs.VAR_INT.map(i -> ExportSize.values()[i], ExportSize::ordinal), ClientboundExportPacket::size,
            ByteBufCodecs.VAR_INT.map(i -> ExportLook.values()[i], ExportLook::ordinal), ClientboundExportPacket::look,
            ClientboundExportPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow direction, Player player) {
        Client.exportExposures(ids, size, look);
        return true;
    }

    public static class Client {
        public static void exportExposures(List<String> ids, ExportSize size, ExportLook look) {
            ExportExposuresTask.start(ids, size, look);
        }
    }
}
