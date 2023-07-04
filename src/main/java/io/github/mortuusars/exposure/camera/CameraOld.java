package io.github.mortuusars.exposure.camera;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.viewfinder.IViewfinder;
import io.github.mortuusars.exposure.camera.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.client.ClientOnlyLogic;
import io.github.mortuusars.exposure.client.viewfinder.ViewfinderRenderer;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CameraOld {


    private static final IViewfinder viewfinder = new Viewfinder();

    @Nullable
    private static ItemAndStack<CameraItem> camera;

    public static void activate(InteractionHand hand) {
        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;
        ItemStack itemInHand = player.getItemInHand(hand);

        if (itemInHand.getItem() instanceof CameraItem) {
            camera = new ItemAndStack<>(itemInHand);
            viewfinder.activateAndBroadcast(player, hand);
            ViewfinderRenderer.setup();
        }
        else
            Exposure.LOGGER.error("Cannot activate a camera: " + itemInHand + " is not a CameraItem.");
    }

    public static void deactivate() {
        viewfinder.deactivateAndBroadcast(Minecraft.getInstance().player);
        camera = null;
    }

    public static boolean isActive(Player player) {
        return viewfinder.isActive(player);
    }

    public static Optional<ItemAndStack<CameraItem>> getActiveCamera() {
        return Optional.ofNullable(camera);
    }

    public static void refreshActiveCamera(ItemStack cameraStack) {
        Preconditions.checkState(!cameraStack.isEmpty(), "Camera stack cannot be empty.");
        camera = new ItemAndStack<>(cameraStack);
    }

    public static Optional<ItemAndStack<CameraItem>> getCameraInHand(Player player) {
        ItemStack cameraStack = ItemStack.EMPTY;

        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack itemInHand = player.getItemInHand(hand);
            if (itemInHand.getItem() instanceof CameraItem)
                cameraStack = itemInHand;
        }

        return cameraStack.isEmpty() ? Optional.empty() : Optional.of(new ItemAndStack<>(cameraStack));
    }

    public static void changeZoom(float focalLength) {
        getActiveCamera().ifPresent(camera -> {
            camera.getItem().setZoom(camera.getStack(), focalLength);
            updateAndSyncCameraInHand(camera.getStack());
        });
    }

    public static void updateAndSyncCameraInHand(ItemStack cameraStack) {
//        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientOnlyLogic.updateAndSyncCameraStack(cameraStack, Exposure.getCamera().getCameraInHand());
    }
}
