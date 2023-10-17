package io.github.mortuusars.exposure.camera.film;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public enum FilmType implements StringRepresentable {
    BLACK_AND_WHITE("black_and_white"),
    COLOR("color");

    public static final StringRepresentable.EnumCodec<FilmType> CODEC = StringRepresentable.fromEnum(FilmType::values);

    private final String name;

    FilmType(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    /**
     * @return the FilmType specified by the given name or null if no such FilmType exists
     */
    @Nullable
    public static FilmType byName(@Nullable String name) {
        return CODEC.byName(name);
    }
}
