package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.world.camera.CameraId;
import io.github.mortuusars.exposure.world.camera.CameraInHand;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.entity.CameraOperator;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record ActiveCameraInHandSetS2CP(int operatorEntityId, CameraId cameraId, InteractionHand hand) implements Packet {
    public static final Identifier ID = Exposure.resource("active_camera_in_hand_set");
    public static final Type<ActiveCameraInHandSetS2CP> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ActiveCameraInHandSetS2CP> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ActiveCameraInHandSetS2CP::operatorEntityId,
            CameraId.STREAM_CODEC, ActiveCameraInHandSetS2CP::cameraId,
            ByteBufCodecs.VAR_INT.map(i -> InteractionHand.values()[i], InteractionHand::ordinal), ActiveCameraInHandSetS2CP::hand,
            ActiveCameraInHandSetS2CP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        if (player.level().getEntity(operatorEntityId) instanceof LivingEntity entity
                && entity instanceof CameraOperator operator
                && entity instanceof CameraHolder holder) {
            operator.setActiveExposureCamera(new CameraInHand(holder, cameraId, hand));
        }
        return true;
    }
}
