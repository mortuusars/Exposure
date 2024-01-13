package io.github.mortuusars.exposure.util;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum Side implements StringRepresentable {
    LEFT(0, "left"),
    RIGHT(1, "right");

    private final int index;
    private final String name;

    Side(int index, String name) {
        this.index = index;
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }
}
