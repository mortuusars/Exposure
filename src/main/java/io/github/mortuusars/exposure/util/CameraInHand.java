package io.github.mortuusars.exposure.util;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.DeactivateCamerasInHandServerboundPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CameraInHand {
    public static final CameraInHand EMPTY = new CameraInHand(null, null);

    @Nullable
    private ItemAndStack<CameraItem> camera;
    @Nullable
    private InteractionHand hand;

    public CameraInHand(@Nullable ItemAndStack<CameraItem> camera, @Nullable InteractionHand hand) {
        this.camera = camera;
        this.hand = hand;
    }

    public CameraInHand(@NotNull Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack itemInHand = player.getItemInHand(hand);
            if (itemInHand.getItem() instanceof CameraItem) {
                this.camera = new ItemAndStack<>(itemInHand);
                this.hand = hand;
                return;
            }
        }
    }

    public static void deactivate(Player player) {
        Preconditions.checkArgument(player != null, "Player cannot be null");
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack itemInHand = player.getItemInHand(hand);
            if (itemInHand.getItem() instanceof CameraItem cameraItem)
                cameraItem.deactivate(player, itemInHand);
        }

        if (player.getLevel().isClientSide)
            Packets.sendToServer(new DeactivateCamerasInHandServerboundPacket());
    }

    public static @Nullable InteractionHand getActiveHand(Player player) {
        Preconditions.checkArgument(player != null, "Player should not be null.");

        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack itemInHand = player.getItemInHand(hand);
            if (itemInHand.getItem() instanceof CameraItem cameraItem && cameraItem.isActive(itemInHand))
                return hand;
        }

        return null;
    }

    public static boolean isActive(Player player) {
        return getActiveHand(player) != null;
    }

    public static CameraInHand getActive(Player player) {
        @Nullable InteractionHand activeHand = getActiveHand(player);
        if (activeHand == null)
            return new CameraInHand(null, null);

        return new CameraInHand(new ItemAndStack<>(player.getItemInHand(activeHand)), activeHand);
    }

    public boolean isEmpty() {
        return this.equals(EMPTY) || camera == null || hand == null;
    }

    public ItemAndStack<CameraItem> getCamera() {
        Preconditions.checkState(!isEmpty(), "getCamera should not be called before checking isEmpty first.");
        return camera;
    }

    public CameraItem getItem() {
        Preconditions.checkState(!isEmpty(), "getItem should not be called before checking isEmpty first.");
        Preconditions.checkState(camera != null, "getItem should not be called before checking isEmpty first.");
        return camera.getItem();
    }

    public ItemStack getStack() {
        Preconditions.checkState(!isEmpty(), "getStack should not be called before checking isEmpty first.");
        Preconditions.checkState(camera != null, "getStack should not be called before checking isEmpty first.");
        return camera.getStack();
    }

    public InteractionHand getHand() {
        Preconditions.checkState(!isEmpty(), "getHand should not be called before checking isEmpty first.");
        return hand;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CameraInHand that = (CameraInHand) o;
        return Objects.equals(camera, that.camera) && hand == that.hand;
    }

    @Override
    public int hashCode() {
        return Objects.hash(camera, hand);
    }

    @Override
    public String toString() {
        return "CameraInHand{" +
                "camera=" + (camera != null ? camera.getStack() : "null") +
                ", hand=" + hand +
                '}';
    }
}
