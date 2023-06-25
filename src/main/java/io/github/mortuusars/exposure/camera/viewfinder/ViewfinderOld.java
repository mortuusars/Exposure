package io.github.mortuusars.exposure.camera.viewfinder;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.camera.Camera;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class ViewfinderOld {
    public static final String FOCAL_LENGTH_TAG = "FocalLength";

    private static float maxFov = focalLengthToFov(18);
    private static float minFov = focalLengthToFov(55);

    public static float currentFov;
    public static float targetFov = maxFov;

    private static boolean isActive;
    private static ItemAndStack<CameraItem> camera;
    private static InteractionHand holdingHand;

    public static void activate(ItemStack camera, InteractionHand hand) {
        Preconditions.checkArgument(!camera.isEmpty(), "Camera cannot be empty.");
        setActive(true, new ItemAndStack<>(camera), hand);
    }

    public static void deactivate() {
        setActive(false, camera, holdingHand);
    }

    @SuppressWarnings("DataFlowIssue")
    private static void setActive(boolean active, ItemAndStack<CameraItem> camera, InteractionHand hand) {
        isActive = active;
        ViewfinderOld.camera = camera;
        holdingHand = hand;

        Camera.FocalRange focalRange = camera.getItem().getFocalRange(camera.getStack());

        maxFov = focalLengthToFov(focalRange.min());
        minFov = focalLengthToFov(focalRange.max());

        if (camera.getStack().getTag() != null && camera.getStack().getTag().contains(FOCAL_LENGTH_TAG, Tag.TAG_INT)) {
            int focalLength = camera.getStack().getOrCreateTag().getInt(FOCAL_LENGTH_TAG);
            float fov = focalLengthToFov(focalLength);

            targetFov = Mth.clamp(fov, minFov, maxFov);
        }
    }

    public static boolean isActive() {
        Minecraft mc = Minecraft.getInstance();
        if (isActive && mc.player != null) {
            isActive = mc.player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof CameraItem
                    || mc.player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof CameraItem;
        }

        return isActive;
    }

    public static double getMouseSensitivityModifier() {
        // TODO: Should take lens focal ranges into account
        return isActive ? Mth.clamp(1f - (maxFov - targetFov) / maxFov, 0.1f, 1f) : 1f;
    }

    public static int fovToFocalLength(float fov) {
        double sensorWidth = 36.0; // Sensor width in millimeters
        return (int) Math.round(sensorWidth / (2.0 * Math.tan(Math.toRadians(fov / 2.0))));
    }

    public static float focalLengthToFov(float focalLength) {
        double sensorWidth = 36.0; // Sensor width in millimeters
        return (float) (2.0 * Math.toDegrees(Math.atan(sensorWidth / (2.0 * focalLength))));
    }

    public static void modifyZoom(ZoomDirection direction, boolean fineTuning) {
        float step = 10f * ( 1f - Mth.clamp((maxFov - currentFov) / maxFov, 0.3f, 1f));
        float inertia = Math.abs((targetFov - currentFov)) * 0.8f;

        float change = step + inertia;

        if (fineTuning)
            change *= 0.25f;

        targetFov = Mth.clamp(targetFov += direction == ZoomDirection.IN ? +change : -change, minFov, maxFov);

        camera.getStack().getOrCreateTag().putInt(FOCAL_LENGTH_TAG, fovToFocalLength(targetFov));
        camera.getItem().clientsideUpdateCameraInInventory(camera.getStack(), holdingHand);
    }
}
