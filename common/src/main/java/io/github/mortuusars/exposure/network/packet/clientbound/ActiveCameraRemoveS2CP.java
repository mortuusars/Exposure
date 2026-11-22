package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.world.entity.CameraOperator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record ActiveCameraRemoveS2CP(int operatorEntityId) implements Packet {
    public static final Identifier ID = Exposure.resource("active_camera_remove");
    public static final CustomPacketPayload.Type<ActiveCameraRemoveS2CP> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ActiveCameraRemoveS2CP> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ActiveCameraRemoveS2CP::operatorEntityId,
            ActiveCameraRemoveS2CP::new
    );


    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        if (player.level().getEntity(operatorEntityId) instanceof CameraOperator operator) {
            operator.removeActiveExposureCamera();
        }
        return true;
    }
}
