package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.camera.ExposureFrame;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.item.FilmItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public record ServerboundUpdateCameraPacket(String id, InteractionHand hand, int filmFrameIndex) {
    public void toBuffer(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(id);
        friendlyByteBuf.writeEnum(hand);
        friendlyByteBuf.writeInt(filmFrameIndex);
    }

    public static ServerboundUpdateCameraPacket fromBuffer(FriendlyByteBuf buffer) {
        return new ServerboundUpdateCameraPacket(buffer.readUtf(), buffer.readEnum(InteractionHand.class), buffer.readInt());
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        @Nullable ServerPlayer player = context.getSender();

        if (player == null)
            throw new IllegalStateException("Cannot handle UpdateCameraPacket: Player was null.");

        context.enqueueWork(() -> update(player));
        return true;
    }

    private void update(ServerPlayer player) {
        ItemStack camera = player.getItemInHand(hand);
        if (!(camera.getItem() instanceof CameraItem cameraItem))
            throw new IllegalStateException("Cannot handle UpdateCameraPacket: Item in hand is not camera: " + camera);

        ItemStack film = cameraItem.getLoadedFilm(camera);
        if (!(film.getItem() instanceof FilmItem filmItem))
            throw new IllegalStateException("Cannot handle UpdateCameraPacket: Film in camera is not a FilmItem: " + film);

        film = filmItem.setFrame(film, filmFrameIndex, new ExposureFrame(id));
        cameraItem.setFilm(camera, film);
//        player.setItemInHand(hand, camera);
    }
}
