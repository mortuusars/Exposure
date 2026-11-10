package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.task.ExportExposuresTask;
import io.github.mortuusars.mortaar.client.Minecrft;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import io.github.mortuusars.mortaar.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class ClientboundExportStopPacket implements Packet {
    public static final ClientboundExportStopPacket INSTANCE = new ClientboundExportStopPacket();

    public static final ResourceLocation ID = Exposure.resource("export_stop");
    public static final Type<ClientboundExportStopPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, ClientboundExportStopPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private ClientboundExportStopPacket() { }

    @Override

    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow direction, Player player) {
        Client.stopExportTask();
        return true;
    }

    public static class Client {
        public static void stopExportTask() {
            if (!ExportExposuresTask.stopCurrentTask()) {
                Minecrft.playerOrThrow().displayClientMessage(Component.translatable("task.exposure.export.not_running")
                        .withStyle(ChatFormatting.RED), false);
            }
        }
    }
}
