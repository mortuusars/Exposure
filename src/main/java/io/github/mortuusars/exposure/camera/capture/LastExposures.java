package io.github.mortuusars.exposure.camera.capture;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("unused")
public class LastExposures {
    private static final ArrayList<String> lastExposures = new ArrayList<>();
    private static int limit = 32;
    public static Collection<String> get() {
        return ImmutableList.copyOf(lastExposures);
    }

    public static void add(String exposureId) {
        lastExposures.add(0, exposureId);

        while (lastExposures.size() > limit) {
            lastExposures.remove(limit);
        }
    }

    public static void clear() {
        lastExposures.clear();
    }

    public static int getLimit() {
        return limit;
    }

    public static void setLimit(int limit) {
        LastExposures.limit = limit;
    }
}
