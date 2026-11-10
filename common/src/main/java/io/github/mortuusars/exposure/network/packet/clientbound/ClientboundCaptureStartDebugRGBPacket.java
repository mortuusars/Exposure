package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.capture.CaptureParameters;
import io.github.mortuusars.exposure.client.capture.template.CaptureTemplate;
import io.github.mortuusars.exposure.client.capture.template.CaptureTemplates;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.mortaar.network.packet.Packet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ClientboundCaptureStartDebugRGBPacket(ResourceLocation templateId, List<CaptureParameters> captureProperties) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("capture_start_debug_rgb");
    public static final Type<ClientboundCaptureStartDebugRGBPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundCaptureStartDebugRGBPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ClientboundCaptureStartDebugRGBPacket::templateId,
            CaptureParameters.STREAM_CODEC.apply(ByteBufCodecs.list(3)), ClientboundCaptureStartDebugRGBPacket::captureProperties,
            ClientboundCaptureStartDebugRGBPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        Client.startDebugRGBCapture(templateId, captureProperties);
        return true;
    }

    public static class Client {
        public static void startDebugRGBCapture(ResourceLocation templateId, List<CaptureParameters> captureProperties) {
            CaptureTemplate template = CaptureTemplates.getOrThrow(templateId);
            for (CaptureParameters captureParameters : captureProperties) {
                Task<?> captureTask = template.createTask(captureParameters);
                ExposureClient.cycles().enqueueTask(captureTask);
            }
        }
    }
}
