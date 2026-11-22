package io.github.mortuusars.exposure.world.level.storage;

import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ExposureFrameHistory extends SavedData {
    public static final Codec<ExposureFrameHistory> CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.list(Frame.CODEC)).stable()
            .xmap(ExposureFrameHistory::new, ExposureFrameHistory::getFrames);

    public static final int LIMIT = 32;

    private final Map<UUID, List<Frame>> frames;

    public ExposureFrameHistory(Map<UUID, List<Frame>> frames) {
        this.frames = new HashMap<>(frames);
    }

    public Map<UUID, List<Frame>> getFrames() {
        return frames;
    }

    public List<Frame> getFramesOf(Entity entity) {
        return getFramesOf(entity.getUUID());
    }

    public List<Frame> getFramesOf(UUID uuid) {
        return frames.getOrDefault(uuid, Collections.emptyList());
    }

    public void add(Entity entity, Frame frame) {
        add(entity.getUUID(), frame);
    }

    public void add(UUID uuid, Frame frame) {
        List<Frame> list = frames.compute(uuid, (id, framesList) ->
                framesList == null ? new ArrayList<>() : new ArrayList<>(framesList));
        while (list.size() >= LIMIT) {
            list.removeFirst();
        }
        list.add(frame);
        setDirty();
    }

    public void clear() {
        frames.clear();
    }

    public void clearOf(Entity entity) {
        frames.remove(entity.getUUID());
    }

    public static SavedDataType<ExposureFrameHistory> factory() {
        return new SavedDataType<>("exposure_frame_history", () -> new ExposureFrameHistory(new HashMap<>()), CODEC, null);
    }

    public static @NotNull ExposureFrameHistory loadOrCreate(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(ExposureFrameHistory.factory());
    }
}
