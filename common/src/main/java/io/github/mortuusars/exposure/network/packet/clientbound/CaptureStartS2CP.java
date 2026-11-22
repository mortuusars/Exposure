package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.capture.CaptureParameters;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record CaptureStartS2CP(Identifier templateId, CaptureParameters captureParameters) implements Packet {
    public static final Identifier ID = Exposure.resource("capture_start");
    public static final Type<CaptureStartS2CP> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, CaptureStartS2CP> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, CaptureStartS2CP::templateId,
            CaptureParameters.STREAM_CODEC, CaptureStartS2CP::captureParameters,
            CaptureStartS2CP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ClientPacketsHandler.startCapture(this);
        return true;
    }
}
