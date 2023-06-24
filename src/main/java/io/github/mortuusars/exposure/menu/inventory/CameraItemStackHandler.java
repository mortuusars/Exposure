package io.github.mortuusars.exposure.menu.inventory;

import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.item.FilmItem;
import io.github.mortuusars.exposure.item.attachment.CameraAttachments;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpyglassItem;
import net.minecraftforge.common.Tags;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

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
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return (slot == CameraItem.FILM && stack.getItem() instanceof FilmItem)
                || (slot == CameraItem.LENS && stack.getItem() instanceof SpyglassItem)
                || (slot == CameraItem.FILTER && stack.is(Tags.Items.GLASS_PANES));
    }

    @Override
    protected void onContentsChanged(int slot) {
        cameraItem.attachmentsChanged(player, cameraStack, slot, stacks.get(slot));
    }

    private static NonNullList<ItemStack> getCameraInventory(ItemStack cameraStack) {
        CameraItem cameraItem = (CameraItem) cameraStack.getItem();
        CameraAttachments attachments = cameraItem.getAttachments(cameraStack);

        NonNullList<ItemStack> items = NonNullList.create();


        for (Map.Entry<Integer, String> attachmentSlot : CameraItem.SLOTS.int2ObjectEntrySet()) {
            items.add(attachments.getAttachment(attachmentSlot.getValue()));
        }

        return items;
    }
}
