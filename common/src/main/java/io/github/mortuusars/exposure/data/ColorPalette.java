package io.github.mortuusars.exposure.data;


import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.mortaar.util.color.Color;

import java.util.Arrays;
import java.util.List;

/**
 * List of <=256 colors. Last color should always be fully transparent.
 * If created with less than 256 colors - rest of the colors will be filled with black and last set to transparent.
 */
public record ColorPalette(int[] colors) {
    /**
     * Uses string representation of hex color - "FF7F7F7F", to make working with encoded values easier,
     * since we cannot use hex to represent int in json - it'll look like random mess of numbers, like -160239495.
     */
    public static final Codec<ColorPalette> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Color.HEX_STRING_CODEC.listOf().fieldOf("colors").forGetter(ColorPalette::toColorList))
            .apply(instance, ColorPalette::new));

    public ColorPalette {
        Preconditions.checkState(colors.length > 0, "Cannot create palette that's empty.");
        Preconditions.checkState(colors.length <= 256, "Palette cannot have more than 256 colors.");
        Preconditions.checkState(colors.length < 256 || colors[255] == Color.TRANSPARENT.getARGB(),
                "Color at index 255 (last) should always be transparent.");

        if (colors.length < 256) {
            int[] newColors = new int[256];
            Arrays.fill(newColors, Color.BLACK.getARGB());
            System.arraycopy(colors, 0, newColors, 0, colors.length);
            colors = newColors;
            colors[255] = Color.TRANSPARENT.getARGB();
        }
    }

    public ColorPalette(List<Color> colors) {
        this(colors.stream().mapToInt(Color::getARGB).toArray());
    }

    public List<Color> toColorList() {
        return Arrays.stream(colors).mapToObj(Color::argb).toList();
    }

    public int byId(int id) {
        return colors[id & 0xFF];
    }

    public int closestTo(Color color) {
        if (color.getA() == 0) {
            return 255;
        }

        int closest = 0;
        int closestDistance = Integer.MAX_VALUE;

        for (int i = 0; i < colors.length - 1; i++) { // Without last color which is transparent
            int distance = color.squaredDifferenceTo(colors[i]);
            if (distance < closestDistance) {
                closest = i;
                closestDistance = distance;
            }
        }

        return closest;
    }
}