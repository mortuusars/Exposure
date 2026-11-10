package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.capture.CaptureParameters;
import io.github.mortuusars.exposure.client.capture.template.CaptureTemplates;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.mortaar.network.packet.Packet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record ClientboundCaptureStartPacket(ResourceLocation templateId, CaptureParameters captureParameters) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("capture_start");
    public static final Type<ClientboundCaptureStartPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundCaptureStartPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ClientboundCaptureStartPacket::templateId,
            CaptureParameters.STREAM_CODEC, ClientboundCaptureStartPacket::captureParameters,
            ClientboundCaptureStartPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        Client.startCapture(templateId, captureParameters);
        return true;
    }

    public static class Client {
        public static void startCapture(ResourceLocation templateId, CaptureParameters captureParameters) {
            Task<?> captureTask = CaptureTemplates.getOrThrow(templateId).createTask(captureParameters);
            ExposureClient.cycles().enqueueTask(captureTask);
        }
    }
}
