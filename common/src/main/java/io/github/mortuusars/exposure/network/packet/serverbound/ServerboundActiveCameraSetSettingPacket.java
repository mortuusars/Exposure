package io.github.mortuusars.exposure.network.packet.serverbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.camera.CameraOnStand;
import io.github.mortuusars.exposure.world.item.camera.CameraSetting;
import io.github.mortuusars.mortaar.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ServerboundActiveCameraSetSettingPacket(CameraSetting<?> setting, byte[] encodedValue) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("active_camera_set_setting");
    public static final Type<ServerboundActiveCameraSetSettingPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ServerboundActiveCameraSetSettingPacket> STREAM_CODEC = StreamCodec.composite(
            CameraSetting.STREAM_CODEC, ServerboundActiveCameraSetSettingPacket::setting,
            ByteBufCodecs.byteArray(4096), ServerboundActiveCameraSetSettingPacket::encodedValue,
            ServerboundActiveCameraSetSettingPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow direction, Player player) {
        @Nullable Camera camera = player.getActiveExposureCamera();
        if (camera == null || camera.isEmpty()) return false;

        setting.decodeAndSet(camera.getHolder(), camera.getItemStack(), player.registryAccess(), encodedValue);
        if (camera instanceof CameraOnStand cameraOnStand) {
            cameraOnStand.getStand().forceUpdate();
        }
        return true;
    }
}