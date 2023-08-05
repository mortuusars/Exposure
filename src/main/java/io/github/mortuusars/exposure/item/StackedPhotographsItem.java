package io.github.mortuusars.exposure.item;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.gui.ClientGUI;
import io.github.mortuusars.exposure.client.gui.component.PhotographTooltip;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
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

    public int getMaxPhotographs() {
        return maxPhotographs;
    }

    public int getPhotographsCount(ItemStack stack) {
        return getOrCreatePhotographsListTag(stack).size();
    }

    public List<ItemAndStack<PhotographItem>> getPhotographs(ItemStack stack) {
        ListTag listTag = getOrCreatePhotographsListTag(stack);
        if (listTag.size() == 0)
            return Collections.emptyList();

        List<ItemAndStack<PhotographItem>> photographs = new ArrayList<>();
        for (int i = 0; i < listTag.size(); i++) {
            photographs.add(getPhotograph(listTag, i));
        }
        return photographs;
    }

    private ListTag getOrCreatePhotographsListTag(ItemStack stack) {
        return stack.getTag() != null ? stack.getOrCreateTag().getList(PHOTOGRAPHS_TAG, Tag.TAG_COMPOUND) : new ListTag();
    }

    public boolean canAddPhotograph(ItemStack stack) {
        return getPhotographsCount(stack) < getMaxPhotographs();
    }

    public void addPhotograph(ItemStack stack, ItemStack photographStack, int index) {
        Preconditions.checkState(index >= 0 && index <= getPhotographsCount(stack), index + " is out of bounds. Count: " + getPhotographsCount(stack));
        Preconditions.checkState(canAddPhotograph(stack), "Cannot add more photographs than this stack can store. Max count: " + getMaxPhotographs());
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

    private ItemAndStack<PhotographItem> getPhotograph(ListTag photographsList, int index) {
        CompoundTag stackTag = photographsList.getCompound(index);
        ItemStack stack = ItemStack.of(stackTag);
        return new ItemAndStack<>(stack);
    }

    // ---

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack stack) {
        List<ItemAndStack<PhotographItem>> photographs = getPhotographs(stack);
        if (photographs.size() == 0)
            return Optional.empty();

        ItemAndStack<PhotographItem> topPhotograph = photographs.get(0);
        return topPhotograph.getItem().getIdOrResource(topPhotograph.getStack())
                .map(idOrResource -> new PhotographTooltip(idOrResource, photographs.size()));
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
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

        if (slotItem.getItem() instanceof PhotographItem) {
            addPhotographToBottom(stack, slotItem);
            slot.set(ItemStack.EMPTY);

            playAddSoundClientside(player);

            return true;
        }

        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
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
            addPhotographOnTop(stack, other);
            access.set(ItemStack.EMPTY);

            playAddSoundClientside(player);

            return true;
        }

        if (other.getItem() instanceof StackedPhotographsItem otherStackedItem) {
            int otherCount = otherStackedItem.getPhotographsCount(other);
            for (int i = 0; i < otherCount; i++) {
                if (canAddPhotograph(stack)) {
                    ItemAndStack<PhotographItem> photograph = otherStackedItem.removeBottomPhotograph(other);
                    addPhotographOnTop(stack, photograph.getStack());
                }
            }

            if (otherStackedItem.getPhotographsCount(other) == 0)
                access.set(ItemStack.EMPTY);
            else if (otherStackedItem.getPhotographsCount(other) == 1)
                access.set(otherStackedItem.removeTopPhotograph(other).getStack());

            playAddSoundClientside(player);

            return true;
        }

        return false;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);

        List<ItemAndStack<PhotographItem>> photographs = getPhotographs(itemInHand);
        if (photographs.size() > 0) {
            if (level.isClientSide)
                ClientGUI.showPhotographScreen(photographs);

            player.getCooldowns().addCooldown(this, 10);
        }

        return InteractionResultHolder.success(itemInHand);
    }

    public static void playAddSoundClientside(Player player) {
        if (player.getLevel().isClientSide)
            player.playSound(Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(), 0.6f,
                    player.getLevel().getRandom().nextFloat() * 0.2f + 1.2f);
    }

    public static void playRemoveSoundClientside(Player player) {
        if (player.getLevel().isClientSide)
            player.playSound(Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(), 0.75f,
                    player.getLevel().getRandom().nextFloat() * 0.2f + 0.75f);
    }
}
