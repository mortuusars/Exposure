package io.github.mortuusars.exposure.item;

import io.github.mortuusars.exposure.camera.ExposureFrame;
import io.github.mortuusars.exposure.camera.film.FilmType;
import io.github.mortuusars.exposure.client.ClientGUI;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
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

public class FilmItem extends Item {
    private static final String FRAMES_TAG = "Frames";
    private static final String FRAME_SIZE_TAG = "FrameSize";

    private final FilmType filmType;
    private final int frameCount;
    private final int barColor;

    public FilmItem (FilmType filmType, int frameCount, int barColor, Properties properties) {
        super(properties);
        this.filmType = filmType;
        this.frameCount = frameCount;
        this.barColor = barColor;
    }

    public boolean isBarVisible(@NotNull ItemStack stack) {
        return getExposedFramesCount(stack) > 0;
    }

    public int getBarWidth(@NotNull ItemStack stack) {
        return Math.min(1 + 12 * getExposedFramesCount(stack) / getMaxFrameCount(), 13);
    }

    public int getBarColor(@NotNull ItemStack stack) {
        return barColor;
    }

    protected int getExposedFramesCount(ItemStack stack) {
        return stack.hasTag() && stack.getOrCreateTag().contains(FRAMES_TAG, Tag.TAG_LIST) ?
                stack.getOrCreateTag().getList(FRAMES_TAG, Tag.TAG_COMPOUND).size() : 0;
    }

    public FilmType getType() {
        return filmType;
    }

    public int getMaxFrameCount() {
        return frameCount;
    }

    public int getFrameSize(ItemStack filmStack) {
        if (filmStack.getOrCreateTag().contains(FRAME_SIZE_TAG, Tag.TAG_INT))
            return filmStack.getOrCreateTag().getInt(FRAME_SIZE_TAG);
        else
            return getDefaultFrameSize();
    }

    public int getDefaultFrameSize() {
        return 320;
    }

    public List<ExposureFrame> getExposedFrames(ItemStack filmStack) {
        if (!filmStack.hasTag() || !filmStack.getOrCreateTag().contains(FRAMES_TAG, Tag.TAG_LIST))
            return Collections.emptyList();

        List<ExposureFrame> frames = new ArrayList<>();

        for (Tag frameTag : filmStack.getOrCreateTag().getList(FRAMES_TAG, Tag.TAG_COMPOUND)) {
            frames.add(ExposureFrame.load((CompoundTag) frameTag));
        }

        return frames;
    }

    public void addFrame(ItemStack filmStack, ExposureFrame frame) {
        CompoundTag tag = filmStack.getOrCreateTag();

        if (!tag.contains(FRAMES_TAG, Tag.TAG_LIST)) {
            tag.put(FRAMES_TAG, new ListTag());
        }

        ListTag listTag = tag.getList(FRAMES_TAG, Tag.TAG_COMPOUND);

        if (listTag.size() >= getMaxFrameCount())
            throw new IllegalStateException("Cannot add more frames than film could fit. Size: " + listTag.size());

        listTag.add(frame.save(new CompoundTag()));
        tag.put(FRAMES_TAG, listTag);
    }

    public boolean canAddFrame(ItemStack filmStack) {
        if (!filmStack.hasTag() || !filmStack.getOrCreateTag().contains(FRAMES_TAG, Tag.TAG_LIST))
            return true;

        return filmStack.getOrCreateTag().getList(FRAMES_TAG, Tag.TAG_COMPOUND).size() < getMaxFrameCount();
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack film = player.getItemInHand(hand);

        if (level.isClientSide)
            ClientGUI.showExposureViewScreen(film);

        return InteractionResultHolder.success(film);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        int exposedFrames = getExposedFrames(stack).size();
        if (exposedFrames > 0) {
            int totalFrames = getMaxFrameCount();
            tooltipComponents.add(Component.translatable("item.exposure.film.tooltip.frame_count", exposedFrames, totalFrames)
                    .withStyle(ChatFormatting.GRAY));
        }

        int frameSize = getFrameSize(stack);
        if (frameSize != getDefaultFrameSize()) {
            tooltipComponents.add(Component.translatable("item.exposure.film.tooltip.frame_size",
                    Component.literal(String.format("%.1f", frameSize / 10f)))
                            .withStyle(ChatFormatting.GRAY));
        }

        if (stack.hasTag() && stack.getOrCreateTag().contains("Notes", Tag.TAG_LIST)) {
            ListTag notes = stack.getOrCreateTag().getList("Notes", Tag.TAG_STRING);
            if (notes.size() > 0) {
                tooltipComponents.add(Component.literal("Notes:"));
                for (int i = 0; i < notes.size(); i++) {
                    tooltipComponents.add(Component.literal("  " + notes.getString(i)).withStyle(ChatFormatting.GRAY));
                }
            }
        }
    }
}
