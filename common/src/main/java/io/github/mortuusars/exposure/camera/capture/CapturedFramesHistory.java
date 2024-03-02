package io.github.mortuusars.exposure.camera.capture;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.camera.infrastructure.FrameData;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("unused")
public class CapturedFramesHistory {
    private static final ArrayList<CompoundTag> lastExposures = new ArrayList<>();
    private static int limit = 32;
    public static Collection<CompoundTag> get() {
        return ImmutableList.copyOf(lastExposures);
    }

    public static void add(CompoundTag frame) {
        if (frame.getString(FrameData.ID).isEmpty())
            LogUtils.getLogger().warn(frame + " - frame might not be valid. No ID is present.");

        lastExposures.add(0, frame);

        while (lastExposures.size() > limit) {
            lastExposures.remove(limit);
        }
    }

    public static int getLimit() {
        return limit;
    }

    public static void setLimit(int limit) {
        CapturedFramesHistory.limit = limit;
    }

    public static void clear() {
        lastExposures.clear();
    }
}
