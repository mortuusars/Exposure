package io.github.mortuusars.exposure.integration.shoulder_surfing;

import com.github.exopandora.shouldersurfing.api.client.IShoulderSurfing;
import com.github.exopandora.shouldersurfing.api.client.Perspective;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.camera.CameraOnStand;
import org.jetbrains.annotations.Nullable;

public class ShoulderSurfingCompat {
    private static @Nullable Perspective perspectiveBefore;

    public static void onViewfinderSetup(Camera camera) {
        perspectiveBefore = Perspective.current();
        if (perspectiveBefore != Perspective.FIRST_PERSON
              && (perspectiveBefore == Perspective.SHOULDER_SURFING
                  || perspectiveBefore == Perspective.THIRD_PERSON_BACK
                  || camera instanceof CameraOnStand)) {
            IShoulderSurfing.getInstance().changePerspective(Perspective.FIRST_PERSON);
        }
    }

    public static void onViewfinderRemove() {
        if (perspectiveBefore != null) {
            IShoulderSurfing.getInstance().changePerspective(perspectiveBefore);
            perspectiveBefore = null;
        }
    }
}
