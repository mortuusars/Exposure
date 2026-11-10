package io.github.mortuusars.exposure.network.packet.serverbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.CameraId;
import io.github.mortuusars.mortaar.network.packet.Packet;
import io.github.mortuusars.exposure.server.CameraInstances;
import io.github.mortuusars.exposure.util.TranslatableError;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record ServerboundInterplanarProjectionFinishedPacket(CameraId cameraId,
                                                boolean successful,
                                                Optional<TranslatableError> error) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("interplanar_projection_finished");
    public static final Type<ServerboundInterplanarProjectionFinishedPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ServerboundInterplanarProjectionFinishedPacket> STREAM_CODEC = StreamCodec.composite(
            CameraId.STREAM_CODEC, ServerboundInterplanarProjectionFinishedPacket::cameraId,
            ByteBufCodecs.BOOL, ServerboundInterplanarProjectionFinishedPacket::successful,
            ByteBufCodecs.optional(TranslatableError.STREAM_CODEC), ServerboundInterplanarProjectionFinishedPacket::error,
            ServerboundInterplanarProjectionFinishedPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        CameraInstances.ifPresent(cameraId, cameraInstance -> cameraInstance.setProjectionResult(player.level(), successful, error));
        return true;
    }
}
