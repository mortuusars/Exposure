package io.github.mortuusars.exposure.network.packet;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.network.Packets;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public record CameraInHandAddFrameServerboundPacket(InteractionHand hand, CompoundTag frame) {
    public static void register(SimpleChannel channel, int id) {
        channel.messageBuilder(CameraInHandAddFrameServerboundPacket.class, id, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CameraInHandAddFrameServerboundPacket::toBuffer)
                .decoder(CameraInHandAddFrameServerboundPacket::fromBuffer)
                .consumerMainThread(CameraInHandAddFrameServerboundPacket::handle)
                .add();
    }

    public static void send(InteractionHand hand, CompoundTag frame) {
        Packets.sendToServer(new CameraInHandAddFrameServerboundPacket(hand, frame));
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeEnum(hand);
        buffer.writeNbt(frame);
    }

    public static CameraInHandAddFrameServerboundPacket fromBuffer(FriendlyByteBuf buffer) {
        InteractionHand hand = buffer.readEnum(InteractionHand.class);
        @Nullable CompoundTag frame = buffer.readAnySizeNbt();
        if (frame == null)
            frame = new CompoundTag();
        return new CameraInHandAddFrameServerboundPacket(hand, frame);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        @Nullable ServerPlayer player = context.getSender();
        Preconditions.checkState(player != null, "Cannot handle packet: Player was null");

        ItemStack itemInHand = player.getItemInHand(hand);
        if (!(itemInHand.getItem() instanceof CameraItem cameraItem))
            throw new IllegalStateException("Item in hand in not a Camera.");

        cameraItem.exposeFilmFrame(itemInHand, frame);
        return true;
    }
}
