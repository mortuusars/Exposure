package io.github.mortuusars.exposure.network.packet.common;

import io.github.mortuusars.exposure.world.entity.CameraOperator;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class ActiveCameraDeactivateCommonPacket implements Packet {
    public static final ActiveCameraDeactivateCommonPacket INSTANCE = new ActiveCameraDeactivateCommonPacket();

    public static final Identifier ID = Exposure.resource("active_camera_deactivate");
    public static final Type<ActiveCameraDeactivateCommonPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ActiveCameraDeactivateCommonPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private ActiveCameraDeactivateCommonPacket() {
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ((CameraOperator) player).getActiveExposureCameraOptional().ifPresent(camera -> {
            camera.map((item, stack) -> item.deactivate(camera.getHolder().asHolderEntity(), stack));
            ((CameraOperator) player).removeActiveExposureCamera();
        });
        return true;
    }
}
