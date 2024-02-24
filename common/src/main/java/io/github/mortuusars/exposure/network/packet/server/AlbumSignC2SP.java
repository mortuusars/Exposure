package io.github.mortuusars.exposure.network.packet.server;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.item.AlbumItem;
import io.github.mortuusars.exposure.network.PacketDirection;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record AlbumSignC2SP(String title) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("album_sign");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public static AlbumSignC2SP fromBuffer(FriendlyByteBuf buffer) {
        return new AlbumSignC2SP(buffer.readUtf());
    }

    @Override
    public FriendlyByteBuf toBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(title);
        return buffer;
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        Preconditions.checkState(player != null, "Cannot handle packet: Player was null");

        InteractionHand hand;
        if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof AlbumItem albumItem && albumItem.isEditable())
            hand = InteractionHand.MAIN_HAND;
        else if (player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof AlbumItem albumItem && albumItem.isEditable())
            hand = InteractionHand.OFF_HAND;
        else
            throw new IllegalStateException("Player receiving this packet should have an album in one of the hands.");

        ItemStack albumStack = player.getItemInHand(hand);
        AlbumItem albumItem = (AlbumItem) albumStack.getItem();

        ItemStack signedAlbum = albumItem.sign(albumStack, title, player.getName().getString());
        player.setItemInHand(hand, signedAlbum);

        player.level().playSound(null, player, SoundEvents.VILLAGER_WORK_CARTOGRAPHER, SoundSource.PLAYERS, 0.8f ,1f);

        return true;
    }
}
