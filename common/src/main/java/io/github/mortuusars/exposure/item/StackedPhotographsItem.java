package io.github.mortuusars.exposure.item;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Either;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.infrastructure.FrameData;
import io.github.mortuusars.exposure.gui.ClientGUI;
import io.github.mortuusars.exposure.gui.component.PhotographTooltip;
import io.github.mortuusars.exposure.entity.PhotographEntity;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class StackedPhotographsItem extends Item {
    public static final String PHOTOGRAPHS_TAG = "Photographs";
    private final int maxPhotographs;

    public StackedPhotographsItem(int maxPhotographs, Properties properties) {
        super(properties);
        this.maxPhotographs = maxPhotographs;
    }

    /**
     * @return How many photographs can be stacked together.
     */
    public int getStackLimit() {
        return maxPhotographs;
    }

    public int getPhotographsCount(ItemStack stack) {
        return getOrCreatePhotographsListTag(stack).size();
    }

    public List<ItemAndStack<PhotographItem>> getPhotographs(ItemStack stack) {
        return getPhotographs(stack, getStackLimit());
    }

    public List<ItemAndStack<PhotographItem>> getPhotographs(ItemStack stack, int limit) {
        ListTag listTag = getOrCreatePhotographsListTag(stack);
        if (listTag.isEmpty())
            return Collections.emptyList();

        List<ItemAndStack<PhotographItem>> photographs = new ArrayList<>();
        for (int i = 0; i < Math.min(listTag.size(), limit); i++) {
            photographs.add(getPhotograph(listTag, i));
        }

        return photographs;
    }

    public boolean canAddPhotograph(ItemStack stack) {
        return getPhotographsCount(stack) < getStackLimit();
    }

    public void addPhotograph(ItemStack stack, ItemStack photographStack, int index) {
        Preconditions.checkState(index >= 0 && index <= getPhotographsCount(stack), index + " is out of bounds. Count: " + getPhotographsCount(stack));
        Preconditions.checkState(canAddPhotograph(stack), "Cannot add more photographs than this photo can store. Max count: " + getStackLimit());
        ListTag listTag = getOrCreatePhotographsListTag(stack);
        listTag.add(index, photographStack.save(new CompoundTag()));
        stack.getOrCreateTag().put(PHOTOGRAPHS_TAG, listTag);
    }

    public void addPhotographOnTop(ItemStack stack, ItemStack photographStack) {
        addPhotograph(stack, photographStack, 0);
    }

    public void addPhotographToBottom(ItemStack stack, ItemStack photographStack) {
        addPhotograph(stack, photographStack, getPhotographsCount(stack));
    }

    public ItemAndStack<PhotographItem> removePhotograph(ItemStack stack, int index) {
        Preconditions.checkState(index >= 0 && index < getPhotographsCount(stack), index + " is out of bounds. Count: " + getPhotographsCount(stack));

        ListTag listTag = getOrCreatePhotographsListTag(stack);
        ItemStack photographStack = ItemStack.of((CompoundTag)listTag.remove(index));
        stack.getOrCreateTag().put(PHOTOGRAPHS_TAG, listTag);

        return new ItemAndStack<>(photographStack);
    }

    public ItemAndStack<PhotographItem> removeTopPhotograph(ItemStack stack) {
        return removePhotograph(stack, 0);
    }

    public ItemAndStack<PhotographItem> removeBottomPhotograph(ItemStack stack) {
        return removePhotograph(stack, getPhotographsCount(stack) - 1);
    }

    private ListTag getOrCreatePhotographsListTag(ItemStack stack) {
        return stack.getTag() != null ? stack.getOrCreateTag().getList(PHOTOGRAPHS_TAG, Tag.TAG_COMPOUND) : new ListTag();
    }

    private ItemAndStack<PhotographItem> getPhotograph(ListTag photographsList, int index) {
        CompoundTag stackTag = photographsList.getCompound(index);
        ItemStack stack = ItemStack.of(stackTag);
        return new ItemAndStack<>(stack);
    }

    public @Nullable Either<String, ResourceLocation> getFirstIdOrTexture(ItemStack stack) {
        ListTag listTag = getOrCreatePhotographsListTag(stack);
        if (listTag.isEmpty())
            return null;

        CompoundTag first = listTag.getCompound(0).getCompound("tag");
        String id = first.getString(FrameData.ID);
        if (!id.isEmpty())
            return Either.left(id);

        String resource = first.getString(FrameData.TEXTURE);
        if (!resource.isEmpty())
            return Either.right(new ResourceLocation(resource));

        return null;
    }

    public List<@Nullable Either<String, ResourceLocation>> getTopPhotographs(ItemStack stack, int count) {
        Preconditions.checkArgument(count > 0, "count '{}' is not valid. > 0", count);

        List<@Nullable Either<String, ResourceLocation>> photographs = new ArrayList<>();
        ListTag listTag = getOrCreatePhotographsListTag(stack);

        for (int i = 0; i < Math.min(listTag.size(), count); i++) {
            CompoundTag photographTag = listTag.getCompound(i).getCompound("tag");

            String id = photographTag.getString(FrameData.ID);
            if (!id.isEmpty()) {
                photographs.add(Either.left(id));
                continue;
            }

            String resource = photographTag.getString(FrameData.TEXTURE);
            if (!resource.isEmpty()) {
                photographs.add(Either.right(new ResourceLocation(resource)));
                continue;
            }

            photographs.add(null);
        }

        while (photographs.size() < count) {
            photographs.add(null);
        }

        return photographs;
    }

    // ---

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack stack) {
        List<ItemAndStack<PhotographItem>> photographs = getPhotographs(stack);
        if (photographs.isEmpty())
            return Optional.empty();

        return Optional.of(new PhotographTooltip(new ItemAndStack<>(stack)));
    }

    @Override
    public boolean overrideStackedOnOther(@NotNull ItemStack stack, @NotNull Slot slot, @NotNull ClickAction action, @NotNull Player player) {
        if (action != ClickAction.SECONDARY || getPhotographsCount(stack) == 0 || !slot.mayPlace(new ItemStack(Exposure.Items.PHOTOGRAPH.get())))
            return false;

        ItemStack slotItem = slot.getItem();
        if (slotItem.isEmpty()) {
            ItemAndStack<PhotographItem> photograph = removeBottomPhotograph(stack);
            slot.set(photograph.getStack());

            if (getPhotographsCount(stack) == 1)
                player.containerMenu.setCarried(removeTopPhotograph(stack).getStack());

            playRemoveSoundClientside(player);

            return true;
        }

        if (slotItem.getItem() instanceof PhotographItem && canAddPhotograph(stack)) {
            addPhotographToBottom(stack, slotItem);
            slot.set(ItemStack.EMPTY);

            playAddSoundClientside(player);

            return true;
        }

        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(@NotNull ItemStack stack, @NotNull ItemStack other, @NotNull Slot slot, @NotNull ClickAction action, @NotNull Player player, @NotNull SlotAccess access) {
        if (action != ClickAction.SECONDARY || !slot.mayPlace(new ItemStack(Exposure.Items.PHOTOGRAPH.get())))
            return false;

        if (getPhotographsCount(stack) > 0 && other.isEmpty()) {
            ItemAndStack<PhotographItem> photograph = removeTopPhotograph(stack);
            access.set(photograph.getStack());

            if (getPhotographsCount(stack) == 1) {
                ItemAndStack<PhotographItem> lastPhotograph = removeTopPhotograph(stack);
                slot.set(lastPhotograph.getStack());
            }

            playRemoveSoundClientside(player);

            return true;
        }

        if (other.getItem() instanceof PhotographItem) {
            if (canAddPhotograph(stack)) {
                addPhotographOnTop(stack, other);
                access.set(ItemStack.EMPTY);

                playAddSoundClientside(player);

                return true;
            }
            else
                return false;
        }

        if (other.getItem() instanceof StackedPhotographsItem otherStackedItem) {
            int otherCount = otherStackedItem.getPhotographsCount(other);
            int addedCount = 0;
            for (int i = 0; i < otherCount; i++) {
                if (canAddPhotograph(stack)) {
                    ItemAndStack<PhotographItem> photograph = otherStackedItem.removeBottomPhotograph(other);
                    addPhotographOnTop(stack, photograph.getStack());
                    addedCount++;
                }
            }

            if (otherStackedItem.getPhotographsCount(other) == 0)
                access.set(ItemStack.EMPTY);
            else if (otherStackedItem.getPhotographsCount(other) == 1)
                access.set(otherStackedItem.removeTopPhotograph(other).getStack());

            if (addedCount > 0)
                playAddSoundClientside(player);

            return true;
        }

        return false;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        BlockPos clickedPos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        BlockPos resultPos = clickedPos.relative(direction);
        Player player = context.getPlayer();
        ItemStack itemInHand = context.getItemInHand();

        if (itemInHand.getItem() != this || getPhotographsCount(itemInHand) == 0)
            return InteractionResult.FAIL;

        if (player == null || player.level().isOutsideBuildHeight(resultPos) || !player.mayUseItemAt(resultPos, direction, itemInHand))
            return InteractionResult.FAIL;

        if (player.isSecondaryUseActive()) {
            cyclePhotographs(itemInHand, player);
            return InteractionResult.SUCCESS;
        }

        ItemAndStack<PhotographItem> topPhotograph = removeTopPhotograph(itemInHand);

        Level level = context.getLevel();
        PhotographEntity photographEntity = new PhotographEntity(level, resultPos, direction, topPhotograph.getStack().copy());

        if (photographEntity.survives()) {
            if (!level.isClientSide) {
                photographEntity.playPlacementSound();
                level.gameEvent(player, GameEvent.ENTITY_PLACE, photographEntity.position());
                level.addFreshEntity(photographEntity);
            }

            if (!player.isCreative()) {
                int photographsCount = getPhotographsCount(itemInHand);
                if (photographsCount == 0)
                    itemInHand.shrink(1);
                else if (photographsCount == 1)
                    player.setItemInHand(context.getHand(), removeTopPhotograph(itemInHand).getStack());
            }
            else {
                // Because in creative you don't get photograph back when breaking Photograph entity,
                // we don't remove placed photograph from the photo.
                addPhotographOnTop(itemInHand, topPhotograph.getStack());
            }
        }
        else {
            addPhotographOnTop(itemInHand, topPhotograph.getStack());
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
//    @Override
//    public InteractionResult onItemUseFirst(ItemStack photo, UseOnContext context) {
//        if (!context.isSecondaryUseActive())
//            return InteractionResult.PASS;
//
//        boolean result = cyclePhotographs(photo, context.getPlayer());
//        return result ? InteractionResult.SUCCESS : InteractionResult.FAIL;
//

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);

        if (player.isSecondaryUseActive()) {
            cyclePhotographs(itemInHand, player);
            return InteractionResultHolder.success(itemInHand);
        }

        List<ItemAndStack<PhotographItem>> photographs = getPhotographs(itemInHand);
        if (!photographs.isEmpty()) {
            if (level.isClientSide) {
                ClientGUI.openPhotographScreen(photographs);
                player.playSound(Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(), 0.6f, 1.1f);
            }
            return InteractionResultHolder.success(itemInHand);
        }

        return InteractionResultHolder.fail(itemInHand);
    }

    public boolean cyclePhotographs(ItemStack stack, @Nullable Player player) {
        if (getPhotographsCount(stack) < 2)
            return false;

        ItemAndStack<PhotographItem> topPhotograph = removeTopPhotograph(stack);
        addPhotographToBottom(stack, topPhotograph.getStack());
        if (player != null) {
            player.level().playSound(player, player, Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(), SoundSource.PLAYERS, 0.6f,
                player.level().getRandom().nextFloat() * 0.2f + 1.2f);
            player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        }

        return true;
    }

    public static void playAddSoundClientside(Player player) {
        if (player.level().isClientSide)
            player.playSound(Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(), 0.6f,
                    player.level().getRandom().nextFloat() * 0.2f + 1.2f);
    }

    public static void playRemoveSoundClientside(Player player) {
        if (player.level().isClientSide)
            player.playSound(Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(), 0.75f,
                    player.level().getRandom().nextFloat() * 0.2f + 0.75f);
    }
}
