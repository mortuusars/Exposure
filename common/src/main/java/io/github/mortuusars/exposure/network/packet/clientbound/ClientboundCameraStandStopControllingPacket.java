package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import io.github.mortuusars.mortaar.network.packet.Packet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record ClientboundCameraStandStopControllingPacket(int standId) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("camera_stand_stop_controlling");
    public static final Type<ClientboundCameraStandStopControllingPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundCameraStandStopControllingPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ClientboundCameraStandStopControllingPacket::standId,
            ClientboundCameraStandStopControllingPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        Client.stopControllingCameraStand(standId);
        return true;
    }

    public static class Client {
        public static void stopControllingCameraStand(int standId) {
            if (Minecrft.get().cameraEntity != Minecrft.playerOrThrow()
                    && Minecrft.levelOrThrow().getEntity(standId) instanceof CameraStandEntity stand) {
                stand.stopControlling();
                CameraClient.setCameraEntity(Minecrft.playerOrThrow());
            }
        }
    }
}
