package io.github.mortuusars.exposure.menu;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.sound.OnePerPlayerSounds;
import io.github.mortuusars.exposure.util.CameraInHand;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class CameraAttachmentsMenu extends AbstractContainerMenu {
    private final int attachmentSlots;
    private final Player player;
    private final Level level;
    private final ItemAndStack<CameraItem> camera;
    private final List<CameraItem.AttachmentType> attachmentTypes;

    private boolean contentsInitialized;

    public CameraAttachmentsMenu(int containerId, Inventory playerInventory, ItemStack cameraStack) {
        super(Exposure.MenuTypes.CAMERA.get(), containerId);
        player = playerInventory.player;
        level = playerInventory.player.level();
        camera = new ItemAndStack<>(cameraStack);
        attachmentTypes = camera.getItem().getAttachmentTypes(camera.getStack());

        SimpleContainer container = new SimpleContainer(getCameraAttachments(camera).toArray(ItemStack[]::new)) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        };

        this.attachmentSlots = addSlotsForAttachments(container);

        addPlayerSlots(playerInventory);
    }

    @Override
    public void initializeContents(int stateId, List<ItemStack> items, ItemStack carried) {
        contentsInitialized = false;
        super.initializeContents(stateId, items, carried);
        contentsInitialized = true;
    }

    protected int addSlotsForAttachments(Container container) {
        int attachmentSlots = 0;

        int[][] slots = new int[][]{
                // SlotId, x, y, maxStackSize
                {CameraItem.FILM_ATTACHMENT.slot(), 13, 42, 1},
                {CameraItem.FLASH_ATTACHMENT.slot(), 147, 15, 1},
                {CameraItem.LENS_ATTACHMENT.slot(), 147, 43, 1},
                {CameraItem.FILTER_ATTACHMENT.slot(), 147, 71, 1}
        };

        for (int[] slot : slots) {
            Optional<CameraItem.AttachmentType> attachment = camera.getItem()
                    .getAttachmentTypeForSlot(camera.getStack(), slot[0]);

            if (attachment.isPresent()) {
                addSlot(new FilteredSlot(container, slot[0], slot[1], slot[2], slot[3],
                        this::onItemInSlotChanged, attachment.get().stackValidator()));
                attachmentSlots++;
            }
        }

        return attachmentSlots;
    }

    protected void addPlayerSlots(Inventory playerInventory) {
        //Player Inventory
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, (column + row * 9) + 9, column * 18 + 8, 103 + row * 18));
            }
        }

        //Hotbar
        for (int slot = 0; slot < 9; slot++) {
            int finalSlot = slot;
            addSlot(new Slot(playerInventory, finalSlot, slot * 18 + 8, 161) {
                @Override
                public boolean mayPickup(@NotNull Player player) {
                    return super.mayPickup(player) && player.getInventory().selected != finalSlot;
                }
            });
        }
    }

    protected void onItemInSlotChanged(FilteredSlot.SlotChangedArgs args) {
        if (!level.isClientSide) {
            camera.getItem().getAttachmentTypeForSlot(camera.getStack(), args.slot().getSlotId())
                    .ifPresent(attachmentType -> camera.getItem()
                            .setAttachment(camera.getStack(), attachmentType, args.newStack()));
            return;
        }

        if (!contentsInitialized)
            return;

        int slotId = args.slot().getSlotId();
        ItemStack oldStack = args.oldStack();
        ItemStack newStack = args.newStack();

        if (slotId == CameraItem.FILM_ATTACHMENT.slot()) {
            if (!newStack.isEmpty())
                OnePerPlayerSounds.play(player, Exposure.SoundEvents.FILM_ADVANCE.get(), SoundSource.PLAYERS, 0.9f, 1f);
        } else if (slotId == CameraItem.FLASH_ATTACHMENT.slot()) {
            if (!newStack.isEmpty())
                OnePerPlayerSounds.play(player, Exposure.SoundEvents.CAMERA_BUTTON_CLICK.get(), SoundSource.PLAYERS, 0.8f, 1f);
        } else if (slotId == CameraItem.LENS_ATTACHMENT.slot()) {
            if (!oldStack.is(newStack.getItem())) {
                OnePerPlayerSounds.play(player, newStack.isEmpty() ?
                        SoundEvents.SPYGLASS_STOP_USING : SoundEvents.SPYGLASS_USE, SoundSource.PLAYERS, 0.9f, 1f);
            }
        } else if (slotId == CameraItem.FILTER_ATTACHMENT.slot()) {
            if (!newStack.isEmpty() && !oldStack.is(newStack.getItem())) {
                OnePerPlayerSounds.play(player, Exposure.SoundEvents.FILTER_PLACE.get(), SoundSource.PLAYERS, 0.8f,
                        level.getRandom().nextFloat() * 0.2f + 0.9f);
            }
        }
    }

    private static NonNullList<ItemStack> getCameraAttachments(ItemAndStack<CameraItem> camera) {
        NonNullList<ItemStack> items = NonNullList.create();

        List<CameraItem.AttachmentType> attachmentTypes = camera.getItem().getAttachmentTypes(camera.getStack());
        for (CameraItem.AttachmentType attachmentType : attachmentTypes) {
            items.add(camera.getItem().getAttachment(camera.getStack(), attachmentType).orElse(ItemStack.EMPTY));
        }

        return items;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int slotIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot clickedSlot = this.slots.get(slotIndex);
        if (clickedSlot.hasItem()) {
            ItemStack slotStack = clickedSlot.getItem();
            itemstack = slotStack.copy();
            if (slotIndex < attachmentSlots) {
                if (!this.moveItemStackTo(slotStack, attachmentSlots, this.slots.size(), true))
                    return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(slotStack, 0, attachmentSlots, false))
                    return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty())
                clickedSlot.set(ItemStack.EMPTY);
            else
                clickedSlot.setChanged();
        }

        return itemstack;
    }

    /**
     * Fixed method to respect slot photo limit.
     */
    @Override
    protected boolean moveItemStackTo(ItemStack movedStack, int startIndex, int endIndex, boolean reverseDirection) {
        boolean hasRemainder = false;
        int i = startIndex;
        if (reverseDirection) {
            i = endIndex - 1;
        }
        if (movedStack.isStackable()) {
            while (!movedStack.isEmpty() && !(!reverseDirection ? i >= endIndex : i < startIndex)) {
                Slot slot = this.slots.get(i);
                ItemStack slotStack = slot.getItem();
                if (!slotStack.isEmpty() && ItemStack.isSameItemSameTags(movedStack, slotStack)) {
                    int maxSize;
                    int j = slotStack.getCount() + movedStack.getCount();
                    if (j <= (maxSize = Math.min(slot.getMaxStackSize(), movedStack.getMaxStackSize()))) {
                        movedStack.setCount(0);
                        slotStack.setCount(j);
                        slot.setChanged();
                        hasRemainder = true;
                    } else if (slotStack.getCount() < maxSize) {
                        movedStack.shrink(maxSize - slotStack.getCount());
                        slotStack.setCount(maxSize);
                        slot.setChanged();
                        hasRemainder = true;
                    }
                }
                if (reverseDirection) {
                    --i;
                    continue;
                }
                ++i;
            }
        }
        if (!movedStack.isEmpty()) {
            i = reverseDirection ? endIndex - 1 : startIndex;
            while (!(!reverseDirection ? i >= endIndex : i < startIndex)) {
                Slot slot1 = this.slots.get(i);
                ItemStack itemmovedStack1 = slot1.getItem();
                if (itemmovedStack1.isEmpty() && slot1.mayPlace(movedStack)) {
                    if (movedStack.getCount() > slot1.getMaxStackSize()) {
                        slot1.setByPlayer(movedStack.split(slot1.getMaxStackSize()));
                    } else {
                        slot1.setByPlayer(movedStack.split(movedStack.getCount()));
                    }
                    slot1.setChanged();
                    hasRemainder = true;
                    break;
                }
                if (reverseDirection) {
                    --i;
                    continue;
                }
                ++i;
            }
        }
        return hasRemainder;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return player.getMainHandItem().getItem() instanceof CameraItem
                || player.getOffhandItem().getItem() instanceof CameraItem;
    }

    public static CameraAttachmentsMenu fromBuffer(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        return new CameraAttachmentsMenu(containerId, playerInventory, buffer.readItem());
    }
}
