package io.github.mortuusars.exposure.menu.inventory;

import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.menu.CameraMenu;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class CameraItemStackHandler extends ItemStackHandler {
    private final Player player;
    private final ItemStack cameraStack;
    private final CameraItem cameraItem;

    public CameraItemStackHandler(Player player, ItemStack cameraStack) {
        super(getCameraInventory(cameraStack));
        this.player = player;
        this.cameraStack = cameraStack;
        this.cameraItem = ((CameraItem) cameraStack.getItem());
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (slot == CameraMenu.FILM_SLOT) {
            cameraItem.setFilm(cameraStack, stacks.get(slot));
        }
        else if (slot == CameraMenu.LENS_SLOT) {
            cameraItem.setLens(cameraStack, stacks.get(slot));
        }

    }

    private static NonNullList<ItemStack> getCameraInventory(ItemStack cameraStack) {
        CameraItem cameraItem = (CameraItem) cameraStack.getItem();
        return NonNullList.of(ItemStack.EMPTY,
                cameraItem.getLoadedFilm(cameraStack),
                cameraItem.getLens(cameraStack),
                ItemStack.EMPTY);
    }
}
