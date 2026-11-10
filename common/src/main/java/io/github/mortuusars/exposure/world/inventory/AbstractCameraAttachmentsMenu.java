package io.github.mortuusars.exposure.world.inventory;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.inventory.slot.FilteredSlot;
import io.github.mortuusars.exposure.world.item.camera.Attachment;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import io.github.mortuusars.mortaar.util.supporter.Supporters;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public abstract class AbstractCameraAttachmentsMenu extends AbstractContainerMenu {
    public static final int SKIN_REGULAR_BUTTON_ID = 100;
    public static final int SKIN_GOLD_BUTTON_ID = 101;

    protected final Player player;
    protected final CameraAccess cameraAccess;
    protected final List<Attachment<?>> attachments;

    protected boolean clientContentsInitialized;

    protected AbstractCameraAttachmentsMenu(@Nullable MenuType<?> menuType, int containerId, Inventory playerInventory, CameraAccess cameraAccess) {
        super(menuType, containerId);
        this.player = playerInventory.player;
        this.cameraAccess = cameraAccess;
        this.attachments = cameraAccess.map((i, s) -> i.getAttachments());

        SimpleContainer container = createAttachmentsContainer();

        addAttachmentSlots(container);
        addPlayerSlots(playerInventory);
    }

    public CameraAccess getCamera() {
        return cameraAccess;
    }

    protected ItemStack getCameraStack() {
        return getCamera().getStack();
    }

    protected @NotNull SimpleContainer createAttachmentsContainer() {
        ItemStack[] attachmentItems = attachments.stream()
                .map(attachment -> attachment.get(getCamera().getStack()).getCopy())
                .toArray(ItemStack[]::new);

        SimpleContainer container = new SimpleContainer(attachmentItems) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        };

        container.addListener(this::onContainerChanged);
        return container;
    }

    protected void onContainerChanged(Container c) {
        for (int slotId = 0; slotId < c.getContainerSize(); slotId++) {
            Attachment<?> attachment = attachments.get(slotId);
            attachment.set(getCamera().getStack(), c.getItem(slotId));
        }
    }

    /**
     * Only called client-side.
     */
    @Override
    public void initializeContents(int stateId, List<ItemStack> items, ItemStack carried) {
        clientContentsInitialized = false;
        super.initializeContents(stateId, items, carried);
        clientContentsInitialized = true;
    }

    protected void addAttachmentSlots(Container container) {
        Map<Attachment<?>, Vector2i> slotPositions = Map.of(
                Attachment.FILM, new Vector2i(13, 42),
                Attachment.FLASH, new Vector2i(147, 15),
                Attachment.LENS, new Vector2i(147, 43),
                Attachment.FILTER, new Vector2i(147, 71));

        for (int index = 0; index < attachments.size(); index++) {
            Attachment<?> attachmentType = attachments.get(index);
            Vector2i pos = slotPositions.get(attachmentType);
            addSlot(new FilteredSlot(container, index, pos.x(), pos.y(), 1,
                    this::onItemInSlotChanged, attachmentType.itemPredicate()));
        }
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
            addSlot(new Slot(playerInventory, slot, slot * 18 + 8, 161));
        }
    }

    protected void onItemInSlotChanged(FilteredSlot.SlotChangedArgs args) {
        int slotId = args.slot().getSlotId();
        ItemStack newStack = args.newStack();

        Attachment<?> attachment = attachments.get(slotId);

        if (clientContentsInitialized) {
            if (!newStack.isEmpty()) {
                attachment.playInsertSoundSided(player);
            } else {
                attachment.playRemoveSoundSided(player);
            }

            getCamera().apply((item, stack) -> item.actionPerformed(stack, player));
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int slotIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot clickedSlot = this.slots.get(slotIndex);
        if (clickedSlot.hasItem()) {
            ItemStack slotStack = clickedSlot.getItem();
            itemstack = slotStack.copy();
            if (slotIndex < attachments.size()) {
                if (!this.moveItemStackTo(slotStack, attachments.size(), this.slots.size(), true))
                    return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(slotStack, 0, attachments.size(), false))
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
     * Fixed method to respect slot limit.
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
                if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(movedStack, slotStack)) {
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
                ItemStack movedStack1 = slot1.getItem();
                if (movedStack1.isEmpty() && slot1.mayPlace(movedStack)) {
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
    public boolean clickMenuButton(Player player, int id) {
        if (Supporters.isEligibleForGoldenRewards(player.getUUID())) {
            if (id == SKIN_REGULAR_BUTTON_ID) {
                getCamera().apply((i, s) -> s.set(Exposure.DataComponents.CAMERA_GOLD, false));
                return true;
            } else if (id == SKIN_GOLD_BUTTON_ID) {
                getCamera().apply((i, s) -> s.set(Exposure.DataComponents.CAMERA_GOLD, true));
                return true;
            }
        }
        return false;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        getCamera().apply((item, stack) -> item.setDisassembled(stack, false));
    }

    public interface CameraAccess {
        ItemStack getStack();

        default void apply(BiConsumer<CameraItem, ItemStack> consumer) {
            ItemStack camera = getStack();
            consumer.accept(((CameraItem) camera.getItem()), camera);
        }

        default <T> T map(BiFunction<CameraItem, ItemStack, T> func) {
            ItemStack camera = getStack();
            return func.apply(((CameraItem) camera.getItem()), camera);
        }
    }
}
