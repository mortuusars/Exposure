package io.github.mortuusars.exposure.camera.infrastructure;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum FilmType implements StringRepresentable {
    BLACK_AND_WHITE("black_and_white", 255, 255, 255, 1.0F, 1.0F, 1.0F, 1.0F),
    COLOR("color", 180, 130, 110, 1.2F, 0.96F, 0.75F, 1.0F),
    COLOR_POSITIVE("color_positive", 255, 255, 255, 0.0F, 0.0F, 0.0F, 1.0F);

    @SuppressWarnings("deprecation")
    public static final StringRepresentable.EnumCodec<FilmType> CODEC = StringRepresentable.fromEnum(FilmType::values);

    private final String name;
    public final int frameR, frameG, frameB;
    public final float filmR, filmG, filmB, filmA;

    FilmType(String name, int frameR, int frameG, int frameB, float filmR, float filmG, float filmB, float filmA) {
        this.name = name;
        this.frameR = frameR;
        this.frameG = frameG;
        this.frameB = frameB;

        this.filmR = filmR;
        this.filmG = filmG;
        this.filmB = filmB;
        this.filmA = filmA;
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

    public ItemStack createItemStack() {
        return new ItemStack(this == COLOR ? Exposure.Items.COLOR_FILM.get() : Exposure.Items.BLACK_AND_WHITE_FILM.get());
    }

    public ItemStack createDevelopedItemStack() {
        // Updated to a switch statement to handle all types
        return switch (this) {
            case BLACK_AND_WHITE -> new ItemStack(Exposure.Items.DEVELOPED_BLACK_AND_WHITE_FILM.get());
            case COLOR -> new ItemStack(Exposure.Items.DEVELOPED_COLOR_FILM.get());
            case COLOR_POSITIVE -> new ItemStack(Exposure.Items.DEVELOPED_COLOR_POSITIVE_FILM.get());
        };
    }
}
