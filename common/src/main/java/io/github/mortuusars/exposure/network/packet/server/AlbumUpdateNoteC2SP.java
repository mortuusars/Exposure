package io.github.mortuusars.exposure.network.packet.server;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.PacketDirection;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record AlbumUpdateNoteC2SP(int pageIndex, String text) implements IPacket<AlbumUpdateNoteC2SP> {
    public static final ResourceLocation ID = Exposure.resource("album_update_note");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public static AlbumUpdateNoteC2SP fromBuffer(FriendlyByteBuf buffer) {
        return new AlbumUpdateNoteC2SP(buffer.readInt(), buffer.readInt(), buffer.readBoolean());
    }

    @Override
    public FriendlyByteBuf toBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(slotIndex);
        buffer.writeInt(pageIndex);
        buffer.writeBoolean(remove);
        return buffer;
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        Preconditions.checkState(player != null, "Cannot handle packet: Player was null");
        ServerPlayer serverPlayer = ((ServerPlayer) player);

//        ItemStack albumStack;
//        if (serverPlayer.getMainHandItem().getItem() instanceof AlbumItem)
//            albumStack = serverPlayer.getMainHandItem();
//        else if (serverPlayer.getOffhandItem().getItem() instanceof AlbumItem)
//            albumStack = serverPlayer.getOffhandItem();
//        else
//            throw new IllegalStateException("Cannot handle album packet: Player must have an album in mainhand/offhand");
//
//        AlbumItem album = ((AlbumItem) albumStack.getItem());
//
//        if (remove) {
//            ItemStack removedStack = album.setPhotoOnPage(albumStack, ItemStack.EMPTY, pageIndex);
//            boolean added = player.getInventory().add(removedStack);
//            if (!added)
//                player.drop(removedStack, true, false);
//        }
//        else {
//            ItemStack stack = serverPlayer.getInventory().removeItem(slotIndex, 64);
//            album.setPhotoOnPage(albumStack, stack, pageIndex);
//        }

        return true;
    }
}
