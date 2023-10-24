package io.github.mortuusars.exposure.menu;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.block.entity.LightroomBlockEntity;
import io.github.mortuusars.exposure.camera.film.FilmType;
import io.github.mortuusars.exposure.item.DevelopedFilmItem;
import io.github.mortuusars.exposure.item.IFilmItem;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class LightroomMenu extends AbstractContainerMenu {
    public static final int PRINT_BUTTON_ID = 0;
    public static final int PREVIOUS_FRAME_BUTTON_ID = 1;
    public static final int NEXT_FRAME_BUTTON_ID = 2;

    private final LightroomBlockEntity lightroomBlockEntity;
    private final ContainerData data;

    private ListTag frames = new ListTag();

    public LightroomMenu(int containerId, final Inventory playerInventory, final LightroomBlockEntity blockEntity, ContainerData containerData) {
        super(Exposure.MenuTypes.LIGHTROOM.get(), containerId);
        this.lightroomBlockEntity = blockEntity;
        this.data = containerData;

        IItemHandler itemHandler = blockEntity.getInventory();
        {
            this.addSlot(new SlotItemHandler(itemHandler, LightroomBlockEntity.FILM_SLOT, -20, 42) {
                @Override
                public void setChanged() {
                    super.setChanged();
                    frames = getItem().getItem() instanceof DevelopedFilmItem developedFilm ?
                            developedFilm.getExposedFrames(getItem()) : new ListTag();
                }
            });

            this.addSlot(new SlotItemHandler(itemHandler, LightroomBlockEntity.PAPER_SLOT, 8, 92));
            this.addSlot(new SlotItemHandler(itemHandler, LightroomBlockEntity.CYAN_SLOT, 42, 92));
            this.addSlot(new SlotItemHandler(itemHandler, LightroomBlockEntity.MAGENTA_SLOT, 60, 92));
            this.addSlot(new SlotItemHandler(itemHandler, LightroomBlockEntity.YELLOW_SLOT, 78, 92));
            this.addSlot(new SlotItemHandler(itemHandler, LightroomBlockEntity.BLACK_SLOT, 96, 92));

            // OUTPUT
            this.addSlot(new SlotItemHandler(itemHandler, LightroomBlockEntity.RESULT_SLOT, 148, 92) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return false;
                }

                @Override
                public void onTake(Player player, ItemStack pStack) {
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

    public static LightroomMenu fromBuffer(int containerID, Inventory playerInventory, FriendlyByteBuf buffer) {
        return new LightroomMenu(containerID, playerInventory, getBlockEntity(playerInventory, buffer),
                new SimpleContainerData(LightroomBlockEntity.CONTAINER_DATA_SIZE));
    }

    public LightroomBlockEntity getBlockEntity() {
        return lightroomBlockEntity;
    }

    public ContainerData getData() {
        return data;
    }

    public ListTag getExposedFrames() {
        return frames;
    }

    public String getFrameIdByIndex(int index) {
        return index >= 0 && index < getExposedFrames().size() ? getExposedFrames().getCompound(index).getString("Id") : "";
    }

    public boolean isColorFilm() {
        return getSlot(LightroomBlockEntity.FILM_SLOT).getItem().getItem() instanceof IFilmItem filmItem && filmItem.getType() == FilmType.COLOR;
    }

    public int getSelectedFrame() {
        return data.get(LightroomBlockEntity.CONTAINER_DATA_SELECTED_FRAME_ID);
    }

    public int getTotalFrames() {
        ItemStack filmStack = lightroomBlockEntity.getItem(LightroomBlockEntity.FILM_SLOT);
        return (!filmStack.isEmpty() && filmStack.getItem() instanceof IFilmItem filmItem) ? filmItem.getExposedFramesCount(filmStack) : 0;
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        Preconditions.checkState(!player.level.isClientSide, "This should be server-side only.");

        if (buttonId == PREVIOUS_FRAME_BUTTON_ID || buttonId == NEXT_FRAME_BUTTON_ID) {
            ItemStack filmStack = lightroomBlockEntity.getItem(LightroomBlockEntity.FILM_SLOT);
            if (!filmStack.isEmpty() && filmStack.getItem() instanceof DevelopedFilmItem) {
                int frames = getTotalFrames();
                if (frames == 0)
                    return true;

                int selectedFrame = data.get(LightroomBlockEntity.CONTAINER_DATA_SELECTED_FRAME_ID);
                selectedFrame = selectedFrame + (buttonId == NEXT_FRAME_BUTTON_ID ? 1 : -1);
                selectedFrame = Mth.clamp(selectedFrame, 0, frames - 1);
                data.set(LightroomBlockEntity.CONTAINER_DATA_SELECTED_FRAME_ID, selectedFrame);
                return true;
            }
        }

        if (buttonId == PRINT_BUTTON_ID) {
            lightroomBlockEntity.startPrintingProcess(false);
            return true;
        }

        return false;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = slots.get(index);
        ItemStack clickedStack = slot.getItem();
        ItemStack returnedStack = clickedStack.copy();

         if (index < LightroomBlockEntity.SLOTS) {
            if (!moveItemStackTo(clickedStack, LightroomBlockEntity.SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }

            if (index == LightroomBlockEntity.RESULT_SLOT)
                slot.onQuickCraft(clickedStack, returnedStack);
        }
        else if (index < slots.size()) {
            if (!moveItemStackTo(clickedStack, 0, LightroomBlockEntity.SLOTS, false))
                return ItemStack.EMPTY;
        }

        if (clickedStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return returnedStack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return lightroomBlockEntity.stillValid(player);
    }

    private static LightroomBlockEntity getBlockEntity(final Inventory playerInventory, final FriendlyByteBuf data) {
        Objects.requireNonNull(playerInventory, "playerInventory cannot be null");
        Objects.requireNonNull(data, "data cannot be null");
        final BlockEntity blockEntityAtPos = playerInventory.player.level.getBlockEntity(data.readBlockPos());
        if (blockEntityAtPos instanceof LightroomBlockEntity blockEntity)
            return blockEntity;
        throw new IllegalStateException("Block entity is not correct! " + blockEntityAtPos);
    }

    public boolean isPrinting() {
        return data.get(LightroomBlockEntity.CONTAINER_DATA_PRINT_TIME_ID) > 0;
    }
}
