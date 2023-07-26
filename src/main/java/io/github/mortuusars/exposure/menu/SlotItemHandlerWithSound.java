package io.github.mortuusars.exposure.menu;

import io.github.mortuusars.exposure.Exposure;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SlotItemHandlerWithSound extends SlotItemHandler {
    public SlotItemHandlerWithSound(IItemHandler itemHandler, int index, int x, int y) {
        super(itemHandler, index, x, y);
    }
}
