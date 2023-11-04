package io.github.mortuusars.exposure.camera.viewfinder;


import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.infrastructure.FocalRange;
import io.github.mortuusars.exposure.camera.infrastructure.ZoomDirection;
import io.github.mortuusars.exposure.camera.infrastructure.SynchronizedCameraInHandActions;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.util.CameraInHand;
import io.github.mortuusars.exposure.util.Fov;
import net.minecraft.Util;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class ViewfinderClient {
    public static final float ZOOM_STEP = 8f;
    public static final float ZOOM_PRECISE_MODIFIER = 0.25f;
    private static boolean isOpen;
    private static long closedAt = 0;

    private static FocalRange focalRange = FocalRange.FULL;
    private static double targetFov = 90f;
    private static double currentFov = targetFov;

    @Nullable
    private static String previousShaderEffect;

    public static boolean isOpen() {
        return isOpen;
    }

    public static boolean isLookingThrough() {
        return isOpen() && Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON;
    }

    public static void open(Player player) {
        Preconditions.checkState(player.getLevel().isClientSide, "This should be called only client-side.");
        if (isOpen() || player != Minecraft.getInstance().player)
            return;

        CameraInHand camera = CameraInHand.ofPlayer(player);

        focalRange = camera.getItem().getFocalRange(camera.getStack());
        targetFov = Fov.focalLengthToFov(Mth.clamp(camera.getItem().getFocalLength(camera.getStack()), focalRange.min(), focalRange.max()));

        isOpen = true;

        Optional<ItemStack> attachment = camera.getItem().getAttachment(camera.getStack(), CameraItem.FILTER_ATTACHMENT);
        attachment.ifPresent(stack -> {
            PostChain effect = Minecraft.getInstance().gameRenderer.currentEffect();
            if (effect != null)
                previousShaderEffect = effect.getName();

            String itemName = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(stack.getItem())).getPath();
            Minecraft.getInstance().gameRenderer.loadEffect(new ResourceLocation("exposure", "shaders/post/" + itemName + ".json"));
        });

        ViewfinderOverlay.setup();
    }

    public static void close(Player player) {
        Preconditions.checkState(player.getLevel().isClientSide, "This should be called only client-side.");

        if (player != Minecraft.getInstance().player)
            return;

        if (!isOpen())
            return;

        // This method sometimes gets called twice: from regular closing and from playerTick.
        // And isOpen is true the second time for some reason.
        // I'm not sure if this fixes it, but leaving it just in case.
        if (Util.getMillis() - closedAt < 100) {
            isOpen = false;
            return;
        }
        closedAt = Util.getMillis();

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

    public static class ForgeEvents {
        @SubscribeEvent
        public static void mouseScroll(InputEvent.MouseScrollingEvent event) {
            if (isLookingThrough() && event.getScrollDelta() != 0) {
                event.setCanceled(true);
                zoom(event.getScrollDelta() > 0d ? ZoomDirection.IN : ZoomDirection.OUT, false);
            }
        }

        @SubscribeEvent
        public static void computeFOV(ViewportEvent.ComputeFov event) {
            if (!event.usedConfiguredFov())
                return;

            if (isLookingThrough())
                currentFov = Mth.lerp(Math.min(0.6f * Minecraft.getInstance().getDeltaFrameTime(), 0.6f), currentFov, targetFov);
            else if (Math.abs(currentFov - event.getFOV()) > 0.00001)
                currentFov = Mth.lerp(Math.min(0.8f * Minecraft.getInstance().getDeltaFrameTime(), 0.8f), currentFov, event.getFOV());
            else {
                currentFov = event.getFOV();
                return;
            }

            event.setFOV(currentFov);
        }
    }
}
