package io.github.mortuusars.exposure.server;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.CameraId;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * This system allows storing some camera data that we need to always have,
 * regardless of player holding the camera item or not.<br>
 * Some actions are delayed, and we cannot make sure that player will still hold
 * a camera when that action completes (projecting from path for example).
 * <br><br>
 * Currently, it's not necessary to persist this data.
 * But if the need arises - it should not be hard to make it a {@link net.minecraft.world.level.saveddata.SavedData}.
 */
public class CameraInstances {
    private static final Map<CameraId, CameraInstance> INSTANCES = new HashMap<>();

    public static @Nullable CameraInstance get(CameraId id) {
        if (id.uuid().equals(Util.NIL_UUID)) return null;
        return INSTANCES.get(id);
    }

    public static Optional<CameraInstance> getOptional(CameraId id) {
        if (id.uuid().equals(Util.NIL_UUID)) return Optional.empty();
        return Optional.ofNullable(INSTANCES.get(id));
    }

    public static @Nullable CameraInstance get(ItemStack stack) {
        return get(CameraId.ofStack(stack));
    }

    public static Optional<CameraInstance> getOptional(ItemStack stack) {
        return getOptional(CameraId.ofStack(stack));
    }

    public static CameraInstance getOrThrow(CameraId id) {
        @Nullable CameraInstance instance = get(id);
        Preconditions.checkState(instance != null, "No Camera Instance with id '%s' found.", id);
        return instance;
    }

    public static void ifPresent(CameraId id, Consumer<CameraInstance> instanceConsumer) {
        @Nullable CameraInstance instance = get(id);
        if (instance != null) {
            instanceConsumer.accept(instance);
        }
    }

    public static void ifPresent(ItemStack stack, Consumer<CameraInstance> instanceConsumer) {
        @Nullable CameraId id = stack.get(Exposure.DataComponents.CAMERA_ID);
        if (id != null) {
            ifPresent(id, instanceConsumer);
        }
    }

    public static void add(CameraId id, CameraInstance instance) {
        INSTANCES.put(id, instance);
    }

    public static void createOrUpdate(CameraId id, Consumer<CameraInstance> instanceConsumer) {
        CameraInstance instance = INSTANCES.computeIfAbsent(id, uuid -> new CameraInstance(id));
        instanceConsumer.accept(instance);
    }

    // --

    public static boolean canReleaseShutter(CameraId id) {
        return getOptional(id).map(cameraInstance -> !cameraInstance.isWaitingForProjection()).orElse(true);
    }
}
