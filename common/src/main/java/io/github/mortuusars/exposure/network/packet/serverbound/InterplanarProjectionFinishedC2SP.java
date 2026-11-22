package io.github.mortuusars.exposure.network.packet.serverbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.CameraId;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.server.CameraInstances;
import io.github.mortuusars.exposure.util.TranslatableError;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record InterplanarProjectionFinishedC2SP(CameraId cameraId,
                                                boolean successful,
                                                Optional<TranslatableError> error) implements Packet {
    public static final Identifier ID = Exposure.resource("interplanar_projection_finished");
    public static final Type<InterplanarProjectionFinishedC2SP> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, InterplanarProjectionFinishedC2SP> STREAM_CODEC = StreamCodec.composite(
            CameraId.STREAM_CODEC, InterplanarProjectionFinishedC2SP::cameraId,
            ByteBufCodecs.BOOL, InterplanarProjectionFinishedC2SP::successful,
            ByteBufCodecs.optional(TranslatableError.STREAM_CODEC), InterplanarProjectionFinishedC2SP::error,
            InterplanarProjectionFinishedC2SP::new
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
