package io.github.mortuusars.exposure.network.packet.server;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.network.Packets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public record CameraSelfieModeServerboundPacket(InteractionHand hand, boolean isInSelfieMode, boolean effects) {
    public static void register(SimpleChannel channel, int id) {
        channel.messageBuilder(CameraSelfieModeServerboundPacket.class, id, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CameraSelfieModeServerboundPacket::toBuffer)
                .decoder(CameraSelfieModeServerboundPacket::fromBuffer)
                .consumerMainThread(CameraSelfieModeServerboundPacket::handle)
                .add();
    }

    public static void send(InteractionHand hand, boolean isInSelfieMode, boolean effects) {
        Packets.sendToServer(new CameraSelfieModeServerboundPacket(hand, isInSelfieMode, effects));
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeEnum(hand);
        buffer.writeBoolean(isInSelfieMode);
        buffer.writeBoolean(effects);
    }

    public static CameraSelfieModeServerboundPacket fromBuffer(FriendlyByteBuf buffer) {
        return new CameraSelfieModeServerboundPacket(buffer.readEnum(InteractionHand.class), buffer.readBoolean(), buffer.readBoolean());
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        @Nullable ServerPlayer player = context.getSender();
        Preconditions.checkState(player != null, "Cannot handle packet: Player was null");

        ItemStack itemInHand = player.getItemInHand(hand);
        if (!(itemInHand.getItem() instanceof CameraItem cameraItem))
            throw new IllegalStateException("Item in hand in not a Camera.");

        if (effects)
            cameraItem.setSelfieModeWithEffects(player, itemInHand, isInSelfieMode);
        else
            cameraItem.setSelfieMode(itemInHand, isInSelfieMode);

        return true;
    }
}