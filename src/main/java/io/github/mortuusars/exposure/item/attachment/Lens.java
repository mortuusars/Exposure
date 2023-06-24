package io.github.mortuusars.exposure.item.attachment;

import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.world.item.ItemStack;

public class Lens {
    private final MinMaxBounds<Integer> focalRange;
    private int currentFocalLength;

    public Lens(MinMaxBounds<Integer> focalRange, int currentFocalLength) {
        this.focalRange = focalRange;
        this.currentFocalLength = currentFocalLength;
    }


}
