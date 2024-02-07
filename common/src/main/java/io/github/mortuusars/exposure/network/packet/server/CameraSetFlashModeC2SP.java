package io.github.mortuusars.exposure.network.packet.server;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.infrastructure.FlashMode;
import io.github.mortuusars.exposure.network.PacketDirection;
import io.github.mortuusars.exposure.network.packet.IPacket;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record CameraSetFlashModeC2SP(FlashMode flashMode) implements IPacket<CameraSetFlashModeC2SP> {
    public static final ResourceLocation ID = Exposure.resource("camera_set_flash_mode");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public FriendlyByteBuf toBuffer(FriendlyByteBuf buffer) {
        flashMode.toBuffer(buffer);
        return buffer;
    }

    public static CameraSetFlashModeC2SP fromBuffer(FriendlyByteBuf buffer) {
        return new CameraSetFlashModeC2SP(FlashMode.fromBuffer(buffer));
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        Preconditions.checkState(player != null, "Cannot handle packet: Player was null");

        CameraInHand camera = CameraInHand.getActive(player);
        if (!camera.isEmpty()) {
            camera.getItem().setFlashMode(camera.getStack(), flashMode);
        }

        return true;
    }
}
