package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.mortaar.network.packet.Packet;
import io.github.mortuusars.exposure.world.entity.CameraOperator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record ClientboundActiveCameraRemovePacket(int operatorEntityId) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("active_camera_remove");
    public static final CustomPacketPayload.Type<ClientboundActiveCameraRemovePacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ClientboundActiveCameraRemovePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ClientboundActiveCameraRemovePacket::operatorEntityId,
            ClientboundActiveCameraRemovePacket::new
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
