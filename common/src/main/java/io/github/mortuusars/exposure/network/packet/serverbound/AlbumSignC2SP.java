package io.github.mortuusars.exposure.network.packet.serverbound;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.item.AlbumItem;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record AlbumSignC2SP(int slot, String title, String author) implements Packet {
    public static final Identifier ID = Exposure.resource("album_sign");
    public static final CustomPacketPayload.Type<AlbumSignC2SP> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, AlbumSignC2SP> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, AlbumSignC2SP::slot,
            ByteBufCodecs.STRING_UTF8, AlbumSignC2SP::title,
            ByteBufCodecs.STRING_UTF8, AlbumSignC2SP::author,
            AlbumSignC2SP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        Preconditions.checkState(player != null, "Cannot handle packet: Player was null");

        ItemStack albumStack = player.getInventory().getItem(slot());
        if (albumStack.getItem() instanceof AlbumItem albumItem) {
            ItemStack signedAlbumStack = albumItem.sign(albumStack, title(), author());
            player.getInventory().setItem(slot(), signedAlbumStack);
            player.level().playSound(null, player, Exposure.SoundEvents.WRITE.get(), SoundSource.PLAYERS, 0.8f ,1f);
        }

        return true;
    }
}
