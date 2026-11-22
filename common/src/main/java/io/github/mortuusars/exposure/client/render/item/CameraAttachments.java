package io.github.mortuusars.exposure.client.render.item;

import com.mojang.serialization.Codec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.function.IntFunction;

public enum CameraAttachments implements StringRepresentable {
    NONE(0, "none"),
    LENS(1, "lens"),
    FLASH(2, "flash"),
    LENS_AND_FLASH(3, "lens_and_flash");

    public static final Codec<CameraAttachments> CODEC = StringRepresentable.fromEnum(CameraAttachments::values);
    public static final IntFunction<CameraAttachments> BY_ID = ByIdMap.continuous(CameraAttachments::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    private final byte id;
    private final String name;

    CameraAttachments(final int id, final String name) {
        this.id = (byte) id;
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    private byte getId() {
        return id;
    }
}
