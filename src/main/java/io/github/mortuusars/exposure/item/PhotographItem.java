package io.github.mortuusars.exposure.item;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Either;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.gui.ClientGUI;
import io.github.mortuusars.exposure.client.gui.component.PhotographTooltip;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PhotographItem extends Item {
    public PhotographItem(Properties properties) {
        super(properties);
    }

    public Optional<Either<String, ResourceLocation>> getIdOrResource(ItemStack stack) {
        if (stack.getTag() == null)
            return Optional.empty();

        String id = stack.getTag().getString("Id");
        if (id.length() > 0)
            return Optional.of(Either.left(id));

        String resource = stack.getTag().getString("Resource");
        if (resource.length() > 0)
            return Optional.of(Either.right(new ResourceLocation(resource)));

        return Optional.empty();
    }

    public List<Component> getNote(ItemStack stack) {
        if (stack.getTag() != null) {
            ListTag noteListTag = stack.getTag().getList("Note", Tag.TAG_STRING);
            if (noteListTag.size() > 0) {
                ArrayList<Component> noteComponents = new ArrayList<>();
                for (Tag noteLine : noteListTag) {
                    noteComponents.add(Component.Serializer.fromJson(noteLine.getAsString()));
                }
                return noteComponents;
            }
        }

        return Collections.emptyList();
    }

    public void setId(ItemStack stack, @NotNull String id) {
        Preconditions.checkState(!StringUtil.isNullOrEmpty(id), "'id' cannot be null or empty.");
        stack.getOrCreateTag().putString("Id", id);
    }

    public void setResource(ItemStack stack, @NotNull ResourceLocation resourceLocation) {
        stack.getOrCreateTag().putString("Resource", resourceLocation.toString());
    }

    public void setNote(ItemStack stack, List<Component> note) {
        if (note.size() == 0 && stack.getTag() != null) {
            stack.getTag().remove("Note");
            return;
        }

        ListTag noteListTag = new ListTag();
        for (Component component : note) {
            noteListTag.add(StringTag.valueOf(Component.Serializer.toJson(component)));
        }
        stack.getOrCreateTag().put("Note", noteListTag);
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack stack) {
        Optional<Either<String, ResourceLocation>> idOrResource = getIdOrResource(stack);
        if (idOrResource.isPresent())
            return Optional.of(new PhotographTooltip(new ItemAndStack<>(stack)));
        return super.getTooltipImage(stack);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);

        getIdOrResource(itemInHand).ifPresentOrElse(idOrResource -> {
            if (level.isClientSide)
                ClientGUI.showPhotographScreen(new ItemAndStack<>(itemInHand));
        },
        () -> {
            if (level.isClientSide) {
                player.displayClientMessage(Component.translatable("item.exposure.photograph.message.no_data")
                        .withStyle(ChatFormatting.RED), true);
                player.playSound(SoundEvents.BOOK_PAGE_TURN, 1f, 0.65f);
                Exposure.LOGGER.error("Cannot show an image: no Id or Resource was found. - " + itemInHand);
            }
        });

        player.getCooldowns().addCooldown(this, 10);
        return InteractionResultHolder.success(itemInHand);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY)
            return false;

        if (other.getItem() instanceof PhotographItem) {
            StackedPhotographsItem stackedPhotographsItem = Exposure.Items.STACKED_PHOTOGRAPHS.get();
            ItemStack stackedPhotographsStack = new ItemStack(stackedPhotographsItem);

            stackedPhotographsItem.addPhotographOnTop(stackedPhotographsStack, stack);
            stackedPhotographsItem.addPhotographOnTop(stackedPhotographsStack, other);
            slot.set(ItemStack.EMPTY);
            access.set(stackedPhotographsStack);

            StackedPhotographsItem.playAddSoundClientside(player);

            return true;
        }

        return false;
    }
}
