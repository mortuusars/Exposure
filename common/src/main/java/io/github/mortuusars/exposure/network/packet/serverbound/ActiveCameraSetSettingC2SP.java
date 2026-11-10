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

public record ActiveCameraSetSettingC2SP(CameraSetting<?> setting, byte[] encodedValue) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("active_camera_set_setting");
    public static final Type<ActiveCameraSetSettingC2SP> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ActiveCameraSetSettingC2SP> STREAM_CODEC = StreamCodec.composite(
            CameraSetting.STREAM_CODEC, ActiveCameraSetSettingC2SP::setting,
            ByteBufCodecs.byteArray(4096), ActiveCameraSetSettingC2SP::encodedValue,
            ActiveCameraSetSettingC2SP::new
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