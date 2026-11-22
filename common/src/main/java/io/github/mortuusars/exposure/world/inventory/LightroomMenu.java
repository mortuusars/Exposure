package io.github.mortuusars.exposure.world.inventory;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.block.entity.Lightroom;
import io.github.mortuusars.exposure.world.block.entity.LightroomBlockEntity;
import io.github.mortuusars.exposure.world.lightroom.PrintingMode;
import io.github.mortuusars.exposure.world.item.DevelopedFilmItem;
import io.github.mortuusars.exposure.world.item.FilmItem;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class LightroomMenu extends AbstractContainerMenu {
    public static final int PRINT_BUTTON_ID = 0;
    public static final int PRINT_CREATIVE_BUTTON_ID = 1;
    public static final int PREVIOUS_FRAME_BUTTON_ID = 2;
    public static final int NEXT_FRAME_BUTTON_ID = 3;
    public static final int TOGGLE_PROCESS_BUTTON_ID = 4;

    private final LightroomBlockEntity lightroomBlockEntity;
    private final ContainerData data;

    private List<Frame> frames = Collections.emptyList();

    public LightroomMenu(int containerId, final Inventory playerInventory, final LightroomBlockEntity blockEntity, ContainerData containerData) {
        super(Exposure.MenuTypes.LIGHTROOM.get(), containerId);
        this.lightroomBlockEntity = blockEntity;
        this.data = containerData;

        {
            this.addSlot(new Slot(blockEntity, Lightroom.FILM_SLOT, -20, 42) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return blockEntity.isItemValidForSlot(Lightroom.FILM_SLOT, stack);
                }

                @Override
                public void setChanged() {
                    frames = getItem().getItem() instanceof DevelopedFilmItem developedFilm ?
                            developedFilm.getStoredFrames(getItem()) : Collections.emptyList();
                    if (lightroomBlockEntity.getLevel() != null && !lightroomBlockEntity.getLevel().isClientSide()) {
                        data.set(LightroomBlockEntity.CONTAINER_DATA_SELECTED_FRAME_ID, 0);
                    }
                    super.setChanged();
                }
            });

            this.addSlot(new Slot(blockEntity, Lightroom.PAPER_SLOT, 8, 92){
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return blockEntity.isItemValidForSlot(Lightroom.PAPER_SLOT, stack);
                }
            });
            this.addSlot(new Slot(blockEntity, Lightroom.CYAN_SLOT, 42, 92){
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return blockEntity.isItemValidForSlot(Lightroom.CYAN_SLOT, stack);
                }
            });
            this.addSlot(new Slot(blockEntity, Lightroom.MAGENTA_SLOT, 60, 92){
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return blockEntity.isItemValidForSlot(Lightroom.MAGENTA_SLOT, stack);
                }
            });
            this.addSlot(new Slot(blockEntity, Lightroom.YELLOW_SLOT, 78, 92){
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return blockEntity.isItemValidForSlot(Lightroom.YELLOW_SLOT, stack);
                }
            });
            this.addSlot(new Slot(blockEntity, Lightroom.BLACK_SLOT, 96, 92){
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return blockEntity.isItemValidForSlot(Lightroom.BLACK_SLOT, stack);
                }
            });

            // OUTPUT
            this.addSlot(new Slot(blockEntity, Lightroom.RESULT_SLOT, 148, 92) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return false;
                }

                @Override
                public void onTake(@NotNull Player player, @NotNull ItemStack pStack) {
                    super.onTake(player, pStack);
                    blockEntity.dropStoredExperience(player);
                }

                @Override
                public void onQuickCraft(@NotNull ItemStack oldStackIn, @NotNull ItemStack newStackIn) {
                    super.onQuickCraft(oldStackIn, newStackIn);
                    blockEntity.dropStoredExperience(playerInventory.player);
                }
            });
        }

        // Player inventory slots
        for(int row = 0; row < 3; ++row) {
            for(int column = 0; column < 9; ++column) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 127 + row * 18));
            }
        }

        // Player hotbar slots
        // Hotbar should go after main inventory for Shift+Click to work properly.
        for(int index = 0; index < 9; ++index) {
            this.addSlot(new Slot(playerInventory, index, 8 + index * 18, 185));
        }

        this.addDataSlots(data);
    }

    public static LightroomMenu fromBuffer(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        return new LightroomMenu(containerId, playerInventory, getBlockEntity(playerInventory, buffer),
                new SimpleContainerData(LightroomBlockEntity.CONTAINER_DATA_SIZE));
    }

    public LightroomBlockEntity getBlockEntity() {
        return lightroomBlockEntity;
    }

    public ContainerData getData() {
        return data;
    }

    public List<Frame> getExposedFrames() {
        return frames;
    }

    public @Nullable Frame getFrameByIndex(int index) {
        return index >= 0 && index < getExposedFrames().size() ? getExposedFrames().get(index) : null;
    }

    public int getSelectedFrame() {
        return data.get(LightroomBlockEntity.CONTAINER_DATA_SELECTED_FRAME_ID);
    }

    public boolean isPrinting() {
        return data.get(LightroomBlockEntity.CONTAINER_DATA_PRINT_TIME_ID) > 0;
    }

    public int getTotalFramesCount() {
        ItemStack filmStack = getBlockEntity().getItem(Lightroom.FILM_SLOT);
        return (!filmStack.isEmpty() && filmStack.getItem() instanceof FilmItem filmItem) ? filmItem.getStoredFramesCount(filmStack) : 0;
    }

    public boolean canChangeProcess() {
        return getBlockEntity().isRefracted();
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId == PRINT_BUTTON_ID) {
            if (!player.level().isClientSide()) {
                getBlockEntity().startPrintingProcess(false);
            }
            return true;
        }

        if (buttonId == PRINT_CREATIVE_BUTTON_ID) {
            if (player.isCreative() && !player.level().isClientSide()) {
                getBlockEntity().printFrameInCreative();
            }
            return true;
        }

        if (buttonId == PREVIOUS_FRAME_BUTTON_ID || buttonId == NEXT_FRAME_BUTTON_ID) {
            int framesCount = getTotalFramesCount();
            if (framesCount > 0 && !player.level().isClientSide()) {
                int selectedFrame = data.get(LightroomBlockEntity.CONTAINER_DATA_SELECTED_FRAME_ID);
                int change = buttonId == NEXT_FRAME_BUTTON_ID ? 1 : -1;
                selectedFrame = selectedFrame + change;
                selectedFrame = Mth.clamp(selectedFrame, 0, framesCount - 1);
                data.set(LightroomBlockEntity.CONTAINER_DATA_SELECTED_FRAME_ID, selectedFrame);
            }
            return true;
        }

        if (buttonId == TOGGLE_PROCESS_BUTTON_ID) {
            PrintingMode currentProcess = getBlockEntity().getActualPrintingMode();
            getBlockEntity().setPrintMode(currentProcess.cycle());
            return true;
        }

        return false;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = slots.get(index);
        ItemStack clickedStack = slot.getItem();
        ItemStack returnedStack = clickedStack.copy();

         if (index < Lightroom.SLOTS) {
            if (!moveItemStackTo(clickedStack, Lightroom.SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }

            if (index == Lightroom.RESULT_SLOT)
                slot.onQuickCraft(clickedStack, returnedStack);
        }
        else if (index < slots.size()) {
            if (!moveItemStackTo(clickedStack, 0, Lightroom.SLOTS, false))
                return ItemStack.EMPTY;
        }

        if (clickedStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return returnedStack;
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
        return lightroomBlockEntity.stillValid(player);
    }

    private static LightroomBlockEntity getBlockEntity(final Inventory playerInventory, final FriendlyByteBuf data) {
        Objects.requireNonNull(playerInventory, "playerInventory cannot be null");
        Objects.requireNonNull(data, "data cannot be null");
        final BlockEntity blockEntityAtPos = playerInventory.player.level().getBlockEntity(data.readBlockPos());
        if (blockEntityAtPos instanceof LightroomBlockEntity blockEntity)
            return blockEntity;
        throw new IllegalStateException("Block entity is not correct! " + blockEntityAtPos);
    }
}
