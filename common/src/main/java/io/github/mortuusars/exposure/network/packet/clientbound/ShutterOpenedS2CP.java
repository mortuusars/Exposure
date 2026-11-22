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

public class ShutterOpenedS2CP implements Packet {
    public static final ShutterOpenedS2CP INSTANCE = new ShutterOpenedS2CP();

    public static final Identifier ID = Exposure.resource("shutter_opened");
    public static final Type<ShutterOpenedS2CP> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ShutterOpenedS2CP> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private ShutterOpenedS2CP() { }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ClientPacketsHandler.shutterOpened();
        return true;
    }
}