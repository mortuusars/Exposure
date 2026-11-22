package io.github.mortuusars.exposure.world.inventory;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.CameraId;
import io.github.mortuusars.exposure.world.inventory.slot.FilteredSlot;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CameraInHandAttachmentsMenu extends AbstractCameraAttachmentsMenu {
    protected final int cameraSlotIndex;
    protected final boolean openedFromGui;

    public CameraInHandAttachmentsMenu(int containerId, Inventory playerInventory, int cameraSlotIndex, boolean openedFromGui) {
        super(Exposure.MenuTypes.CAMERA_IN_HAND.get(), containerId, playerInventory, new InventoryCameraAccess(playerInventory, cameraSlotIndex));
        this.cameraSlotIndex = cameraSlotIndex;
        this.openedFromGui = openedFromGui;
    }

    public boolean isOpenedFromGui() {
        return openedFromGui;
    }

    @Override
    protected void addPlayerSlots(Inventory playerInventory) {
        //Player Inventory
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, (column + row * 9) + 9, column * 18 + 8, 103 + row * 18) {
                    @Override
                    public boolean mayPickup(@NotNull Player player) {
                        return super.mayPickup(player) && getContainerSlot() != cameraSlotIndex;
                    }

                    @Override
                    public boolean isActive() {
                        return getContainerSlot() != cameraSlotIndex;
                    }

                    @Override
                    public boolean isHighlightable() {
                        return getContainerSlot() != cameraSlotIndex;
                    }
                });
            }
        }

        //Hotbar
        for (int slot = 0; slot < 9; slot++) {
            int finalSlot = slot;
            addSlot(new Slot(playerInventory, finalSlot, slot * 18 + 8, 161) {
                @Override
                public boolean mayPickup(@NotNull Player player) {
                    return super.mayPickup(player) && getContainerSlot() != cameraSlotIndex;
                }

                @Override
                public boolean isActive() {
                    return getContainerSlot() != cameraSlotIndex;
                }

                @Override
                public boolean isHighlightable() {
                    return getContainerSlot() != cameraSlotIndex;
                }
            });
        }
    }

    @Override
    protected void onContainerChanged(Container c) {
        super.onContainerChanged(c);
        if (!player.level().isClientSide() && player.isCreative()) {
            // Fixes item not updating properly when not in "Inventory" tab of creative inventory
            player.getInventory().setItem(cameraSlotIndex, getCameraStack());
        }
    }

    @Override
    protected void onItemInSlotChanged(FilteredSlot.SlotChangedArgs args) {
        super.onItemInSlotChanged(args);

        if (!player.level().isClientSide() && player.isCreative()) {
            // Fixes item not updating properly when not in "Inventory" tab of creative inventory
            player.getInventory().setItem(cameraSlotIndex, getCameraStack());
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        // Without this, client inventory is not syncing properly when menu is closed. (only when opened by r-click in GUI)
        player.inventoryMenu.resumeRemoteUpdates();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return CameraId.ofStack(getCameraStack()).matches(player.getInventory().getItem(cameraSlotIndex));
    }

    public static CameraInHandAttachmentsMenu fromBuffer(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        return new CameraInHandAttachmentsMenu(containerId, playerInventory, buffer.readInt(), buffer.readBoolean());
    }

    public static class InventoryCameraAccess implements CameraAccess {
        protected final Inventory inventory;
        protected final int slot;

        public InventoryCameraAccess(Inventory inventory, int slot) {
            this.inventory = inventory;
            this.slot = slot;
            Preconditions.checkState(getStack().getItem() instanceof CameraItem,
                    "Failed to open access the camera. " + getStack() + " is not a CameraItem.");
        }

        @Override
        public ItemStack getStack() {
            return inventory.getItem(slot);
        }
    }
}
