package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.world.camera.CameraId;
import io.github.mortuusars.exposure.world.camera.CameraOnStand;
import io.github.mortuusars.exposure.world.entity.CameraOperator;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record ActiveCameraOnStandSetS2CP(int operatorEntityId, int cameraStandId, CameraId cameraId) implements Packet {
    public static final Identifier ID = Exposure.resource("active_camera_on_stand_set");
    public static final Type<ActiveCameraOnStandSetS2CP> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ActiveCameraOnStandSetS2CP> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ActiveCameraOnStandSetS2CP::operatorEntityId,
            CameraId.STREAM_CODEC, ActiveCameraOnStandSetS2CP::cameraId,
            ByteBufCodecs.VAR_INT, ActiveCameraOnStandSetS2CP::cameraStandId,
            (operatorEntityId1, cameraId1, cameraStandId1) -> new ActiveCameraOnStandSetS2CP(operatorEntityId1, cameraStandId1, cameraId1)
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        if (player.level().getEntity(operatorEntityId) instanceof CameraOperator operator
                && player.level().getEntity(cameraStandId) instanceof CameraStandEntity cameraStand) {
            operator.setActiveExposureCamera(new CameraOnStand(operator, cameraStand, cameraId));
        }
        return true;
    }
}
