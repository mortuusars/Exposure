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

public record SetItemInHandServerboundPacket(ItemStack itemStack, InteractionHand hand) {
    public static void register(SimpleChannel channel, int id) {
        channel.messageBuilder(SetItemInHandServerboundPacket.class, id, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SetItemInHandServerboundPacket::toBuffer)
                .decoder(SetItemInHandServerboundPacket::fromBuffer)
                .consumerMainThread(SetItemInHandServerboundPacket::handle)
                .add();
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeItemStack(itemStack, false);
        buffer.writeEnum(hand);
    }

    public static SetItemInHandServerboundPacket fromBuffer(FriendlyByteBuf buffer) {
        return new SetItemInHandServerboundPacket(buffer.readItem(), buffer.readEnum(InteractionHand.class));
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        @Nullable ServerPlayer player = context.getSender();

        if (player == null)
            throw new IllegalStateException("Cannot handle packet: Player is null.");

        player.setItemInHand(hand, itemStack);
        return true;
    }
}
