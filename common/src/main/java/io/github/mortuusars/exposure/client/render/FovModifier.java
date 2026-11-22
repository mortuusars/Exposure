package io.github.mortuusars.exposure.client.render;

import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.animation.Animation;
import io.github.mortuusars.exposure.client.animation.EasingFunction;
import io.github.mortuusars.exposure.client.camera.viewfinder.Viewfinder;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class FovModifier {
    private static float lastFov = -1;
    private static @Nullable Animation restoringAnimation;

    private static float overrideFov = -1;

    public static boolean shouldOverride() {
        return overrideFov != -1 || (CameraClient.viewfinder() != null && CameraClient.viewfinder().isLookingThrough());
    }

    public static float modify(float originalValue) {
        if (overrideFov != -1) {
            return overrideFov;
        }

        Viewfinder viewfinder = CameraClient.viewfinder();
        if (viewfinder == null || !viewfinder.isLookingThrough()) {
            return restoreToOriginal(originalValue);
        }

        restoringAnimation = null;
        lastFov = viewfinder.zoom().getCurrentFov();
        return lastFov;
    }

    /**
     * Sets fov value regardless of viewfinder or settings. Don't forget to cancel with {@link FovModifier#cancelOverride()}.
     */
    public static void setOverride(float fov) {
        overrideFov = fov;
    }

    public static void cancelOverride() {
        overrideFov = -1;
    }

    private static float restoreToOriginal(float originalValue) {
        if (lastFov == -1) {
            return originalValue;
        }

        if (restoringAnimation == null) {
            restoringAnimation = new Animation(300, EasingFunction.EASE_OUT_EXPO);
        }

        float fov = Mth.lerp(restoringAnimation.getValue(), lastFov, originalValue);

        if (restoringAnimation.isFinished()) {
            restoringAnimation = null;
            lastFov = -1;
        }

        return fov;
    }
}
