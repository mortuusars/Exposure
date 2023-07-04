package io.github.mortuusars.exposure.camera;

import io.github.mortuusars.exposure.camera.component.Shutter;

public class ClientCameraHolder {
    public static final Camera CLIENT_CAMERA = new ClientCamera(new Shutter());
}
