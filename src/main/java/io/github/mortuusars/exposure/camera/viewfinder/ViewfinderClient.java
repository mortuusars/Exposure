package io.github.mortuusars.exposure.camera.viewfinder;


import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.component.FocalRange;
import io.github.mortuusars.exposure.camera.component.ZoomDirection;
import io.github.mortuusars.exposure.camera.infrastructure.SynchronizedCameraInHandActions;
import io.github.mortuusars.exposure.config.Config;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.util.Fov;
import io.github.mortuusars.exposure.util.ItemAndStack;
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
import java.util.function.Supplier;

public class ViewfinderClient {
    private static boolean isOpen;

    private static Supplier<ItemAndStack<CameraItem>> camera;
    private static FocalRange focalRange = FocalRange.FULL;
    private static double targetFov = 90f;
    private static double currentFov = targetFov;

    @Nullable
    private static String previousShaderEffect;

    public static boolean isOpen() {
        return isOpen;
    }

    public static void open(Player player, Supplier<ItemAndStack<CameraItem>> cameraSupplier) {
        Preconditions.checkState(player.getLevel().isClientSide, "This should be called only client-side.");
        camera = cameraSupplier;

        ItemAndStack<CameraItem> camera = ViewfinderClient.camera.get();

        focalRange = camera.getItem().getFocalRange(camera.getStack());
        targetFov = Fov.focalLengthToFov(Mth.clamp(camera.getItem().getZoom(camera.getStack()), focalRange.min(), focalRange.max()));

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

    public static double getTargetFov() {
        return targetFov;
    }

    public static void zoom(ZoomDirection direction, boolean precise) {
        double step = 8f * (1f - Mth.clamp((focalRange.min() - currentFov) / focalRange.min(), 0.3f, 1f));
        double inertia = Math.abs((targetFov - currentFov)) * 0.8f;
        double change = step + inertia;

        if (precise)
            change *= 0.25f;

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
        if (!isOpen())
            return sensitivity;

        double modifier = Mth.clamp(1f - (Config.Client.VIEWFINDER_ZOOM_SENSITIVITY_MODIFIER.get() * ((Minecraft.getInstance().options.fov().get() - currentFov) / 5f)), 0.01, 2f);
        return sensitivity * modifier;
    }

    public static class ForgeEvents {
        @SubscribeEvent
        public static void mouseScroll(InputEvent.MouseScrollingEvent event) {
            if (isOpen() && event.getScrollDelta() != 0) {
                event.setCanceled(true);
                zoom(event.getScrollDelta() > 0d ? ZoomDirection.IN : ZoomDirection.OUT, false);
            }
        }

        @SubscribeEvent
        public static void computeFOV(ViewportEvent.ComputeFov event) {
            if (!event.usedConfiguredFov())
                return;

            if (isOpen())
                currentFov = Mth.lerp(Math.min(0.5f * Minecraft.getInstance().getDeltaFrameTime(), 0.5f), currentFov, targetFov);
            else if (Math.abs(currentFov - event.getFOV()) > 0.00001)
                currentFov = Mth.lerp(Math.min(0.75f * Minecraft.getInstance().getDeltaFrameTime(), 0.75f), currentFov, event.getFOV());
            else {
                currentFov = event.getFOV();
                return;
            }

            event.setFOV(currentFov);
        }
    }
}
