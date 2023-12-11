package io.github.mortuusars.exposure.network.packet.server;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.network.PacketDirection;
import io.github.mortuusars.exposure.network.packet.IPacket;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record CameraSetZoomC2SP(double focalLength) implements IPacket<CameraSetZoomC2SP> {
    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeDouble(focalLength);
    }

    public static CameraSetZoomC2SP fromBuffer(FriendlyByteBuf buffer) {
        return new CameraSetZoomC2SP(buffer.readDouble());
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        Preconditions.checkState(player != null, "Cannot handle packet: Player was null");

        CameraInHand camera = CameraInHand.getActive(player);
        if (!camera.isEmpty()) {
            camera.getItem().setZoom(camera.getStack(), focalLength);
        }

        return true;
    }
}
