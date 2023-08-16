package io.github.mortuusars.exposure.menu;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.block.entity.LightroomBlockEntity;
import io.github.mortuusars.exposure.camera.ExposedFrame;
import io.github.mortuusars.exposure.item.DevelopedFilmItem;
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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class LightroomMenu extends AbstractContainerMenu {
    public static final int PRINT_BUTTON_ID = 0;
    public static final int PREVIOUS_FRAME_BUTTON_ID = 1;
    public static final int NEXT_FRAME_BUTTON_ID = 2;

    private final LightroomBlockEntity lightroomBlockEntity;
    private final ContainerData data;

    private List<ExposedFrame> frames = Collections.emptyList();

    public LightroomMenu(int containerId, final Inventory playerInventory, final LightroomBlockEntity blockEntity, ContainerData containerData) {
        super(Exposure.MenuTypes.LIGHTROOM.get(), containerId);
        this.lightroomBlockEntity = blockEntity;
        this.data = containerData;

        IItemHandler itemHandler = blockEntity.getInventory();
        {
            this.addSlot(new SlotItemHandler(itemHandler, LightroomBlockEntity.FILM_SLOT, 17, 90) {
                @Override
                public void setChanged() {
                    super.setChanged();
                    frames = getItem().getItem() instanceof DevelopedFilmItem developedFilm ?
                            developedFilm.getExposedFrames(getItem()) : Collections.emptyList();
                }
            });
            this.addSlot(new SlotItemHandler(itemHandler, LightroomBlockEntity.PAPER_SLOT, 35, 90));

            // OUTPUT
            this.addSlot(new SlotItemHandler(itemHandler, LightroomBlockEntity.RESULT_SLOT, 134, 90) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return false;
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

    public ContainerData getData() {
        return data;
    }

    public List<ExposedFrame> getExposedFrames() {
        return frames;
    }

    public int getCurrentFrame() {
        return data.get(LightroomBlockEntity.CONTAINER_DATA_CURRENT_FRAME_ID);
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        Preconditions.checkState(!player.level.isClientSide, "This should be server-side only.");

        if (buttonId == PREVIOUS_FRAME_BUTTON_ID || buttonId == NEXT_FRAME_BUTTON_ID) {
            ItemStack filmStack = lightroomBlockEntity.getItem(LightroomBlockEntity.FILM_SLOT);
            if (!filmStack.isEmpty() && filmStack.getItem() instanceof DevelopedFilmItem filmItem) {
                List<ExposedFrame> exposedFrames = filmItem.getExposedFrames(filmStack);
                if (exposedFrames.size() == 0)
                    return true;

                int currentFrame = data.get(LightroomBlockEntity.CONTAINER_DATA_CURRENT_FRAME_ID);
                currentFrame = currentFrame + (buttonId == NEXT_FRAME_BUTTON_ID ? 1 : -1);
                currentFrame = Mth.clamp(currentFrame, 0, exposedFrames.size() - 1);
                data.set(LightroomBlockEntity.CONTAINER_DATA_CURRENT_FRAME_ID, currentFrame);
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
            if (!moveItemStackTo(clickedStack, LightroomBlockEntity.SLOTS, slots.size(), true))
                return ItemStack.EMPTY;
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
