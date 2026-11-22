package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class ExportStopS2CP implements Packet {
    public static final ExportStopS2CP INSTANCE = new ExportStopS2CP();

    public static final Identifier ID = Exposure.resource("export_stop");
    public static final Type<ExportStopS2CP> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, ExportStopS2CP> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private ExportStopS2CP() { }

    @Override

    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow direction, Player player) {
        ClientPacketsHandler.stopExportTask();
        return true;
    }
}
