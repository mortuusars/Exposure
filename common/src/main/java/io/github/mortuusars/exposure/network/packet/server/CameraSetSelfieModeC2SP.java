package io.github.mortuusars.exposure.network.packet.server;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.network.PacketDirection;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record CameraSetSelfieModeC2SP(InteractionHand hand, boolean isInSelfieMode, boolean effects) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("camera_set_selfie_mode");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public FriendlyByteBuf toBuffer(FriendlyByteBuf buffer) {
        buffer.writeEnum(hand);
        buffer.writeBoolean(isInSelfieMode);
        buffer.writeBoolean(effects);
        return buffer;
    }

    public static CameraSetSelfieModeC2SP fromBuffer(FriendlyByteBuf buffer) {
        return new CameraSetSelfieModeC2SP(buffer.readEnum(InteractionHand.class), buffer.readBoolean(), buffer.readBoolean());
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
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