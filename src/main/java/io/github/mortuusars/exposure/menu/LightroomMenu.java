package io.github.mortuusars.exposure.menu;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.block.entity.LightroomBlockEntity;
import io.github.mortuusars.exposure.camera.ExposureFrame;
import io.github.mortuusars.exposure.item.DevelopedFilmItem;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.item.StackedPhotographsItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class LightroomMenu extends AbstractContainerMenu {
    public static final int PRINT_BUTTON_ID = 0;
    public static final int PREVIOUS_FRAME_BUTTON_ID = 1;
    public static final int NEXT_FRAME_BUTTON_ID = 2;

    private final LightroomBlockEntity lightroomBlockEntity;

    private final ContainerData data;
    public LightroomMenu(int containerId, final Inventory playerInventory, final LightroomBlockEntity blockEntity, ContainerData containerData) {
        super(Exposure.MenuTypes.LIGHTROOM.get(), containerId);
        this.lightroomBlockEntity = blockEntity;
        this.data = containerData;

        IItemHandler itemHandler = blockEntity.getInventory();
        {
            this.addSlot(new SlotItemHandler(itemHandler, LightroomBlockEntity.FILM_SLOT, 17, 89));
            this.addSlot(new SlotItemHandler(itemHandler, LightroomBlockEntity.PAPER_SLOT, 35, 89));

            // OUTPUT
            this.addSlot(new SlotItemHandler(itemHandler, LightroomBlockEntity.RESULT_SLOT, 134, 89) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return false;
                }
            });

        }

        // Player inventory slots
        for(int row = 0; row < 3; ++row) {
            for(int column = 0; column < 9; ++column) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 128 + row * 18));
            }
        }

        // Player hotbar slots
        // Hotbar should go after main inventory for Shift+Click to work properly.
        for(int index = 0; index < 9; ++index) {
            this.addSlot(new Slot(playerInventory, index, 8 + index * 18, 186));
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

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        Preconditions.checkState(!player.level.isClientSide, "This should be server-side only.");

        if (buttonId == PREVIOUS_FRAME_BUTTON_ID || buttonId == NEXT_FRAME_BUTTON_ID) {
            ItemStack filmStack = lightroomBlockEntity.getItem(LightroomBlockEntity.FILM_SLOT);
            if (!filmStack.isEmpty() && filmStack.getItem() instanceof DevelopedFilmItem filmItem) {
                List<ExposureFrame> exposedFrames = filmItem.getExposedFrames(filmStack);
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
            ItemStack filmStack = lightroomBlockEntity.getItem(LightroomBlockEntity.FILM_SLOT);
            ItemStack paperStack = lightroomBlockEntity.getItem(LightroomBlockEntity.PAPER_SLOT);
            if (!filmStack.isEmpty() && !paperStack.isEmpty() && filmStack.getItem() instanceof DevelopedFilmItem developedFilmItem) {
                List<ExposureFrame> exposedFrames = developedFilmItem.getExposedFrames(filmStack);
                if (exposedFrames.size() == 0)
                    return true;

                int currentFrame = data.get(LightroomBlockEntity.CONTAINER_DATA_CURRENT_FRAME_ID);
                if (currentFrame >= 0 && currentFrame < exposedFrames.size()) {
                    ExposureFrame exposureFrame = exposedFrames.get(currentFrame);
                    PhotographItem photographItem = Exposure.Items.PHOTOGRAPH.get();
                    ItemStack photographStack = new ItemStack(photographItem);

                    exposureFrame.save(photographStack.getOrCreateTag());
                    photographItem.setId(photographStack, exposureFrame.id);

                    ItemStack resultStack = lightroomBlockEntity.getItem(LightroomBlockEntity.RESULT_SLOT);
                    if (resultStack.isEmpty())
                        resultStack = photographStack;
                    else if (resultStack.getItem() instanceof PhotographItem existingPhotographItem) {
                        StackedPhotographsItem stackedPhotographsItem = Exposure.Items.STACKED_PHOTOGRAPHS.get();
                        ItemStack newStackedPhotographs = new ItemStack(stackedPhotographsItem);
                        stackedPhotographsItem.addPhotographOnTop(newStackedPhotographs, resultStack);
                        stackedPhotographsItem.addPhotographOnTop(newStackedPhotographs, photographStack);
                        resultStack = newStackedPhotographs;
                    }
                    else if (resultStack.getItem() instanceof StackedPhotographsItem stackedPhotographsItem) {
                        stackedPhotographsItem.addPhotographOnTop(resultStack, photographStack);
                    }
                    else {
                        Exposure.LOGGER.error("Unexpected item in result slot: " + resultStack);
                        return true;
                    }

                    lightroomBlockEntity.setItem(LightroomBlockEntity.RESULT_SLOT, resultStack);

                    paperStack.shrink(1);
                    player.level.playSound(null, player, SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1f, 1f);
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = slots.get(index);
        ItemStack clickedStack = slot.getItem();

        if (index < LightroomBlockEntity.SLOTS) {
            if (!moveItemStackTo(clickedStack, LightroomBlockEntity.SLOTS, slots.size(), true))
                return ItemStack.EMPTY;

            // BEs inventory onContentsChanged is not fired when removing agreement by shift clicking.
            // So we force update the slot.
            // This is needed to update agreement-related stuff. (Blockstate was not updating properly to reflect the removal).
//            if (index == DeliveryTableBlockEntity.AGREEMENT_SLOT)
//                blockEntity.setItem(DeliveryTableBlockEntity.AGREEMENT_SLOT, slot.getItem());
        }
        else if (index < slots.size()) {
            if (!moveItemStackTo(clickedStack, 0, LightroomBlockEntity.SLOTS, false))
                return ItemStack.EMPTY;
        }

        return ItemStack.EMPTY;
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
}
