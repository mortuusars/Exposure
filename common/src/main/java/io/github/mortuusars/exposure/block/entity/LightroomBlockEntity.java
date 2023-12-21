package io.github.mortuusars.exposure.block.entity;

import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.block.LightroomBlock;
import io.github.mortuusars.exposure.camera.infrastructure.FilmType;
import io.github.mortuusars.exposure.data.storage.ExposureSavedData;
import io.github.mortuusars.exposure.item.DevelopedFilmItem;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.item.StackedPhotographsItem;
import io.github.mortuusars.exposure.menu.LightroomMenu;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class LightroomBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int CONTAINER_DATA_SIZE = 3;
    public static final int CONTAINER_DATA_PROGRESS_ID = 0;
    public static final int CONTAINER_DATA_PRINT_TIME_ID = 1;
    public static final int CONTAINER_DATA_SELECTED_FRAME_ID = 2;

    protected final ContainerData containerData = new ContainerData() {
        public int get(int id) {
            return switch (id) {
                case CONTAINER_DATA_PROGRESS_ID -> LightroomBlockEntity.this.progress;
                case CONTAINER_DATA_PRINT_TIME_ID -> LightroomBlockEntity.this.printTime;
                case CONTAINER_DATA_SELECTED_FRAME_ID -> LightroomBlockEntity.this.getSelectedFrame();
                default -> 0;
            };
        }

        public void set(int id, int value) {
            if (id == CONTAINER_DATA_PROGRESS_ID)
                LightroomBlockEntity.this.progress = value;
            else if (id == CONTAINER_DATA_PRINT_TIME_ID)
                LightroomBlockEntity.this.printTime = value;
            else if (id == CONTAINER_DATA_SELECTED_FRAME_ID)
                LightroomBlockEntity.this.setSelectedFrame(value);
            setChanged();
        }

        public int getCount() {
            return CONTAINER_DATA_SIZE;
        }
    };

    private NonNullList<ItemStack> items = NonNullList.withSize(Lightroom.SLOTS, ItemStack.EMPTY);

    protected int selectedFrame = 0;
    protected int progress = 0;
    protected int printTime = 0;
    protected int printedPhotographsCount = 0;
    protected boolean advanceFrame;

    public LightroomBlockEntity(BlockPos pos, BlockState blockState) {
        super(Exposure.BlockEntityTypes.LIGHTROOM.get(), pos, blockState);
    }

    public static <T extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockState blockState, T blockEntity) {
        if (blockEntity instanceof LightroomBlockEntity lightroomBlockEntity)
            lightroomBlockEntity.tick();
    }

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

    protected void onFramePrinted() {
        if (advanceFrame)
            advanceFrame();
    }

    protected void advanceFrame() {
        ItemAndStack<DevelopedFilmItem> film = new ItemAndStack<>(getItem(Lightroom.FILM_SLOT));
        int frames = film.getItem().getExposedFramesCount(film.getStack());

        if (getSelectedFrame() >= frames - 1) { // On last frame
            if (tryEjectFilm())
                setChanged();
        } else {
            setSelectedFrame(getSelectedFrame() + 1);
            setChanged();
        }
    }

    public boolean isAdvancingFrameOnPrint() {
        return advanceFrame;
    }

    protected boolean tryEjectFilm() {
        if (level == null || level.isClientSide || getItem(Lightroom.FILM_SLOT).isEmpty())
            return false;

        BlockPos pos = getBlockPos();
        Direction facing = level.getBlockState(pos).getValue(LightroomBlock.FACING);

        if (level.getBlockState(pos.relative(facing)).canOcclude())
            return false;

        Vec3i normal = facing.getNormal();
        Vec3 point = Vec3.atCenterOf(pos).add(normal.getX() * 0.75f, normal.getY() * 0.75f, normal.getZ() * 0.75f);
        ItemEntity itemEntity = new ItemEntity(level, point.x, point.y, point.z, removeItem(Lightroom.FILM_SLOT, 1));
        itemEntity.setDeltaMovement(normal.getX() * 0.05f, normal.getY() * 0.05f + 0.15f, normal.getZ() * 0.05f);
        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);
        return true;
    }

    public int getSelectedFrame() {
        return selectedFrame;
    }

    public void setSelectedFrame(int index) {
        if (selectedFrame != index) {
            selectedFrame = index;
            stopPrintingProcess();
        }
    }

    public void startPrintingProcess(boolean advanceFrame) {
        if (!canPrint())
            return;

        ItemStack filmStack = getItem(Lightroom.FILM_SLOT);
        if (!(filmStack.getItem() instanceof DevelopedFilmItem developedFilmItem))
            return;

        printTime = developedFilmItem.getType() == FilmType.COLOR ?
                Config.Common.LIGHTROOM_COLOR_FILM_PRINT_TIME.get() :
                Config.Common.LIGHTROOM_BW_FILM_PRINT_TIME.get();
        this.advanceFrame = advanceFrame;
        if (level != null) {
            level.setBlock(getBlockPos(), level.getBlockState(getBlockPos())
                    .setValue(LightroomBlock.LIT, true), Block.UPDATE_CLIENTS);
            level.playSound(null, getBlockPos(), Exposure.SoundEvents.LIGHTROOM_PRINT.get(), SoundSource.BLOCKS,
                    1f, level.getRandom().nextFloat() * 0.3f + 1f);
        }
    }

    public void stopPrintingProcess() {
        progress = 0;
        printTime = 0;
        advanceFrame = false;
        if (level != null && level.getBlockState(getBlockPos()).getBlock() instanceof LightroomBlock)
            level.setBlock(getBlockPos(), level.getBlockState(getBlockPos())
                    .setValue(LightroomBlock.LIT, false), Block.UPDATE_CLIENTS);
    }

    public boolean isPrinting() {
        return printTime > 0;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean canPrint() {
        if (getSelectedFrame() < 0) // Upper bound is checked further down
            return false;

        if (getItem(Lightroom.PAPER_SLOT).isEmpty())
            return false;

        ItemStack filmStack = getItem(Lightroom.FILM_SLOT);
        if (!(filmStack.getItem() instanceof DevelopedFilmItem developedFilm) || !developedFilm.hasExposedFrame(filmStack, getSelectedFrame()))
            return false;

        if (!hasDyesForPrint(developedFilm.getType()))
            return false;

        ItemStack resultStack = getItem(Lightroom.RESULT_SLOT);
        return resultStack.isEmpty() || resultStack.getItem() instanceof PhotographItem
                || (resultStack.getItem() instanceof StackedPhotographsItem stackedPhotographsItem
                && stackedPhotographsItem.canAddPhotograph(resultStack));
    }

    public boolean hasDyesForPrint(FilmType type) {
        if (type == FilmType.COLOR)
            return !getItem(Lightroom.CYAN_SLOT).isEmpty()
                    && !getItem(Lightroom.MAGENTA_SLOT).isEmpty()
                    && !getItem(Lightroom.YELLOW_SLOT).isEmpty()
                    && !getItem(Lightroom.BLACK_SLOT).isEmpty();

        if (type == FilmType.BLACK_AND_WHITE)
            return !getItem(Lightroom.BLACK_SLOT).isEmpty();

        LogUtils.getLogger().info("Don't know what dyes needed for the film type: <" + type + ">");
        return false;
    }

    public boolean tryPrint() {
        Preconditions.checkState(level != null && !level.isClientSide, "Cannot be called clientside.");
        if (!canPrint())
            return false;

        ItemAndStack<DevelopedFilmItem> film = new ItemAndStack<>(getItem(Lightroom.FILM_SLOT));
        ListTag frames = film.getItem().getExposedFrames(film.getStack());

        CompoundTag frameTag = frames.getCompound(getSelectedFrame());
        frameTag.putString("Type", film.getItem().getType().getSerializedName());

        ItemStack photographStack = new ItemStack(Exposure.Items.PHOTOGRAPH.get());
        photographStack.setTag(frameTag);

        ItemStack resultStack = getItem(Lightroom.RESULT_SLOT);
        if (resultStack.isEmpty())
            resultStack = photographStack;
        else if (resultStack.getItem() instanceof PhotographItem) {
            StackedPhotographsItem stackedPhotographsItem = Exposure.Items.STACKED_PHOTOGRAPHS.get();
            ItemStack newStackedPhotographs = new ItemStack(stackedPhotographsItem);
            stackedPhotographsItem.addPhotographOnTop(newStackedPhotographs, resultStack);
            stackedPhotographsItem.addPhotographOnTop(newStackedPhotographs, photographStack);
            resultStack = newStackedPhotographs;
        } else if (resultStack.getItem() instanceof StackedPhotographsItem stackedPhotographsItem) {
            stackedPhotographsItem.addPhotographOnTop(resultStack, photographStack);
        } else {
            LogUtils.getLogger().error("Unexpected item in result slot: " + resultStack);
            return false;
        }

        setItem(Lightroom.RESULT_SLOT, resultStack);

        if (film.getItem().getType() == FilmType.COLOR) {
            getItem(Lightroom.CYAN_SLOT).shrink(1);
            getItem(Lightroom.MAGENTA_SLOT).shrink(1);
            getItem(Lightroom.YELLOW_SLOT).shrink(1);
        }
        getItem(Lightroom.BLACK_SLOT).shrink(1);

        getItem(Lightroom.PAPER_SLOT).shrink(1);

        if (level != null)
            level.playSound(null, getBlockPos(), Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(), SoundSource.PLAYERS, 0.8f, 1f);

        printedPhotographsCount++;

        // Mark exposure as printed
        String id = frameTag.getString("Id");
        if (id.length() > 0) {
            ExposureServer.getExposureStorage().getOrQuery(id).ifPresent(exposure -> {
                CompoundTag properties = exposure.getProperties();
                if (!properties.getBoolean(ExposureSavedData.WAS_PRINTED_PROPERTY)) {
                    properties.putBoolean(ExposureSavedData.WAS_PRINTED_PROPERTY, true);
                    exposure.setDirty();
                }
            });
        }

        return true;
    }

    public void dropStoredExperience(@Nullable Player player) {
        if (level == null || level.isClientSide)
            return;

        int xpPerPrint = Config.Common.LIGHTROOM_EXPERIENCE_PER_PRINT.get();
        if (xpPerPrint > 0) {
            for (int i = 0; i < printedPhotographsCount; i++) {
                ExperienceOrb.award(((ServerLevel) level), player != null ? player.position() : Vec3.atCenterOf(getBlockPos()), xpPerPrint - 1 + level.getRandom()
                        .nextInt(0, 3));
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
    protected @NotNull AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new LightroomMenu(containerId, inventory, this, containerData);
    }

    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot == Lightroom.FILM_SLOT) return stack.getItem() instanceof DevelopedFilmItem;
        else if (slot == Lightroom.CYAN_SLOT) return stack.is(Exposure.Tags.Items.CYAN_PRINTING_DYES);
        else if (slot == Lightroom.MAGENTA_SLOT) return stack.is(Exposure.Tags.Items.MAGENTA_PRINTING_DYES);
        else if (slot == Lightroom.YELLOW_SLOT) return stack.is(Exposure.Tags.Items.YELLOW_PRINTING_DYES);
        else if (slot == Lightroom.BLACK_SLOT) return stack.is(Exposure.Tags.Items.BLACK_PRINTING_DYES);
        else if (slot == Lightroom.PAPER_SLOT) return stack.is(Exposure.Tags.Items.PHOTO_PAPERS);
        else if (slot == Lightroom.RESULT_SLOT) return stack.getItem() instanceof PhotographItem;
        return false;
    }

    protected void inventoryContentsChanged(int slot) {
        if (slot == Lightroom.FILM_SLOT)
            setSelectedFrame(0);

        setChanged();
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
        return Lightroom.ALL_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack itemStack, @Nullable Direction direction) {
        if (direction == Direction.DOWN)
            return false;
        return canPlaceItem(index, itemStack);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return index != Lightroom.RESULT_SLOT && isItemValidForSlot(index, stack) && super.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, @NotNull ItemStack pStack, @NotNull Direction direction) {
        for (int outputSlot : Lightroom.OUTPUT_SLOTS) {
            if (index == outputSlot)
                return true;
        }
        return false;
    }


    // Load/Save
    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);

        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items);

        // Backwards compatibility:
        if (tag.contains("Inventory", Tag.TAG_COMPOUND)) {
            CompoundTag inventory = tag.getCompound("Inventory");
            ListTag itemsList = inventory.getList("Items", Tag.TAG_COMPOUND);

            for (int i = 0; i < itemsList.size(); i++) {
                CompoundTag itemTags = itemsList.getCompound(i);
                int slot = itemTags.getInt("Slot");

                if (slot >= 0 && slot < items.size())
                    items.set(slot, ItemStack.of(itemTags));
            }
        }

        this.setSelectedFrame(tag.getInt("SelectedFrame"));
        this.progress = tag.getInt("Progress");
        this.printTime = tag.getInt("PrintTime");
        this.printedPhotographsCount = tag.getInt("PrintedPhotographsCount");
        this.advanceFrame = tag.getBoolean("AdvanceFrame");
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        if (getSelectedFrame() > 0)
            tag.putInt("SelectedFrame", getSelectedFrame());
        if (progress > 0)
            tag.putInt("Progress", progress);
        if (printTime > 0)
            tag.putInt("PrintTime", printTime);
        if (printedPhotographsCount > 0)
            tag.putInt("PrintedPhotographsCount", printedPhotographsCount);
        if (advanceFrame)
            tag.putBoolean("AdvanceFrame", true);
    }

    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    public int getContainerSize() {
        return Lightroom.SLOTS;
    }

    @Override
    public boolean isEmpty() {
        return getItems().stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return getItems().get(slot);
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        ItemStack itemStack = ContainerHelper.removeItem(getItems(), slot, amount);
        if (!itemStack.isEmpty())
            this.setChanged();
        return itemStack;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(getItems(), slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.getItems().set(slot, stack);
        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
        this.setChanged();
    }

    @Override
    public void clearContent() {
        getItems().clear();
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
}
