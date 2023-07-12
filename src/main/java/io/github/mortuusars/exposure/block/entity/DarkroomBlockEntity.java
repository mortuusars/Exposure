package io.github.mortuusars.exposure.block.entity;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.item.FilmItem;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.menu.DarkroomMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DarkroomBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    public static final int SLOTS = 7;
    public static final int FILM_SLOT = 0;
    public static final int CYAN_DYE_SLOT = 1;
    public static final int MAGENTA_DYE_SLOT = 2;
    public static final int YELLOW_DYE_SLOT = 3;
    public static final int BLACK_DYE_SLOT = 4;
    public static final int PAPER_SLOT = 5;
    public static final int RESULT_SLOT = 6;

    public static final int[] INPUT_SLOTS = new int[] { 0, 1, 2, 3, 4, 5 };
    public static final int[] OUTPUT_SLOTS = new int[] { 6 };

    public static final int CONTAINER_DATA_SIZE = 2;
    public static final int CONTAINER_DATA_PROGRESS_ID = 0;
    public static final int CONTAINER_DATA_CURRENT_FRAME_ID = 1;

    protected final ContainerData containerData = new ContainerData() {
        public int get(int id) {
            return switch (id) {
                case CONTAINER_DATA_PROGRESS_ID -> DarkroomBlockEntity.this.progress;
                case CONTAINER_DATA_CURRENT_FRAME_ID -> DarkroomBlockEntity.this.currentFrame;
                default -> 0;
            };
        }

        public void set(int id, int value) {
            if (id == CONTAINER_DATA_PROGRESS_ID)
                DarkroomBlockEntity.this.progress = value;
            else if (id == CONTAINER_DATA_CURRENT_FRAME_ID)
                DarkroomBlockEntity.this.currentFrame = value;
        }

        public int getCount() {
            return CONTAINER_DATA_SIZE;
        }
    };

    protected final ItemStackHandler inventory;
    private final LazyOptional<IItemHandler> inventoryHandler;
    protected int currentFrame = 0;
    protected int progress = 0;

    public DarkroomBlockEntity(BlockPos pos, BlockState blockState) {
        super(Exposure.BlockEntityTypes.DARKROOM.get(), pos, blockState);
        this.inventory = createItemHandler(SLOTS);
        this.inventoryHandler = LazyOptional.of(() -> inventory);
//        SidedInvWrapper.create(this, Direction.DOWN);
    }

    public static boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot == FILM_SLOT) return stack.getItem() instanceof FilmItem;
        else if (slot == CYAN_DYE_SLOT) return stack.is(Tags.Items.DYES_CYAN);
        else if (slot == MAGENTA_DYE_SLOT) return stack.is(Tags.Items.DYES_MAGENTA);
        else if (slot == YELLOW_DYE_SLOT) return stack.is(Tags.Items.DYES_YELLOW);
        else if (slot == BLACK_DYE_SLOT) return stack.is(Tags.Items.DYES_BLACK);
        else if (slot == PAPER_SLOT) return stack.is(Items.PAPER);
        else if (slot == RESULT_SLOT) return stack.getItem() instanceof PhotographItem;
        return false;
    }

    public static <T extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockState blockState, T blockEntity) {
        if (blockEntity instanceof DarkroomBlockEntity darkroomBlockEntity) {

        }
    }


    // Container

    protected @NotNull ItemStackHandler createItemHandler(int slots) {
        return new ItemStackHandler(slots) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return isItemValidForSlot(slot, stack);
            }

            @NotNull
            @Override
            public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                return super.insertItem(slot, stack, simulate);
            }

            @Override
            protected void onContentsChanged(int slot) {
                inventoryContentsChanged(slot);
            }
        };
    }

    private void inventoryContentsChanged(int slot) {
        if (slot == FILM_SLOT) {
            currentFrame = 0;
        }

        setChanged();
    }

    public IItemHandler getInventory() {
        return inventory;
    }

    @Override
    public int getContainerSize() {
        return inventory.getSlots();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return inventory.getStackInSlot(slot);
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        return inventory.extractItem(slot, amount, false);
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = inventory.getStackInSlot(slot);
        inventory.setStackInSlot(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        inventory.setStackInSlot(slot, stack);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.level != null && this.level.getBlockEntity(this.worldPosition) == this
                && player.distanceToSqr(this.worldPosition.getX() + 0.5D,
                    this.worldPosition.getY() + 0.5D,
                    this.worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public int @NotNull [] getSlotsForFace(Direction face) {
        if (face == Direction.DOWN)
            return OUTPUT_SLOTS;
        return INPUT_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack itemStack, @Nullable Direction direction) {
        if (direction == Direction.DOWN)
            return false;
        return canPlaceItem(index, itemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, @NotNull ItemStack pStack, @NotNull Direction direction) {
        for (int outputSlot : OUTPUT_SLOTS) {
            if (index == outputSlot)
                return true;
        }
        return false;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER)
            return this.inventoryHandler.cast();

        return super.getCapability(capability, side);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        inventoryHandler.invalidate();
    }


    // Load/Save
    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.inventory.deserializeNBT(tag.getCompound("Inventory"));
        this.progress = tag.getInt("Progress");
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", this.inventory.serializeNBT());
        tag.putInt("Progress", progress);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }


    // Sync:

    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null)
            load(packet.getTag());
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.exposure.darkroom");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new DarkroomMenu(containerId, playerInventory, this, containerData);
    }
}
