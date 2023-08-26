package io.github.mortuusars.exposure.menu.inventory;

import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CameraItemStackHandler extends ItemStackHandler {
    private final ItemAndStack<CameraItem> camera;

    public CameraItemStackHandler(ItemAndStack<CameraItem> camera) {
        super(getCameraInventory(camera));
        this.camera = camera;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return camera.getItem().getAttachmentTypeForSlot(camera.getStack(), slot)
                .map(type -> type.stackValidator().test(stack))
                .orElse(false);
    }

    @Override
    protected void onContentsChanged(int slot) {
        camera.getItem().getAttachmentTypeForSlot(camera.getStack(), slot)
                .ifPresent(attachmentType -> camera.getItem().setAttachment(camera.getStack(), attachmentType, stacks.get(slot)));
    }

    private static NonNullList<ItemStack> getCameraInventory(ItemAndStack<CameraItem> camera) {
        NonNullList<ItemStack> items = NonNullList.create();

        List<CameraItem.AttachmentType> attachmentTypes = camera.getItem().getAttachmentTypes(camera.getStack());
        for (CameraItem.AttachmentType attachmentType : attachmentTypes) {
            items.add(camera.getItem().getAttachment(camera.getStack(), attachmentType).orElse(ItemStack.EMPTY));
        }

        return items;
    }
}
