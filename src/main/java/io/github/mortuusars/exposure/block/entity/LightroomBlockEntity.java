package io.github.mortuusars.exposure.block.entity;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.block.LightroomBlock;
import io.github.mortuusars.exposure.camera.ExposedFrame;
import io.github.mortuusars.exposure.camera.film.FilmType;
import io.github.mortuusars.exposure.config.Config;
import io.github.mortuusars.exposure.item.DevelopedFilmItem;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.item.StackedPhotographsItem;
import io.github.mortuusars.exposure.menu.LightroomMenu;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LightroomBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int SLOTS = 3;
    public static final int FILM_SLOT = 0;
    public static final int PAPER_SLOT = 1;
    public static final int RESULT_SLOT = 2;

    public static final int[] INPUT_SLOTS = new int[] { 0, 1 };
    public static final int[] OUTPUT_SLOTS = new int[] { 2 };

    public static final int CONTAINER_DATA_SIZE = 3;
    public static final int CONTAINER_DATA_PROGRESS_ID = 0;
    public static final int CONTAINER_DATA_PRINT_TIME_ID = 1;
    public static final int CONTAINER_DATA_CURRENT_FRAME_ID = 2;

    protected final ContainerData containerData = new ContainerData() {
        public int get(int id) {
            return switch (id) {
                case CONTAINER_DATA_PROGRESS_ID -> LightroomBlockEntity.this.progress;
                case CONTAINER_DATA_PRINT_TIME_ID -> LightroomBlockEntity.this.printTime;
                case CONTAINER_DATA_CURRENT_FRAME_ID -> LightroomBlockEntity.this.currentFrame;
                default -> 0;
            };
        }

        public void set(int id, int value) {
            if (id == CONTAINER_DATA_PROGRESS_ID)
                LightroomBlockEntity.this.progress = value;
            else if (id == CONTAINER_DATA_PRINT_TIME_ID)
                LightroomBlockEntity.this.printTime = value;
            else if (id == CONTAINER_DATA_CURRENT_FRAME_ID)
                LightroomBlockEntity.this.currentFrame = value;
        }

        public int getCount() {
            return CONTAINER_DATA_SIZE;
        }
    };

    protected final ItemStackHandler inventory;
    private final LazyOptional<IItemHandler> inventoryHandler;
    protected int currentFrame = 0;
    protected int progress = 0;
    protected int printTime = 0;
    protected boolean advanceFrame;

    public LightroomBlockEntity(BlockPos pos, BlockState blockState) {
        super(Exposure.BlockEntityTypes.LIGHTROOM.get(), pos, blockState);
        this.inventory = createItemHandler(SLOTS);
        this.inventoryHandler = LazyOptional.of(() -> inventory);
//        SidedInvWrapper.create(this, Direction.DOWN);
    }

    public static <T extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockState blockState, T blockEntity) {
        if (blockEntity instanceof LightroomBlockEntity lightroomBlockEntity)
            lightroomBlockEntity.tick();
    }

    private void tick() {
        if (printTime > 0 && canPrint()) {
            if (progress >= printTime) {
                if (tryPrint()) {
                    if (advanceFrame) {
                        ItemStack filmStack = getItem(FILM_SLOT);
                        if (filmStack.getItem() instanceof DevelopedFilmItem developedFilmItem) {
                            int exposedFramesCount = developedFilmItem.getExposedFramesCount(filmStack);
                            if (currentFrame >= exposedFramesCount - 1)
                                ejectFilm();
                            else
                                currentFrame++;
                        }
                    }
                }
                stopPrintingProcess();
            }
            else
                progress++;
        }
        else {
            stopPrintingProcess();
        }
    }

    private void ejectFilm() {
        if (level == null || level.isClientSide || getItem(FILM_SLOT).isEmpty())
            return;

        BlockPos pos = getBlockPos();
        Direction facing = level.getBlockState(pos).getValue(LightroomBlock.FACING);
        Vec3i normal = facing.getNormal();
        Vec3 point = Vec3.atCenterOf(pos).add(normal.getX() * 0.75f, normal.getY() * 0.75f, normal.getZ() * 0.75f);
        ItemEntity itemEntity = new ItemEntity(level, point.x, point.y, point.z, removeItem(FILM_SLOT, 1));
        itemEntity.setDeltaMovement(normal.getX() * 0.05f, normal.getY() * 0.05f + 0.15f, normal.getZ() * 0.05f);
        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);
    }

    public int getCurrentFrame() {
        return currentFrame;
    }

    public void startPrintingProcess(boolean advanceFrame) {
        if (canPrint()) {
            ItemStack filmStack = getItem(FILM_SLOT);
            if (filmStack.getItem() instanceof DevelopedFilmItem developedFilmItem) {
                printTime = developedFilmItem.getType() == FilmType.COLOR ?
                        Config.Common.LIGHTROOM_COLOR_FILM_PRINT_TIME.get() :
                        Config.Common.LIGHTROOM_BW_FILM_PRINT_TIME.get();
                this.advanceFrame = advanceFrame;
                if (level != null)
                    level.setBlock(getBlockPos(), level.getBlockState(getBlockPos()).setValue(LightroomBlock.LIT, true), Block.UPDATE_CLIENTS);
            }
        }
    }

    public void stopPrintingProcess() {
        progress = 0;
        printTime = 0;
        advanceFrame = false;
        if (level != null)
            level.setBlock(getBlockPos(), level.getBlockState(getBlockPos()).setValue(LightroomBlock.LIT, false), Block.UPDATE_CLIENTS);
    }

    public boolean isPrinting() {
        return printTime > 0;
    }

    public boolean canPrint() {
        if (getItem(PAPER_SLOT).isEmpty())
            return false;

        ItemStack filmStack = getItem(FILM_SLOT);
        if (!(filmStack.getItem() instanceof DevelopedFilmItem developedFilm) || !developedFilm.hasExposedFrame(filmStack, currentFrame))
            return false;

        ItemStack resultStack = getItem(RESULT_SLOT);
        return resultStack.isEmpty() || resultStack.getItem() instanceof PhotographItem
                || (resultStack.getItem() instanceof StackedPhotographsItem stackedPhotographsItem
                    && stackedPhotographsItem.canAddPhotograph(resultStack));
    }

    public boolean tryPrint() {
        if (!canPrint())
            return false;

        ItemAndStack<DevelopedFilmItem> film = new ItemAndStack<>(getItem(FILM_SLOT));
        List<ExposedFrame> frames = film.getItem().getExposedFrames(film.getStack());
        if (currentFrame >= frames.size())
            return false;

        ExposedFrame exposureFrame = frames.get(currentFrame);

        PhotographItem photographItem = Exposure.Items.PHOTOGRAPH.get();
        ItemStack photographStack = new ItemStack(photographItem);
        exposureFrame.save(photographStack.getOrCreateTag());
        photographItem.setId(photographStack, exposureFrame.id);

        ItemStack resultStack = getItem(RESULT_SLOT);
        if (resultStack.isEmpty())
            resultStack = photographStack;
        else if (resultStack.getItem() instanceof PhotographItem) {
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
            return false;
        }

        setItem(RESULT_SLOT, resultStack);
        getItem(PAPER_SLOT).shrink(1);

        if (level != null)
            level.playSound(null, getBlockPos(), Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(), SoundSource.PLAYERS, 1f, 1f);

        return true;
    }


    // Container

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("block.exposure.lightroom");
    }

    @Override
    protected @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory) {
        return new LightroomMenu(containerId, playerInventory, this, containerData);
    }

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

    public static boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot == FILM_SLOT) return stack.getItem() instanceof DevelopedFilmItem;
        else if (slot == PAPER_SLOT) return stack.is(Items.PAPER);
        else if (slot == RESULT_SLOT) return stack.getItem() instanceof PhotographItem;
        return false;
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
    public int @NotNull [] getSlotsForFace(@NotNull Direction face) {
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
        this.currentFrame = tag.getInt("CurrentFrame");
        this.progress = tag.getInt("Progress");
        this.printTime = tag.getInt("PrintTime");
        this.advanceFrame = tag.getBoolean("AdvanceFrame");
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", this.inventory.serializeNBT());
        if (currentFrame > 0)
            tag.putInt("CurrentFrame", currentFrame);
        if (progress > 0)
            tag.putInt("Progress", progress);
        if (printTime > 0)
            tag.putInt("PrintTime", printTime);
        if (advanceFrame)
            tag.putBoolean("AdvanceFrame", true);
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
}
