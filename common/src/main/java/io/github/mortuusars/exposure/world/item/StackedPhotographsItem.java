package io.github.mortuusars.exposure.world.item;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.gui.ClientGUI;
import io.github.mortuusars.exposure.world.inventory.tooltip.PhotographTooltip;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class StackedPhotographsItem extends Item {
    public static final Codec<ItemAndStack<PhotographItem>> PHOTOGRAPH_ITEM_AND_STACK_CODEC = ItemStack.CODEC.comapFlatMap(stack -> {
        if (stack.getItem() instanceof PhotographItem) {
            return DataResult.success(new ItemAndStack<>(stack));
        } else {
            return DataResult.error(() -> "'stacked_photographs' can only hold items of type PhotographItem.");
        }
    }, ItemAndStack::getItemStack);

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemAndStack<PhotographItem>> PHOTOGRAPH_ITEM_AND_STACK_STREAM_CODEC =
            ItemStack.STREAM_CODEC.map(
                    ItemAndStack::new, ItemAndStack::getItemStack
            );

    public StackedPhotographsItem(Properties properties) {
        super(properties);
    }

    /**
     * @return How many photographs can be stacked together.
     */
    public int getStackLimit() {
        return Config.Server.STACKED_PHOTOGRAPHS_MAX_SIZE.get();
    }

    public List<ItemAndStack<PhotographItem>> getPhotographs(ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.STACKED_PHOTOGRAPHS, Collections.emptyList());
    }

    public void setPhotographs(ItemStack stack, List<ItemAndStack<PhotographItem>> photographs) {
        stack.set(Exposure.DataComponents.STACKED_PHOTOGRAPHS, photographs);
    }

    public boolean canAddPhotograph(ItemStack stack) {
        return getPhotographs(stack).size() < getStackLimit();
    }

    public void addPhotograph(ItemStack stack, ItemStack photographStack, int index) {
        Preconditions.checkElementIndex(index, getPhotographs(stack).size() + 1);
        Preconditions.checkState(canAddPhotograph(stack),
                "Cannot add more photographs than this photo can store. Max count: " + getStackLimit());
        Preconditions.checkArgument(photographStack.getItem() instanceof PhotographItem, "Only PhotographItem can be stacked.");

        ArrayList<ItemAndStack<PhotographItem>> photographs = new ArrayList<>(getPhotographs(stack));
        photographs.add(index, new ItemAndStack<>(photographStack));

        setPhotographs(stack, photographs);
    }

    public void addPhotographOnTop(ItemStack stack, ItemStack photographStack) {
        addPhotograph(stack, photographStack, 0);
    }

    public void addPhotographToBottom(ItemStack stack, ItemStack photographStack) {
        addPhotograph(stack, photographStack, getPhotographs(stack).size());
    }

    public ItemAndStack<PhotographItem> removePhotograph(ItemStack stack, int index) {
        Preconditions.checkElementIndex(index, getPhotographs(stack).size());

        ArrayList<ItemAndStack<PhotographItem>> photographs = new ArrayList<>(getPhotographs(stack));
        ItemAndStack<PhotographItem> removedPhotograph = photographs.remove(index);

        setPhotographs(stack, photographs);

        return removedPhotograph;
    }

    public ItemAndStack<PhotographItem> removeTopPhotograph(ItemStack stack) {
        return removePhotograph(stack, 0);
    }

    public ItemAndStack<PhotographItem> removeBottomPhotograph(ItemStack stack) {
        return removePhotograph(stack, getPhotographs(stack).size() - 1);
    }

    // ---

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack stack) {
        List<ItemAndStack<PhotographItem>> photographs = getPhotographs(stack);
        if (photographs.isEmpty())
            return Optional.empty();

        return Optional.of(new PhotographTooltip(photographs));
    }

    @Override
    public boolean overrideStackedOnOther(@NotNull ItemStack stack, @NotNull Slot slot, @NotNull ClickAction action, @NotNull Player player) {
        if (action != ClickAction.SECONDARY || getPhotographs(stack).isEmpty() || !slot.mayPlace(new ItemStack(Exposure.Items.PHOTOGRAPH.get())))
            return false;

        ItemStack slotItem = slot.getItem();
        if (slotItem.isEmpty()) {
            ItemAndStack<PhotographItem> photograph = removeBottomPhotograph(stack);
            slot.set(photograph.getItemStack());

            if (getPhotographs(stack).size() == 1)
                player.containerMenu.setCarried(removeTopPhotograph(stack).getItemStack());

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

        if (!getPhotographs(stack).isEmpty() && other.isEmpty()) {
            ItemAndStack<PhotographItem> photograph = removeTopPhotograph(stack);
            access.set(photograph.getItemStack());

            if (getPhotographs(stack).size() == 1) {
                ItemAndStack<PhotographItem> lastPhotograph = removeTopPhotograph(stack);
                slot.set(lastPhotograph.getItemStack());
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
            } else
                return false;
        }

        if (other.getItem() instanceof StackedPhotographsItem otherStackedItem) {
            int otherCount = otherStackedItem.getPhotographs(other).size();
            int addedCount = 0;
            for (int i = 0; i < otherCount; i++) {
                if (canAddPhotograph(stack)) {
                    ItemAndStack<PhotographItem> photograph = otherStackedItem.removeBottomPhotograph(other);
                    addPhotographOnTop(stack, photograph.getItemStack());
                    addedCount++;
                }
            }

            if (otherStackedItem.getPhotographs(other).isEmpty())
                access.set(ItemStack.EMPTY);
            else if (otherStackedItem.getPhotographs(other).size() == 1)
                access.set(otherStackedItem.removeTopPhotograph(other).getItemStack());

            if (addedCount > 0)
                playAddSoundClientside(player);

            return true;
        }

        return false;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);

        if (player.isSecondaryUseActive() && cyclePhotographs(itemInHand)) {
            player.level().playSound(player, player, Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(), SoundSource.PLAYERS, 0.6f,
                    player.level().getRandom().nextFloat() * 0.2f + 1.2f);
            player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
            return InteractionResult.SUCCESS.heldItemTransformedTo(itemInHand);
        }

        List<ItemAndStack<PhotographItem>> photographs = getPhotographs(itemInHand);
        if (!photographs.isEmpty()) {
            if (level.isClientSide()) {
                int slot = hand == InteractionHand.OFF_HAND ? Inventory.SLOT_OFFHAND : player.getInventory().getSelectedSlot();
                ClientGUI.openPhotographsScreenFromItem(slot);
                player.playSound(Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(), 0.6f, 1.1f);
            }
            return InteractionResult.SUCCESS.heldItemTransformedTo(itemInHand);
        }

        return InteractionResult.FAIL;
    }

    public boolean cyclePhotographs(ItemStack stack) {
        if (getPhotographs(stack).size() < 2)
            return false;

        ItemAndStack<PhotographItem> topPhotograph = removeTopPhotograph(stack);
        addPhotographToBottom(stack, topPhotograph.getItemStack());

        return true;
    }

    public static void playAddSoundClientside(Player player) {
        if (player.level().isClientSide())
            player.playSound(Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(), 0.6f,
                    player.level().getRandom().nextFloat() * 0.2f + 1.2f);
    }

    public static void playRemoveSoundClientside(Player player) {
        if (player.level().isClientSide())
            player.playSound(Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(), 0.75f,
                    player.level().getRandom().nextFloat() * 0.2f + 0.75f);
    }
}
