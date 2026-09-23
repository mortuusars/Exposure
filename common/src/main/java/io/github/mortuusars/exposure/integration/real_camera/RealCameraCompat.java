package io.github.mortuusars.exposure.integration.real_camera;

import com.xtracr.realcamera.compat.DisableHelper;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.world.entity.CameraOperator;

public class RealCameraCompat {
    public static void init() {
        DisableHelper.registerOr("renderModel",
                entity -> Config.Client.REAL_CAMERA_DISABLE_IN_VIEWFINDER.get() && entity instanceof CameraOperator operator && operator.getActiveExposureCamera() != null);
    }
}
