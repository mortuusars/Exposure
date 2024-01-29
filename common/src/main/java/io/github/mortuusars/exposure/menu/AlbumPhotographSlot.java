package io.github.mortuusars.exposure.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AlbumPhotographSlot extends Slot {
    private boolean isActive;

    public AlbumPhotographSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean value) {
        isActive = value;
    }
}
