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
    private static float targetFov = 90f;
    private static float currentFov = targetFov;

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

    public static float getTargetFov() {
        return targetFov;
    }

    public static void zoom(ZoomDirection direction, boolean precise) {
        float step = 8f * (1f - Mth.clamp((focalRange.min() - currentFov) / focalRange.min(), 0.3f, 1f));
        float inertia = Math.abs((targetFov - currentFov)) * 0.8f;
        float change = step + inertia;

        if (precise)
            change *= 0.25f;


        float prevFov = targetFov;

        float fov = Mth.clamp(targetFov + (direction == ZoomDirection.IN ? -change : +change),
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
            if (!event.usedConfiguredFov() || !isOpen())
                return;

            currentFov = (float)Mth.lerp(Math.min(1.0, 0.5 * Minecraft.getInstance().getDeltaFrameTime()), currentFov, targetFov);

            event.setFOV(currentFov);


//            if (event.usedConfiguredFov()) {
//                defaultFov = (float) fov;
//                if (targetFov == -1)
//                    targetFov = defaultFov;
//                if (currentFov == -1)
//                    currentFov = defaultFov;
//            }
//
//            if (shouldRender()) {
//                currentFov = Mth.lerp(Math.min(0.25f * minecraft.getDeltaFrameTime(), 1f), currentFov, targetFov);
//            } else if (!fovRestored) {
//                currentFov = Mth.lerp(Math.min(0.5f * minecraft.getDeltaFrameTime(), 1f), currentFov, defaultFov);
//
//                if (Math.abs(currentFov - defaultFov) < 0.0001d) {
//                    fovRestored = true;
//
//                    LocalPlayer player = minecraft.player;
//                    if (player != null && event.usedConfiguredFov()) {
//                        // Item in hand snaps weirdly when fov is changing to normal.
//                        // So we render hand only after fov is restored and play equip animation to smoothly show a hand.
//                        minecraft.gameRenderer.itemInHandRenderer.itemUsed(player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()
//                                ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
//                    }
//                }
//            } else {
//                currentFov = defaultFov;
//                return;
//            }
//
//            event.setFOV(currentFov);
        }
    }
}
