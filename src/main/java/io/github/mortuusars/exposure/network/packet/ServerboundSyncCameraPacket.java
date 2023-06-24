package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.ExposureFrame;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.item.attachment.CameraAttachments;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public record ServerboundSyncCameraPacket(ItemStack cameraStack, InteractionHand hand) {
    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeItemStack(cameraStack, false);
        buffer.writeEnum(hand);
    }

    public static ServerboundSyncCameraPacket fromBuffer(FriendlyByteBuf buffer) {
        return new ServerboundSyncCameraPacket(buffer.readItem(), buffer.readEnum(InteractionHand.class));
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        @Nullable ServerPlayer player = context.getSender();

        if (player == null)
            throw new IllegalStateException("Cannot handle SyncCameraPacket: Player is null.");

        player.setItemInHand(hand, cameraStack);
        return true;
    }
}
