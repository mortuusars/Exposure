package io.github.mortuusars.exposure.camera.viewfinder;


import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.infrastructure.FocalRange;
import io.github.mortuusars.exposure.camera.infrastructure.SynchronizedCameraInHandActions;
import io.github.mortuusars.exposure.camera.infrastructure.ZoomDirection;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.util.CameraInHand;
import io.github.mortuusars.exposure.util.Fov;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class ViewfinderClient {
    public static final float ZOOM_STEP = 8f;
    public static final float ZOOM_PRECISE_MODIFIER = 0.25f;
    private static boolean isOpen;

    private static FocalRange focalRange = FocalRange.FULL;
    private static double targetFov = 90f;
    private static double currentFov = targetFov;

    @Nullable
    private static String previousShaderEffect;

    public static boolean isOpen() {
        return isOpen;
    }

    public static boolean isLookingThrough() {
        return isOpen() && (Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON
                || Minecraft.getInstance().options.getCameraType() == CameraType.THIRD_PERSON_FRONT);
    }

    public static void open() {
        LocalPlayer player = Minecraft.getInstance().player;
        Preconditions.checkState(player != null, "Player should not be null");
        Preconditions.checkState(player.level().isClientSide(), "This should be called only client-side.");

        if (isOpen())
            return;

        @Nullable InteractionHand activeHand = CameraInHand.getActiveHand(player);
        Preconditions.checkState(activeHand != null, "Player should have active camera in hand.");

        ItemAndStack<CameraItem> camera = new ItemAndStack<>(player.getItemInHand(activeHand));

        focalRange = camera.getItem().getFocalRange(camera.getStack());
        targetFov = Fov.focalLengthToFov(Mth.clamp(camera.getItem().getFocalLength(camera.getStack()), focalRange.min(), focalRange.max()));

        isOpen = true;

        Optional<ItemStack> attachment = camera.getItem().getAttachment(camera.getStack(), CameraItem.FILTER_ATTACHMENT);
        attachment.ifPresent(stack -> {
            PostChain effect = Minecraft.getInstance().gameRenderer.currentEffect();
            if (effect != null)
                previousShaderEffect = effect.getName();

            String itemName = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
            Minecraft.getInstance().gameRenderer.loadEffect(new ResourceLocation("exposure", "shaders/post/" + itemName + ".json"));
        });

        SelfieClient.update(camera, activeHand, false);

        ViewfinderOverlay.setup();
    }

    public static void close() {
        isOpen = false;
        targetFov = Minecraft.getInstance().options.fov().get();

        Minecraft.getInstance().gameRenderer.shutdownEffect();
        if (previousShaderEffect != null) {
            Minecraft.getInstance().gameRenderer.loadEffect(new ResourceLocation(previousShaderEffect));
            previousShaderEffect = null;
        }
    }

    public static FocalRange getFocalRange() {
        return focalRange;
    }

    public static double getCurrentFov() {
        return currentFov;
    }

    public static float getSelfieCameraDistance() {
        return 1.75f;
    }

    public static void zoom(ZoomDirection direction, boolean precise) {
        double step = ZOOM_STEP * (1f - Mth.clamp((focalRange.min() - currentFov) / focalRange.min(), 0.3f, 1f));
        double inertia = Math.abs(targetFov - currentFov) * 0.8f;
        double change = step + inertia;

        if (precise)
            change *= ZOOM_PRECISE_MODIFIER;

        double prevFov = targetFov;

        double fov = Mth.clamp(targetFov + (direction == ZoomDirection.IN ? -change : +change),
                Fov.focalLengthToFov(focalRange.max()),
                Fov.focalLengthToFov(focalRange.min()));

        if (Math.abs(prevFov - fov) > 0.01f)
            Objects.requireNonNull(Minecraft.getInstance().player).playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get());

        targetFov = fov;
        SynchronizedCameraInHandActions.setZoom(Fov.fovToFocalLength(fov));
    }

    public static double modifyMouseSensitivity(double sensitivity) {
        if (!isLookingThrough())
            return sensitivity;

        double modifier = Mth.clamp(1f - (Config.Client.VIEWFINDER_ZOOM_SENSITIVITY_MODIFIER.get()
                * ((Minecraft.getInstance().options.fov().get() - currentFov) / 5f)), 0.01, 2f);
        return sensitivity * modifier;
    }

    public static void onPlayerTick(Player player) {
        if (!player.equals(Minecraft.getInstance().player))
            return;

        boolean cameraActive = CameraInHand.isActive(player);
        if (cameraActive && !ViewfinderClient.isOpen())
            ViewfinderClient.open();
        else if (!cameraActive && ViewfinderClient.isOpen())
            ViewfinderClient.close();
    }

    public static boolean handleMouseScroll(ZoomDirection direction) {
        if (isLookingThrough()) {
            zoom(direction, false);
            return true;
        }

        return false;
    }

    public static double modifyFov(double fov) {
        if (isLookingThrough())
            currentFov = Mth.lerp(Math.min(0.6f * Minecraft.getInstance().getDeltaFrameTime(), 0.6f), currentFov, targetFov);
        else if (Math.abs(currentFov - fov) > 0.00001)
            currentFov = Mth.lerp(Math.min(0.8f * Minecraft.getInstance().getDeltaFrameTime(), 0.8f), currentFov, fov);
        else {
            currentFov = fov;
            return fov;
        }

        return currentFov;
    }
}
