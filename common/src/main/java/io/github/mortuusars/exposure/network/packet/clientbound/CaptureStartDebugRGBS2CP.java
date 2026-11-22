package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.capture.CaptureParameters;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record CaptureStartDebugRGBS2CP(Identifier templateId, List<CaptureParameters> captureProperties) implements Packet {
    public static final Identifier ID = Exposure.resource("capture_start_debug_rgb");
    public static final Type<CaptureStartDebugRGBS2CP> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, CaptureStartDebugRGBS2CP> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, CaptureStartDebugRGBS2CP::templateId,
            CaptureParameters.STREAM_CODEC.apply(ByteBufCodecs.list(3)), CaptureStartDebugRGBS2CP::captureProperties,
            CaptureStartDebugRGBS2CP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ClientPacketsHandler.startDebugRGBCapture(this);
        return true;
    }
}
