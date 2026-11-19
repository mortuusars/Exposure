package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.mortaar.network.packet.Packet;
import io.github.mortuusars.exposure.world.entity.stand.CameraStand;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record ClientboundCameraStandSetRotationsPacket(int entityId, float yRot, float xRot) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("camera_stand_set_rotations");
    public static final Type<ClientboundCameraStandSetRotationsPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ClientboundCameraStandSetRotationsPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ClientboundCameraStandSetRotationsPacket::entityId,
            ByteBufCodecs.FLOAT, ClientboundCameraStandSetRotationsPacket::yRot,
            ByteBufCodecs.FLOAT, ClientboundCameraStandSetRotationsPacket::xRot,
            ClientboundCameraStandSetRotationsPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow direction, Player player) {
        if (player.level().getEntity(entityId) instanceof CameraStand stand) {
            stand.setYRot(yRot);
            stand.setXRot(xRot);
        }
        return true;
    }
}