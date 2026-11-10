package io.github.mortuusars.exposure.network.packet.common;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.mortaar.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class CommonActiveCameraDeactivatePacket implements Packet {
    public static final CommonActiveCameraDeactivatePacket INSTANCE = new CommonActiveCameraDeactivatePacket();

    public static final ResourceLocation ID = Exposure.resource("active_camera_deactivate");
    public static final Type<CommonActiveCameraDeactivatePacket> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, CommonActiveCameraDeactivatePacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private CommonActiveCameraDeactivatePacket() {
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        player.getActiveExposureCameraOptional().ifPresent(camera -> {
            camera.map((item, stack) -> item.deactivate(player, camera.getHolder().asHolderEntity(), stack));
            player.removeActiveExposureCamera();
        });
        return true;
    }
}
