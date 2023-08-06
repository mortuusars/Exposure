package io.github.mortuusars.exposure.item;

import io.github.mortuusars.exposure.camera.ExposedFrame;
import io.github.mortuusars.exposure.camera.film.FilmType;
import io.github.mortuusars.exposure.client.gui.ClientGUI;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DevelopedFilmItem extends Item implements IFilmItem {
    public static final String FRAMES_TAG = "Frames";

    private final FilmType type;

    public DevelopedFilmItem(FilmType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    @Override
    public FilmType getType() {
        return type;
    }

    @Override
    public List<ExposedFrame> getExposedFrames(ItemStack filmStack) {
        if (!filmStack.hasTag() || !filmStack.getOrCreateTag().contains(FRAMES_TAG, Tag.TAG_LIST))
            return Collections.emptyList();

        List<ExposedFrame> frames = new ArrayList<>();

        for (Tag frameTag : filmStack.getOrCreateTag().getList(FRAMES_TAG, Tag.TAG_COMPOUND)) {
            frames.add(ExposedFrame.load((CompoundTag) frameTag));
        }

        return frames;
    }

    @Override
    public boolean hasExposedFrame(ItemStack filmStack, int index) {
        if (index < 0 || !filmStack.hasTag() || !filmStack.getOrCreateTag().contains(FRAMES_TAG, Tag.TAG_LIST))
            return false;

        ListTag list = filmStack.getOrCreateTag().getList(FRAMES_TAG, Tag.TAG_COMPOUND);
        return index < list.size();
    }

    protected int getExposedFramesCount(ItemStack stack) {
        return stack.hasTag() && stack.getOrCreateTag().contains(FRAMES_TAG, Tag.TAG_LIST) ?
                stack.getOrCreateTag().getList(FRAMES_TAG, Tag.TAG_COMPOUND).size() : 0;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack film = player.getItemInHand(hand);

        if (level.isClientSide && getExposedFramesCount(film) > 0)
            ClientGUI.showExposureViewScreen(film);

        return InteractionResultHolder.success(film);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        int exposedFrames = getExposedFramesCount(stack);
        if (exposedFrames > 0) {
            tooltipComponents.add(Component.translatable("item.exposure.developed_film.tooltip.frame_count", exposedFrames)
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
