package io.github.mortuusars.exposure.block.entity;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.block.LightroomBlock;
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
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
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
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LightroomBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int SLOTS = 7;
    public static final int FILM_SLOT = 0;
    public static final int PAPER_SLOT = 1;
    public static final int CYAN_SLOT = 2;
    public static final int MAGENTA_SLOT = 3;
    public static final int YELLOW_SLOT = 4;
    public static final int BLACK_SLOT = 5;
    public static final int RESULT_SLOT = 6;

    public static final int[] OUTPUT_SLOTS = new int[] { 6 };

    public static final int CONTAINER_DATA_SIZE = 4;
    public static final int CONTAINER_DATA_PROGRESS_ID = 0;
    public static final int CONTAINER_DATA_PRINT_TIME_ID = 1;
    public static final int CONTAINER_DATA_SELECTED_FRAME_ID = 2;
    public static final int CONTAINER_DATA_EJECT_ID = 3;

    protected final ContainerData containerData = new ContainerData() {
        public int get(int id) {
            return switch (id) {
                case CONTAINER_DATA_PROGRESS_ID -> LightroomBlockEntity.this.progress;
                case CONTAINER_DATA_PRINT_TIME_ID -> LightroomBlockEntity.this.printTime;
                case CONTAINER_DATA_SELECTED_FRAME_ID -> LightroomBlockEntity.this.selectedFrame;
                case CONTAINER_DATA_EJECT_ID -> LightroomBlockEntity.this.ejectFilmAfterLastFrame ? 1 : 0;
                default -> 0;
            };
        }

        public void set(int id, int value) {
            if (id == CONTAINER_DATA_PROGRESS_ID)
                LightroomBlockEntity.this.progress = value;
            else if (id == CONTAINER_DATA_PRINT_TIME_ID)
                LightroomBlockEntity.this.printTime = value;
            else if (id == CONTAINER_DATA_SELECTED_FRAME_ID)
                LightroomBlockEntity.this.selectedFrame = value;
            else if (id == CONTAINER_DATA_EJECT_ID)
                LightroomBlockEntity.this.ejectFilmAfterLastFrame = value == 1;
            setChanged();
        }

        public int getCount() {
            return CONTAINER_DATA_SIZE;
        }
    };

    protected final ItemStackHandler inventory;
    protected final LazyOptional<IItemHandler> inventoryHandler;
    protected LazyOptional<IItemHandlerModifiable>[] inventoryHandlers;
    protected int selectedFrame = 0;
    protected int printedFrame = -1;
    protected int progress = 0;
    protected int printTime = 0;
    protected int printedPhotographsCount = 0;
    protected boolean ejectFilmAfterLastFrame;
    protected boolean advanceFrame;

    public LightroomBlockEntity(BlockPos pos, BlockState blockState) {
        super(Exposure.BlockEntityTypes.LIGHTROOM.get(), pos, blockState);
        this.inventory = createItemHandler(SLOTS);
        this.inventoryHandler = LazyOptional.of(() -> inventory);
        inventoryHandlers = SidedInvWrapper.create(this, Direction.DOWN);
    }

    public static <T extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockState blockState, T blockEntity) {
        if (blockEntity instanceof LightroomBlockEntity lightroomBlockEntity)
            lightroomBlockEntity.tick();
    }

