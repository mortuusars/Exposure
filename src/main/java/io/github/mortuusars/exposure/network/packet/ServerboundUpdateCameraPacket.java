package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.storage.ExposureSavedData;
import io.github.mortuusars.exposure.storage.ExposureStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public record ServerboundUpdateCameraPacket(String id, InteractionHand hand) {
    public void toBuffer(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(id);
        friendlyByteBuf.writeEnum(hand);
    }

    public static ServerboundUpdateCameraPacket fromBuffer(FriendlyByteBuf buffer) {
        return new ServerboundUpdateCameraPacket(buffer.readUtf(), buffer.readEnum(InteractionHand.class));
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        @Nullable ServerPlayer player = context.getSender();

        if (player == null)
            throw new IllegalStateException("Cannot handle UpdateCameraPacket: Player was null.");

        ItemStack itemInHand = player.getItemInHand(hand);
        if (!(itemInHand.getItem() instanceof CameraItem))
            throw new IllegalStateException("Cannot handle UpdateCameraPacket: Item in hand is not camera.");

        itemInHand.getOrCreateTag().putString("lastShot", id);

        return true;
    }
}
