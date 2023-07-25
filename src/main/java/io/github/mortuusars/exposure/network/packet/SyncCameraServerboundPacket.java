package io.github.mortuusars.exposure.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public record SyncCameraServerboundPacket(ItemStack cameraStack, InteractionHand hand) {
    public static void register(SimpleChannel channel, int id) {
        channel.messageBuilder(SyncCameraServerboundPacket.class, id, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SyncCameraServerboundPacket::toBuffer)
                .decoder(SyncCameraServerboundPacket::fromBuffer)
                .consumerMainThread(SyncCameraServerboundPacket::handle)
                .add();
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeItemStack(cameraStack, false);
        buffer.writeEnum(hand);
    }

    public static SyncCameraServerboundPacket fromBuffer(FriendlyByteBuf buffer) {
        return new SyncCameraServerboundPacket(buffer.readItem(), buffer.readEnum(InteractionHand.class));
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
