package io.github.mortuusars.exposure.world.camera.capture;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.resources.Identifier;

public class CaptureType {
    public static final Identifier CAMERA = Exposure.resource("camera");
    public static final Identifier LOAD_COMMAND = Exposure.resource("load_command");
    public static final Identifier EXPOSE_COMMAND = Exposure.resource("expose_command");
    public static final Identifier DEBUG_RGB = Exposure.resource("debug_rgb");
}
