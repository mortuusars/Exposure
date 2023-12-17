package io.github.mortuusars.exposure.item;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.infrastructure.FrameData;
import io.github.mortuusars.exposure.client.gui.ClientGUI;
import io.github.mortuusars.exposure.client.gui.component.PhotographTooltip;
import io.github.mortuusars.exposure.entity.PhotographEntity;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PhotographItem extends Item {
    public PhotographItem(Properties properties) {
        super(properties);
    }

    public @Nullable Either<String, ResourceLocation> getIdOrTexture(ItemStack stack) {
        if (stack.getTag() == null)
            return null;

        String id = stack.getTag().getString(FrameData.ID);
        if (id.length() > 0)
            return Either.left(id);

        String resource = stack.getTag().getString(FrameData.TEXTURE);
        if (resource.length() > 0)
            return Either.right(new ResourceLocation(resource));

        return null;
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
        stack.getOrCreateTag().putString(FrameData.ID, id);
    }

    public void setTexture(ItemStack stack, @NotNull ResourceLocation resourceLocation) {
        stack.getOrCreateTag().putString(FrameData.TEXTURE, resourceLocation.toString());
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
        return getIdOrTexture(stack) != null ? Optional.of(new PhotographTooltip(getIdOrTexture(stack))) : Optional.empty();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        if (stack.getTag() != null) {
            int generation = stack.getTag().getInt("generation");
            if (generation > 0)
                tooltipComponents.add(Component.translatable("item.exposure.photograph.generation." + generation)
                        .withStyle(ChatFormatting.GRAY));

            String photographerName = stack.getTag().getString(FrameData.PHOTOGRAPHER);
            if (photographerName.length() > 0 && Config.Client.PHOTOGRAPH_SHOW_PHOTOGRAPHER_IN_TOOLTIP.get()) {
                tooltipComponents.add(Component.translatable("item.exposure.photograph.photographer_tooltip",
                                Component.literal(photographerName).withStyle(ChatFormatting.WHITE))
                        .withStyle(ChatFormatting.GRAY));
            }

            if (isAdvanced.isAdvanced()) {
                @Nullable Either<String, ResourceLocation> idOrTexture = getIdOrTexture(stack);
                if (idOrTexture != null) {
                    String text = idOrTexture.map(id -> "Exposure Id: " + id, texture -> "Texture: " + texture);
                    tooltipComponents.add(Component.literal(text).withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        BlockPos clickedPos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        BlockPos resultPos = clickedPos.relative(direction);
        Player player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();
        if (player == null || player.level().isOutsideBuildHeight(resultPos) || !player.mayUseItemAt(resultPos, direction, itemStack))
            return InteractionResult.FAIL;

        Level level = context.getLevel();
        PhotographEntity photographEntity = new PhotographEntity(level, resultPos, direction, itemStack.copy());

        if (photographEntity.survives()) {
            if (!level.isClientSide) {
                photographEntity.playPlacementSound();
                level.gameEvent(player, GameEvent.ENTITY_PLACE, photographEntity.position());
                level.addFreshEntity(photographEntity);
            }

            itemStack.shrink(1);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.FAIL;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);

        if (getIdOrTexture(itemInHand) == null)
            LogUtils.getLogger().warn("No Id or Texture is defined. - " + itemInHand);

        if (level.isClientSide) {
            ClientGUI.openPhotographScreen(List.of(new ItemAndStack<>(itemInHand)));
            player.playSound(Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(), 0.6f, 1.1f);
        }

        return InteractionResultHolder.success(itemInHand);
    }

    @Override
    public boolean overrideOtherStackedOnMe(@NotNull ItemStack stack, @NotNull ItemStack other, @NotNull Slot slot, @NotNull ClickAction action, @NotNull Player player, @NotNull SlotAccess access) {
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

    public ItemStack copy(ItemStack original) {
        ItemStack newPhotographStack = new ItemStack(Exposure.Items.PHOTOGRAPH.get());
        CompoundTag compoundtag = original.getTag() != null ? original.getTag().copy() : new CompoundTag();
        compoundtag.putInt("generation", Math.min(WrittenBookItem.getGeneration(original) + 1, 2));
        newPhotographStack.setTag(compoundtag);
        return newPhotographStack;
    }
}
