package io.github.mortuusars.exposure.camera;

import io.github.mortuusars.exposure.camera.component.Shutter;

public class ServerCameraHolder {
    public static final ServerCamera SERVER_CAMERA = new ServerCamera(new Shutter());
}
