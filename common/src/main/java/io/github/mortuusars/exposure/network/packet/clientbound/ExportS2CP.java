package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.data.export.ExportLook;
import io.github.mortuusars.exposure.data.export.ExportSize;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ExportS2CP(List<String> ids, ExportSize size, ExportLook look) implements Packet {
    public static final Identifier ID = Exposure.resource("export");
    public static final Type<ExportS2CP> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, ExportS2CP> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), ExportS2CP::ids,
            ByteBufCodecs.VAR_INT.map(i -> ExportSize.values()[i], ExportSize::ordinal), ExportS2CP::size,
            ByteBufCodecs.VAR_INT.map(i -> ExportLook.values()[i], ExportLook::ordinal), ExportS2CP::look,
            ExportS2CP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow direction, Player player) {
        ClientPacketsHandler.exportExposures(this);
        return true;
    }
}
