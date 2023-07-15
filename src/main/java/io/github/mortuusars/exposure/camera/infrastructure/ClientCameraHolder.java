package io.github.mortuusars.exposure.camera.infrastructure;

import io.github.mortuusars.exposure.camera.component.Shutter;

public class ClientCameraHolder {
    public static final Camera CLIENT_CAMERA = new ClientCamera(new Shutter());
}
