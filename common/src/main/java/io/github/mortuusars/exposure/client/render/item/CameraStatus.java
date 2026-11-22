package io.github.mortuusars.exposure.client.render.item;

import com.mojang.serialization.Codec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.function.IntFunction;

public enum CameraStatus implements StringRepresentable {
    NONE(0, "none"),
    ACTIVE(1, "active"),
    SELFIE(2, "selfie"),
    SELFIE_VIEWFINDER(3, "selfie_viewfinder");

    public static final Codec<CameraStatus> CODEC = StringRepresentable.fromEnum(CameraStatus::values);
    public static final IntFunction<CameraStatus> BY_ID = ByIdMap.continuous(CameraStatus::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    private final byte id;
    private final String name;

    CameraStatus(final int id, final String name) {
        this.id = (byte) id;
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public byte getId() {
        return id;
    }
}