//    protected void tick() {
//        if (printTime > 0 && canPrint()) {
//            if (progress >= printTime) {
//                if (tryPrint()) {
//                    onFramePrinted();
//                }
//
//                stopPrintingProcess();
//            }
//            else {
//                progress++;
//                if (progress % 55 == 0 && printTime - progress > 12 && level != null)
//                    level.playSound(null, getBlockPos(), Exposure.SoundEvents.LIGHTROOM_PRINT.get(), SoundSource.BLOCKS,
//                    1f, level.getRandom().nextFloat() * 0.3f + 1f);
//            }
//        }
//        else {
//            stopPrintingProcess();
//        }
//    }

    protected void tick() {
        if (printTime <= 0 || !canPrint()) {
            stopPrintingProcess();
            return;
        }

        if (progress < printTime) {
            progress++;
            if (progress % 55 == 0 && printTime - progress > 12 && level != null)
                level.playSound(null, getBlockPos(), Exposure.SoundEvents.LIGHTROOM_PRINT.get(), SoundSource.BLOCKS,
                        1f, level.getRandom().nextFloat() * 0.3f + 1f);
            return;
        }

        if (tryPrint()) {
            onFramePrinted();
        }

        stopPrintingProcess();
    }


    private void onFramePrinted() {
        ItemAndStack<DevelopedFilmItem> film = new ItemAndStack<>(getItem(FILM_SLOT));

        int frames = film.getItem().getExposedFramesCount(film.getStack());
        if (ejectFilmAfterLastFrame && printedFrame >= frames - 1) {
            tryEjectFilm();
        }
        else if (advanceFrame && selectedFrame < frames - 1) {
            selectedFrame++;
        }
        setChanged();
    }

    public boolean isEjectingFilmAfterLastFrame() {
        return ejectFilmAfterLastFrame;
    }

    public void toggleEjectFilm() {
        ejectFilmAfterLastFrame = !ejectFilmAfterLastFrame;
        setChanged();
    }

    protected void tryEjectFilm() {
        if (level == null || level.isClientSide || getItem(FILM_SLOT).isEmpty())
            return;

        BlockPos pos = getBlockPos();
        Direction facing = level.getBlockState(pos).getValue(LightroomBlock.FACING);

        if (level.getBlockState(pos.relative(facing)).canOcclude())
            return;

        Vec3i normal = facing.getNormal();
        Vec3 point = Vec3.atCenterOf(pos).add(normal.getX() * 0.75f, normal.getY() * 0.75f, normal.getZ() * 0.75f);
        ItemEntity itemEntity = new ItemEntity(level, point.x, point.y, point.z, removeItem(FILM_SLOT, 1));
        itemEntity.setDeltaMovement(normal.getX() * 0.05f, normal.getY() * 0.05f + 0.15f, normal.getZ() * 0.05f);
        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);
    }

    public int getSelectedFrame() {
        return selectedFrame;
    }

    public void startPrintingProcess(boolean advanceFrame) {
        if (!canPrint())
            return;

        printedFrame = selectedFrame;

        ItemStack filmStack = getItem(FILM_SLOT);
        if (!(filmStack.getItem() instanceof DevelopedFilmItem developedFilmItem))
            return;

        printTime = developedFilmItem.getType() == FilmType.COLOR ?
                Config.Common.LIGHTROOM_COLOR_FILM_PRINT_TIME.get() :
                Config.Common.LIGHTROOM_BW_FILM_PRINT_TIME.get();
        this.advanceFrame = advanceFrame;
        if (level != null) {
            level.setBlock(getBlockPos(), level.getBlockState(getBlockPos()).setValue(LightroomBlock.LIT, true), Block.UPDATE_CLIENTS);
            level.playSound(null, getBlockPos(), Exposure.SoundEvents.LIGHTROOM_PRINT.get(), SoundSource.BLOCKS,
                    1f, level.getRandom().nextFloat() * 0.3f + 1f);
        }
    }

    public void stopPrintingProcess() {
        printedFrame = -1;
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
        if (!(filmStack.getItem() instanceof DevelopedFilmItem developedFilm) || !developedFilm.hasExposedFrame(filmStack, selectedFrame))
            return false;

        if (!hasDyesForPrint(developedFilm.getType()))
            return false;

        ItemStack resultStack = getItem(RESULT_SLOT);
        return resultStack.isEmpty() || resultStack.getItem() instanceof PhotographItem
                || (resultStack.getItem() instanceof StackedPhotographsItem stackedPhotographsItem
                    && stackedPhotographsItem.canAddPhotograph(resultStack));
    }

    public boolean hasDyesForPrint(FilmType type) {
        if (type == FilmType.COLOR)
            return !getItem(CYAN_SLOT).isEmpty() && !getItem(MAGENTA_SLOT).isEmpty() && !getItem(YELLOW_SLOT).isEmpty() && !getItem(BLACK_SLOT).isEmpty();

        if (type == FilmType.BLACK_AND_WHITE)
            return !getItem(BLACK_SLOT).isEmpty();

        Exposure.LOGGER.info("Don't know what dyes needed for the film type: <" + type + ">");
        return false;
    }

    public boolean tryPrint() {
        if (!canPrint() || printedFrame < 0)
            return false;

        ItemAndStack<DevelopedFilmItem> film = new ItemAndStack<>(getItem(FILM_SLOT));
        ListTag frames = film.getItem().getExposedFrames(film.getStack());
        if (printedFrame >= frames.size())
            return false;

        CompoundTag frameTag = frames.getCompound(printedFrame);
        frameTag.putString("Type", film.getItem().getType().getSerializedName());

        ItemStack photographStack = new ItemStack(Exposure.Items.PHOTOGRAPH.get());
        photographStack.setTag(frameTag);

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

        if (film.getItem().getType() == FilmType.COLOR) {
            getItem(CYAN_SLOT).shrink(1);
            getItem(MAGENTA_SLOT).shrink(1);
            getItem(YELLOW_SLOT).shrink(1);
        }
        getItem(BLACK_SLOT).shrink(1);

        getItem(PAPER_SLOT).shrink(1);

        if (level != null)
            level.playSound(null, getBlockPos(), Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(), SoundSource.PLAYERS, 0.8f, 1f);

        printedPhotographsCount++;

        // Mark exposure as printed
        String id = frameTag.getString("Id");
        if (id.length() > 0) {
            Exposure.getStorage().getOrQuery(id).ifPresent(exposure -> {
                if (!exposure.isPrinted()) {
                    exposure.setPrinted(true);
                    exposure.setDirty();
                }
            });
        }

        return true;
    }

    public void dropStoredExperience(@Nullable Player player) {
        if (level == null  || level.isClientSide)
            return;

        int xpPerPrint = Config.Common.LIGHTROOM_EXPERIENCE_PER_PRINT.get();
        if (xpPerPrint > 0) {
            for (int i = 0; i < printedPhotographsCount; i++) {
                ExperienceOrb.award(((ServerLevel) level), player != null ? player.position() : Vec3.atCenterOf(getBlockPos()), xpPerPrint - 1 + level.getRandom().nextInt(0, 3));
            }
        }

        printedPhotographsCount = 0;
        setChanged();
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
        else if (slot == CYAN_SLOT) return stack.is(Exposure.Tags.Items.CYAN_PRINTING_DYES);
        else if (slot == MAGENTA_SLOT) return stack.is(Exposure.Tags.Items.MAGENTA_PRINTING_DYES);
        else if (slot == YELLOW_SLOT) return stack.is(Exposure.Tags.Items.YELLOW_PRINTING_DYES);
        else if (slot == BLACK_SLOT) return stack.is(Exposure.Tags.Items.BLACK_PRINTING_DYES);
        else if (slot == PAPER_SLOT) return stack.is(Exposure.Tags.Items.PHOTO_PAPERS);
        else if (slot == RESULT_SLOT) return stack.getItem() instanceof PhotographItem;
        return false;
    }

    protected void inventoryContentsChanged(int slot) {
        if (slot == FILM_SLOT) {
            selectedFrame = 0;
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
        return new int[0];
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
            return side == Direction.DOWN ? inventoryHandlers[0].cast() : this.inventoryHandler.cast();

        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        for (LazyOptional<IItemHandlerModifiable> inventoryHandler : inventoryHandlers) {
            inventoryHandler.invalidate();
        }
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        inventoryHandlers = net.minecraftforge.items.wrapper.SidedInvWrapper.create(this, Direction.DOWN, Direction.UP, Direction.NORTH);
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
        this.selectedFrame = tag.getInt("SelectedFrame");
        if (tag.contains("PrintedFrame", Tag.TAG_INT))
            this.printedFrame = tag.getInt("PrintedFrame");
        this.progress = tag.getInt("Progress");
        this.printTime = tag.getInt("PrintTime");
        this.printedPhotographsCount = tag.getInt("PrintedPhotographsCount");
        this.ejectFilmAfterLastFrame = tag.getBoolean("EjectFilmAfterLastFrame");
        this.advanceFrame = tag.getBoolean("AdvanceFrame");
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", this.inventory.serializeNBT());
        if (selectedFrame > 0)
            tag.putInt("SelectedFrame", selectedFrame);
        if (printedFrame > -1)
            tag.putInt("PrintedFrame", printedFrame);
        if (progress > 0)
            tag.putInt("Progress", progress);
        if (printTime > 0)
            tag.putInt("PrintTime", printTime);
        if (printedPhotographsCount > 0)
            tag.putInt("PrintedPhotographsCount", printedPhotographsCount);
        if (ejectFilmAfterLastFrame)
            tag.putBoolean("EjectFilmAfterLastFrame", true);
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
