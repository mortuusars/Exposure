package io.github.mortuusars.exposure.network.packet.server;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.camera.infrastructure.CompositionGuide;
import io.github.mortuusars.exposure.network.PacketDirection;
import io.github.mortuusars.exposure.network.packet.IPacket;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record CameraSetCompositionGuideC2SP(CompositionGuide guide) implements IPacket<CameraSetCompositionGuideC2SP> {
    public void toBuffer(FriendlyByteBuf buffer) {
        guide.toBuffer(buffer);
    }

    public static CameraSetCompositionGuideC2SP fromBuffer(FriendlyByteBuf buffer) {
        return new CameraSetCompositionGuideC2SP(CompositionGuide.fromBuffer(buffer));
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        Preconditions.checkState(player != null, "Cannot handle packet: Player was null");

        CameraInHand camera = CameraInHand.getActive(player);
        if (!camera.isEmpty()) {
            camera.getItem().setCompositionGuide(camera.getStack(), guide);
        }

        return true;
    }
}
