package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record CameraStandSetRotationsS2CP(int entityId, float yRot, float xRot) implements Packet {
    public static final Identifier ID = Exposure.resource("camera_stand_set_rotations");
    public static final Type<CameraStandSetRotationsS2CP> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, CameraStandSetRotationsS2CP> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CameraStandSetRotationsS2CP::entityId,
            ByteBufCodecs.FLOAT, CameraStandSetRotationsS2CP::yRot,
            ByteBufCodecs.FLOAT, CameraStandSetRotationsS2CP::xRot,
            CameraStandSetRotationsS2CP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow direction, Player player) {
        if (player.level().getEntity(entityId) instanceof CameraStandEntity stand) {
            stand.setYRot(yRot);
            stand.setXRot(xRot);
        }
        return true;
    }
}