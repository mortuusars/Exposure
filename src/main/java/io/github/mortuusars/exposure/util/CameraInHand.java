package io.github.mortuusars.exposure.util;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.item.CameraItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CameraInHand {
    @Nullable
    private ItemAndStack<CameraItem> camera;
    @Nullable
    private InteractionHand hand;

    @SuppressWarnings("NullableProblems")
    public CameraInHand(ItemAndStack<CameraItem> camera, InteractionHand hand) {
        this.camera = camera;
        this.hand = hand;
    }

    public CameraInHand(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack itemInHand = player.getItemInHand(hand);
            if (itemInHand.getItem() instanceof CameraItem) {
                this.camera = new ItemAndStack<>(itemInHand);
                this.hand = hand;
            }
        }
    }

    public boolean isEmpty() {
        return camera == null || hand == null;
    }

    public ItemAndStack<CameraItem> getCamera() {
        Preconditions.checkState(!isEmpty(), "getCamera should not be called before checking isEmpty first.");
        return camera;
    }

    public CameraItem getItem() {
        Preconditions.checkState(!isEmpty(), "getItem should not be called before checking isEmpty first.");
        return camera.getItem();
    }

    public ItemStack getStack() {
        Preconditions.checkState(!isEmpty(), "getStack should not be called before checking isEmpty first.");
        return camera.getStack();
    }

    public InteractionHand getHand() {
        Preconditions.checkState(!isEmpty(), "getHand should not be called before checking isEmpty first.");
        return hand;
    }

    @Override
    public String toString() {
        return "CameraInHand{" +
                "camera=" + camera +
                ", hand=" + hand +
                '}';
    }
}
