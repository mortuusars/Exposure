package io.github.mortuusars.exposure.world.block.entity;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.world.block.LightroomBlock;
import io.github.mortuusars.exposure.world.level.LevelUtil;
import io.github.mortuusars.exposure.world.lightroom.PrintingMode;
import io.github.mortuusars.exposure.world.lightroom.PrintingProcess;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.inventory.LightroomMenu;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import io.github.mortuusars.exposure.world.item.ChromaticSheetItem;
import io.github.mortuusars.exposure.world.item.DevelopedFilmItem;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.StackedPhotographsItem;
import net.minecraft.util.Util;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

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
                case CONTAINER_DATA_SELECTED_FRAME_ID -> LightroomBlockEntity.this.getSelectedFrameIndex();
                default -> 0;
            };
        }

        public void set(int id, int value) {
            if (id == CONTAINER_DATA_PROGRESS_ID)
                LightroomBlockEntity.this.progress = value;
            else if (id == CONTAINER_DATA_PRINT_TIME_ID)
                LightroomBlockEntity.this.printTime = value;
            else if (id == CONTAINER_DATA_SELECTED_FRAME_ID)
                LightroomBlockEntity.this.setSelectedFrameIndex(value);
            setChanged();
        }

        public int getCount() {
            return CONTAINER_DATA_SIZE;
        }
    };

    public static final Map<DyeColor, Integer> DYE_SLOTS = Map.of(
            DyeColor.CYAN, Lightroom.CYAN_SLOT,
            DyeColor.MAGENTA, Lightroom.MAGENTA_SLOT,
            DyeColor.YELLOW, Lightroom.YELLOW_SLOT,
            DyeColor.BLACK, Lightroom.BLACK_SLOT
    );

    protected NonNullList<ItemStack> items = NonNullList.withSize(Lightroom.SLOTS, ItemStack.EMPTY);
    protected UUID lastPlayerId = Util.NIL_UUID;
    protected int selectedFrameIndex;
    protected int progress;
    protected int printTime;
    protected int storedExperience;
    protected boolean advanceFrame;
    protected PrintingMode printingMode = PrintingMode.REGULAR;

    protected @Nullable Frame selectedFrame;

    public LightroomBlockEntity(BlockPos pos, BlockState blockState) {
        super(Exposure.BlockEntityTypes.LIGHTROOM.get(), pos, blockState);
    }

    public static <T extends BlockEntity> void serverTick(Level ignoredLevel, BlockPos ignoredBlockPos,
                                                          BlockState ignoredBlockState, T blockEntity) {
        if (blockEntity instanceof LightroomBlockEntity lightroomBlockEntity) {
            lightroomBlockEntity.tick();
        }
    }

    protected void tick() {
        if (printTime <= 0 || !canPrint()) {
            stopPrintingProcess();
            return;
        }

        if (progress < printTime) {
            progress++;
            if (progress % 55 == 0 && printTime - progress > 12) {
                playPrintingSound();
            }
            return;
        }

        @Nullable Frame frame = getSelectedFrame();
        Preconditions.checkState(frame != null,
                "Frame cannot be null here because of 'canPrint' check. If it is - something went wrong.");

        PrintingProcess process = getPrintingProcess(frame);

        printFrame(frame, process, true);
        stopPrintingProcess();
    }

    public void setLastPlayer(Player player) {
        lastPlayerId = player.getUUID();
    }

    public float getProgressPercentage() {
        if (progress < 1 || printTime < 1)
            return 0f;

        return progress / (float) printTime;
    }

    public boolean isPrinting() {
        return level != null && level.getBlockState(getBlockPos()).getValue(LightroomBlock.PRINTING);
    }

    public boolean isAdvancingFrameOnPrint() {
        return advanceFrame;
    }

    protected boolean canEjectFilm() {
        if (level == null || level.isClientSide() || getItem(Lightroom.FILM_SLOT).isEmpty())
            return false;

        BlockPos pos = getBlockPos();
        Direction facing = level.getBlockState(pos).getValue(LightroomBlock.FACING);

        return !level.getBlockState(pos.relative(facing)).canOcclude();
    }

    protected void ejectFilm() {
        if (level == null || level.isClientSide() || getItem(Lightroom.FILM_SLOT).isEmpty())
            return;

        BlockPos pos = getBlockPos();
        Direction facing = level.getBlockState(pos).getValue(LightroomBlock.FACING);
        ItemStack filmStack = removeItem(Lightroom.FILM_SLOT, 1);

        Vec3i normal = facing.getUnitVec3i();
        Vec3 point = Vec3.atCenterOf(pos).add(normal.getX() * 0.75f, normal.getY() * 0.75f, normal.getZ() * 0.75f);
        ItemEntity itemEntity = new ItemEntity(level, point.x, point.y, point.z, filmStack);
        itemEntity.setDeltaMovement(normal.getX() * 0.05f, normal.getY() * 0.05f + 0.15f, normal.getZ() * 0.05f);
        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);

        inventoryContentsChanged(Lightroom.FILM_SLOT);
    }

    public int getSelectedFrameIndex() {
        return selectedFrameIndex;
    }

    public void setSelectedFrameIndex(int index) {
        if (selectedFrameIndex != index) {
            selectedFrameIndex = index;
            stopPrintingProcess();
        }

        ItemStack filmStack = getItem(Lightroom.FILM_SLOT);
        if (filmStack.getItem() instanceof DevelopedFilmItem developedFilm && developedFilm.hasFrameAt(filmStack, getSelectedFrameIndex())) {
            selectedFrame = developedFilm.getStoredFrames(filmStack).get(getSelectedFrameIndex());
        } else {
            selectedFrame = null;
        }
    }

    public PrintingMode getActualPrintingMode() {
        return isRefracted() ? printingMode : PrintingMode.REGULAR;
    }

    public PrintingMode getPrintingMode() {
        return printingMode;
    }

    public void setPrintMode(PrintingMode printingMode) {
        this.printingMode = printingMode;
        stopPrintingProcess();
        setChanged();
    }

    @Nullable
    public Frame getSelectedFrame() {
        return selectedFrame;
    }

    public boolean isRefracted() {
        return level != null && level.getBlockState(getBlockPos()).getValue(LightroomBlock.REFRACTED);
    }

    public void startPrintingProcess(boolean advanceFrameOnFinish) {
        if (!canPrint())
            return;

        @Nullable Frame frame = getSelectedFrame();
        Preconditions.checkState(frame != null,
                "Frame cannot be null here because of 'canPrint' check. If it is - something went wrong.");

        PrintingProcess process = getPrintingProcess(frame);

        int time = process.getPrintTime();
        printTime = isRefracted() && process.isRegular() ? time * 3 : time;

        advanceFrame = advanceFrameOnFinish;

        if (level != null) {
            BlockState state = level.getBlockState(getBlockPos());
            if (state.getBlock() instanceof LightroomBlock && !state.getValue(LightroomBlock.PRINTING)) {
                level.setBlock(getBlockPos(), state.setValue(LightroomBlock.PRINTING, true), Block.UPDATE_CLIENTS);
                playPrintingStartedSound();
            }
        }
    }

    public void stopPrintingProcess() {
        progress = 0;
        printTime = 0;
        advanceFrame = false;
        if (level != null) {
            BlockState state = level.getBlockState(getBlockPos());
            if (state.getBlock() instanceof LightroomBlock && state.getValue(LightroomBlock.PRINTING)) {
                level.setBlock(getBlockPos(), state
                        .setValue(LightroomBlock.PRINTING, false), Block.UPDATE_CLIENTS);
            }
        }
    }

    public boolean canPrint() {
        @Nullable Frame frame = getSelectedFrame();
        if (frame == null) {
            return false;
        }

        ItemStack paperStack = getItem(Lightroom.PAPER_SLOT);
        if (paperStack.isEmpty()) {
            return false;
        }

        PrintingProcess process = getPrintingProcess(frame);

        return isPaperValidForPrint(frame, process)
                && canOutputToResultSlot(frame, process)
                && hasDyesForPrint(frame, process)
                && hasSufficientLightLevel();
    }

    public boolean hasSufficientLightLevel() {
        if (level == null) return false;
        int requiredLightLevel = Config.Server.LIGHTROOM_LIGHT_REQUIREMENT.get();
        BlockPos above = getBlockPos().above();
        if (isRefracted()) {
            return LevelUtil.getLightLevelAt(level, above.above()) >= requiredLightLevel
                    || LevelUtil.getLightLevelAt(level, above.relative(Direction.NORTH)) >= requiredLightLevel
                    || LevelUtil.getLightLevelAt(level, above.relative(Direction.EAST)) >= requiredLightLevel
                    || LevelUtil.getLightLevelAt(level, above.relative(Direction.SOUTH)) >= requiredLightLevel
                    || LevelUtil.getLightLevelAt(level, above.relative(Direction.WEST)) >= requiredLightLevel;
        }
        return LevelUtil.getLightLevelAt(level, above) >= requiredLightLevel;
    }

    public boolean canPrintInCreativeMode() {
        @Nullable Frame frame = getSelectedFrame();
        if (frame == null) {
            return false;
        }

        PrintingProcess process = getPrintingProcess(frame);

        return canOutputToResultSlot(frame, process);
    }

    protected PrintingProcess getPrintingProcess(@NotNull Frame frame) {
        return switch (getActualPrintingMode()) {
            case REGULAR -> PrintingProcess.fromExposureType(frame.type());
            case CHROMATIC -> PrintingProcess.fromChromaticStep(getChromaticStep(getItem(Lightroom.PAPER_SLOT)));
        };
    }

    protected boolean isPaperValidForPrint(Frame frame, PrintingProcess process) {
        ItemStack paperStack = getItem(Lightroom.PAPER_SLOT);

        if (paperStack.isEmpty()) {
            return false;
        }

        if (process.isChromatic()) {
            return paperStack.is(Exposure.Tags.Items.PHOTO_PAPERS) ||
                    (paperStack.getItem() instanceof ChromaticSheetItem chromaticSheet
                            && !chromaticSheet.canCombine(paperStack));
        }

        return paperStack.is(Exposure.Tags.Items.PHOTO_PAPERS);
    }

    protected boolean hasDyesForPrint(Frame frame, PrintingProcess process) {
        for (DyeColor requiredDye : process.getRequiredDyes()) {
            int slotIndex = DYE_SLOTS.get(requiredDye);
            if (getItem(slotIndex).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public boolean canOutputToResultSlot(Frame frame, PrintingProcess process) {
        ItemStack resultStack = getItem(Lightroom.RESULT_SLOT);

        if (process.isChromatic()) {
            return resultStack.isEmpty();
        }

        return resultStack.isEmpty() || resultStack.getItem() instanceof PhotographItem
                || (resultStack.getItem() instanceof StackedPhotographsItem stackedPhotographsItem
                && stackedPhotographsItem.canAddPhotograph(resultStack));
    }

    protected int getChromaticStep(ItemStack paper) {
        if (!(paper.getItem() instanceof ChromaticSheetItem chromaticSheet))
            return 0;

        return chromaticSheet.getLayers(paper).size();
    }

    protected void printFrame(Frame frame, PrintingProcess process, boolean consumeIngredients) {
        ItemStack printResult = createPrintResult(frame, process);
        putPrintResultInOutputSlot(printResult);

        if (consumeIngredients) {
            consumePrintIngredients(frame, process);
            awardExperienceForPrint(frame, process, printResult);
        }

        onFramePrinted(frame, process, printResult);
    }

    public void printFrameInCreative() {
        @Nullable Frame frame = getSelectedFrame();
        if (frame == null) {
            Exposure.LOGGER.error("Cannot creatively print a frame: No frame is selected.");
            return;
        }

        PrintingProcess process = getPrintingProcess(frame);
        printFrame(frame, process, false);

        // Band-aid fix to not leave sheet in input slot.
        if (getItem(Lightroom.PAPER_SLOT).getItem() instanceof ChromaticSheetItem) {
            getItem(Lightroom.PAPER_SLOT).shrink(1);
            setChanged();
        }
    }

    protected ItemStack createPrintResult(Frame frame, PrintingProcess process) {
        if (process.isChromatic()) {
            ItemStack paperStack = getItem(Lightroom.PAPER_SLOT);
            ItemStack chromaticStack = paperStack.getItem() instanceof ChromaticSheetItem
                    ? paperStack.copy()
                    : new ItemStack(Exposure.Items.CHROMATIC_SHEET.get());
            ChromaticSheetItem chromaticItem = ((ChromaticSheetItem) chromaticStack.getItem());

            chromaticItem.addLayer(chromaticStack, frame);

            @Nullable ServerPlayer player = getLastOrClosestPlayer();
            if (chromaticItem.canCombine(chromaticStack)) {
                if (player != null) {
                    return chromaticItem.combineIntoPhotograph(player, chromaticStack, true);
                } else {
                    // This will be used when creating exposure to set 'wasPrinted' in exposure tag.
                    CompoundTag tag = new CompoundTag();
                    tag.putBoolean("printed", true);
                    chromaticStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                }
            }

            return chromaticStack;
        } else {
            ItemStack photographStack = new ItemStack(Exposure.Items.PHOTOGRAPH.get());
            photographStack.set(Exposure.DataComponents.PHOTOGRAPH_FRAME, frame);
            photographStack.set(Exposure.DataComponents.PHOTOGRAPH_TYPE, frame.type());
            return photographStack;
        }
    }

    protected @Nullable ServerPlayer getLastOrClosestPlayer() {
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            return null;
        }

        if (serverLevel.getPlayerByUUID(lastPlayerId) instanceof ServerPlayer lastPlayer) {
            return lastPlayer;
        }

        BlockPos pos = getBlockPos();
        return serverLevel.players().stream()
                .min(Comparator.comparingDouble(pl -> pl.distanceToSqr(pos.getX(), pos.getY(), pos.getZ())))
                .orElse(null);
    }

    protected void putPrintResultInOutputSlot(ItemStack printResult) {
        ItemStack resultStack = getItem(Lightroom.RESULT_SLOT);
        if (resultStack.isEmpty()) {
            resultStack = printResult;
        } else if (resultStack.getItem() instanceof PhotographItem) {
            StackedPhotographsItem stackedPhotographsItem = Exposure.Items.STACKED_PHOTOGRAPHS.get();
            ItemStack newStackedPhotographs = new ItemStack(stackedPhotographsItem);
            stackedPhotographsItem.addPhotographOnTop(newStackedPhotographs, resultStack);
            stackedPhotographsItem.addPhotographOnTop(newStackedPhotographs, printResult);
            resultStack = newStackedPhotographs;
        } else if (resultStack.getItem() instanceof StackedPhotographsItem stackedPhotographsItem) {
            stackedPhotographsItem.addPhotographOnTop(resultStack, printResult);
        } else {
            Exposure.LOGGER.error("Unexpected item in result slot: {}", resultStack);
            return;
        }
        setItem(Lightroom.RESULT_SLOT, resultStack);
    }

    protected void consumePrintIngredients(Frame frame, PrintingProcess process) {
        getItem(Lightroom.PAPER_SLOT).shrink(1);
        process.getRequiredDyes().forEach(dye -> getItem(DYE_SLOTS.get(dye)).shrink(1));
        setChanged();
    }

    protected void awardExperienceForPrint(Frame frame, PrintingProcess process, ItemStack result) {
        int xp = process.getExperiencePerPrint();

        if (xp > 0) {
            float variability = ThreadLocalRandom.current().nextFloat() * 0.3f + 1f;
            int variableXp = (int) Math.max(1, Math.ceil(xp * variability));
            storedExperience += variableXp;
        }
    }

    protected void onFramePrinted(Frame frame, PrintingProcess process, ItemStack result) {
        if (process.isRegular()) { // Chromatics create new exposure. Marking is not needed.
            frame.identifier().ifId(id ->
                    ExposureServer.exposureRepository().update(id, exposure ->
                            exposure.withTag(ExposureData.Tag::setPrinted)));
        }

        if (getLastOrClosestPlayer() instanceof ServerPlayer player) {
            Exposure.CriteriaTriggers.FRAME_PRINTED.get().trigger(player, getBlockPos(), frame, result);
        }

        if (advanceFrame) {
            advanceFrame();
        }

        playPrintingFinishedSound();
    }

    protected void advanceFrame() {
        ItemAndStack<DevelopedFilmItem> film = new ItemAndStack<>(getItem(Lightroom.FILM_SLOT));
        int frames = film.getItem().getStoredFramesCount(film.getItemStack());

        if (getSelectedFrameIndex() >= frames - 1) { // On last frame
            if (canEjectFilm())
                ejectFilm();
        } else {
            setSelectedFrameIndex(getSelectedFrameIndex() + 1);
            setChanged();
        }
    }

    public void dropStoredExperience(@Nullable Player player) {
        if (level instanceof ServerLevel serverLevel && storedExperience > 0) {
            ExperienceOrb.award(serverLevel, Vec3.atCenterOf(getBlockPos()), storedExperience);
            storedExperience = 0;
            setChanged();
        }
    }

    protected void playPrintingStartedSound() {
        if (level != null) {
            level.playSound(null, getBlockPos(), Exposure.SoundEvents.LIGHTROOM_PRINT.get(), SoundSource.BLOCKS,
                    1f, level.getRandom().nextFloat() * 0.3f + 1f);
        }
    }

    protected void playPrintingSound() {
        if (level != null) {
            level.playSound(null, getBlockPos(), Exposure.SoundEvents.LIGHTROOM_PRINT.get(), SoundSource.BLOCKS,
                    1f, level.getRandom().nextFloat() * 0.3f + 1f);
        }
    }

    protected void playPrintingFinishedSound() {
        if (level != null) {
            level.playSound(null, getBlockPos(), Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(),
                    SoundSource.PLAYERS, 0.8f, 1f);
        }
    }


    // Container

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("container.exposure.lightroom");
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
        else if (slot == Lightroom.PAPER_SLOT)
            return stack.is(Exposure.Tags.Items.PHOTO_PAPERS) ||
                    (stack.getItem() instanceof ChromaticSheetItem chromatic && chromatic.getLayers(stack).size() < 3);
        else if (slot == Lightroom.RESULT_SLOT)
            return stack.getItem() instanceof PhotographItem
                    || stack.getItem() instanceof StackedPhotographsItem
                    || stack.getItem() instanceof ChromaticSheetItem;
        return false;
    }

    protected void inventoryContentsChanged(int slot) {
        if (slot == Lightroom.FILM_SLOT)
            setSelectedFrameIndex(0);

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
    protected void loadAdditional(ValueInput input) {
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);

         // Backwards compatibility:
         input.child("Inventory").ifPresent(inventory -> {
             ValueInput.ValueInputList itemsList = inventory.childrenListOrEmpty("Items");

             for (ValueInput itemTag : itemsList) {
                 int slot = itemTag.getIntOr("Slot", 0);

                 if (slot >= 0 && slot < items.size()) {
                     itemTag.read(ItemStack.MAP_CODEC).ifPresent(stack -> items.set(slot, stack));
                 }
             }
         });

         this.setSelectedFrameIndex(input.getIntOr("SelectedFrame", 0));
         this.progress = input.getIntOr("Progress", 0);
         this.printTime = input.getIntOr("PrintTime", 0);
         this.storedExperience = input.getIntOr("PrintedPhotographsCount", 0);
         this.advanceFrame = input.getBooleanOr("AdvanceFrame", false);
         this.printingMode = PrintingMode.fromStringOrDefault(input.getStringOr("PrintMode", ""), PrintingMode.REGULAR);
         this.lastPlayerId = input.getString("LastPlayerId").map(UUID::fromString).orElse(Util.NIL_UUID);
     }

     @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        if (getSelectedFrameIndex() > 0)
            output.putInt("SelectedFrame", getSelectedFrameIndex());
        if (progress > 0)
            output.putInt("Progress", progress);
        if (printTime > 0)
            output.putInt("PrintTime", printTime);
        if (storedExperience > 0)
            output.putInt("PrintedPhotographsCount", storedExperience);
        if (advanceFrame)
            output.putBoolean("AdvanceFrame", true);
        if (printingMode != PrintingMode.REGULAR)
            output.putString("PrintMode", printingMode.getSerializedName());
        if (!lastPlayerId.equals(Util.NIL_UUID))
            output.putString("LastPlayerId", lastPlayerId.toString());
    }

    protected @NotNull NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
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
            inventoryContentsChanged(slot);
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
        inventoryContentsChanged(slot);
    }

    @Override
    public void clearContent() {
        getItems().clear();
        inventoryContentsChanged(-1);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide()) {
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
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
