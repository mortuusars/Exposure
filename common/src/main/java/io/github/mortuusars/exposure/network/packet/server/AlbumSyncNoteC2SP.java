package io.github.mortuusars.exposure.network.packet.server;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Either;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.item.AlbumPage;
import io.github.mortuusars.exposure.menu.AlbumMenu;
import io.github.mortuusars.exposure.network.PacketDirection;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record AlbumSyncNoteC2SP(int pageIndex, String text) implements IPacket<AlbumSyncNoteC2SP> {
    public static final ResourceLocation ID = Exposure.resource("album_update_note");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public static AlbumSyncNoteC2SP fromBuffer(FriendlyByteBuf buffer) {
        return new AlbumSyncNoteC2SP(buffer.readInt(), buffer.readUtf());
    }

    @Override
    public FriendlyByteBuf toBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(pageIndex);
        buffer.writeUtf(text);
        return buffer;
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        Preconditions.checkState(player != null, "Cannot handle packet: Player was null");

        if (!(player.containerMenu instanceof AlbumMenu albumMenu))
            throw new IllegalStateException("Player receiving this packet should have AlbumMenu open. Current menu: " + player.containerMenu);

        AlbumPage page = albumMenu.getPages().get(pageIndex);
        page.setNote(Either.left(text));
        albumMenu.updateAlbumStack();

        return true;
    }
}
