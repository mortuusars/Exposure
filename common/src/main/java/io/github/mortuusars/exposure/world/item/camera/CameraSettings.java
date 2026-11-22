package io.github.mortuusars.exposure.world.item.camera;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.component.*;
import io.github.mortuusars.exposure.world.sound.SoundEffect;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CameraSettings {
    private static final Map<Identifier, CameraSetting<?>> REGISTRY = new HashMap<>();

    public static <T> CameraSetting<T> register(Identifier id, CameraSetting<T> setting) {
        Preconditions.checkArgument(!REGISTRY.containsKey(id), "Setting with id '%s' is already registered.", id);
        REGISTRY.put(id, setting);
        return setting;
    }

    public static CameraSetting<?> byId(Identifier id) {
        @Nullable CameraSetting<?> setting = REGISTRY.get(id);
        if (setting == null) {
            throw new IllegalStateException("Setting with id '" + id + "' is not registered.");
        }
        return setting;
    }

    public static Identifier idOf(CameraSetting<?> setting) {
        for (Map.Entry<Identifier, CameraSetting<?>> entry : REGISTRY.entrySet()) {
            if (entry.getValue().equals(setting)) {
                return entry.getKey();
            }
        }
        throw new IllegalStateException("Setting is not registered.");
    }

    public static final CameraSetting<Boolean> SELFIE_MODE = register(Exposure.resource("selfie"),
            new CameraSetting<>(Exposure.DataComponents.SELFIE_MODE, false, new SoundEffect(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK)));
    public static final CameraSetting<Float> ZOOM = register(Exposure.resource("zoom"),
            new CameraSetting<>(Exposure.DataComponents.ZOOM, 0f, new SoundEffect(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK)));
    public static final CameraSetting<Double> SELFIE_ROTATION_X = register(Exposure.resource("selfie_rotation_x"),
            new CameraSetting<>(Exposure.DataComponents.SELFIE_ROTATION_X, 0.0, new SoundEffect(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK)));
    public static final CameraSetting<Double> SELFIE_ROTATION_Y = register(Exposure.resource("selfie_rotation_y"),
            new CameraSetting<>(Exposure.DataComponents.SELFIE_ROTATION_Y, 0.0, new SoundEffect(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK)));
    public static final CameraSetting<ShutterSpeed> SHUTTER_SPEED = register(Exposure.resource("shutter_speed"),
            new CameraSetting<>(Exposure.DataComponents.SHUTTER_SPEED, ShutterSpeed.DEFAULT, new SoundEffect(Exposure.SoundEvents.CAMERA_DIAL_CLICK)));
    public static final CameraSetting<CompositionGuide> COMPOSITION_GUIDE = register(Exposure.resource("composition_guide"),
            new CameraSetting<>(Exposure.DataComponents.COMPOSITION_GUIDE, CompositionGuides.NONE, new SoundEffect(Exposure.SoundEvents.CAMERA_BUTTON_CLICK)));
    public static final CameraSetting<SelfTimer> SELF_TIMER = register(Exposure.resource("self_timer"),
            new CameraSetting<>(Exposure.DataComponents.SELF_TIMER, SelfTimer.OFF, new SoundEffect(Exposure.SoundEvents.CAMERA_BUTTON_CLICK)));
    public static final CameraSetting<FlashMode> FLASH_MODE = register(Exposure.resource("flash_mode"),
            new CameraSetting<>(Exposure.DataComponents.FLASH_MODE, FlashMode.OFF, new SoundEffect(Exposure.SoundEvents.CAMERA_BUTTON_CLICK)));
}
