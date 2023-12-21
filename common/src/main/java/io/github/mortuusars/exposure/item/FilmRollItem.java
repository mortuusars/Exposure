package io.github.mortuusars.exposure.item;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.camera.infrastructure.FilmType;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FilmRollItem extends Item implements IFilmItem {
    public static final String FRAME_SIZE_TAG = "FrameSize";

    private final FilmType filmType;
    private final int defaultFrameSize;
    private final int barColor;

    public FilmRollItem(FilmType filmType, int defaultFrameSize, int barColor, Properties properties) {
        super(properties);
        this.filmType = filmType;
        this.defaultFrameSize = defaultFrameSize;
        this.barColor = barColor;
    }

    @Override
    public FilmType getType() {
        return filmType;
    }

    public int getFrameSize(ItemStack filmStack) {
        if (filmStack.getTag() != null && filmStack.getOrCreateTag().contains(FRAME_SIZE_TAG, Tag.TAG_INT))
            return Mth.clamp(filmStack.getOrCreateTag().getInt(FRAME_SIZE_TAG), 1, 2048);
        else
            return defaultFrameSize;
    }

    public boolean isBarVisible(@NotNull ItemStack stack) {
        return getExposedFramesCount(stack) > 0;
    }

    public int getBarWidth(@NotNull ItemStack stack) {
        return Math.min(1 + 12 * getExposedFramesCount(stack) / getMaxFrameCount(stack), 13);
    }

    public int getBarColor(@NotNull ItemStack stack) {
        return barColor;
    }

    public void addFrame(ItemStack filmStack, CompoundTag frame) {
        CompoundTag tag = filmStack.getOrCreateTag();

        if (!tag.contains("Frames", Tag.TAG_LIST)) {
            tag.put("Frames", new ListTag());
        }

        ListTag listTag = tag.getList("Frames", Tag.TAG_COMPOUND);

        if (listTag.size() >= getMaxFrameCount(filmStack))
            throw new IllegalStateException("Cannot add more frames than film could fit. Size: " + listTag.size());

        listTag.add(frame);
        tag.put("Frames", listTag);
    }

    public boolean canAddFrame(ItemStack filmStack) {
        if (!filmStack.hasTag() || !filmStack.getOrCreateTag().contains("Frames", Tag.TAG_LIST))
            return true;

        return filmStack.getOrCreateTag().getList("Frames", Tag.TAG_COMPOUND).size() < getMaxFrameCount(filmStack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        int exposedFrames = getExposedFramesCount(stack);
        if (exposedFrames > 0) {
            int totalFrames = getMaxFrameCount(stack);
            tooltipComponents.add(Component.translatable("item.exposure.film_roll.tooltip.frame_count", exposedFrames, totalFrames)
                    .withStyle(ChatFormatting.GRAY));
        }

        int frameSize = getFrameSize(stack);
        if (frameSize != defaultFrameSize) {
            tooltipComponents.add(Component.translatable("item.exposure.film_roll.tooltip.frame_size",
                    Component.literal(String.format("%.1f", frameSize / 10f)))
                            .withStyle(ChatFormatting.GRAY));
        }

        // Create compat:
        int developingStep = stack.getTag() != null ? stack.getTag().getInt("CurrentDevelopingStep") : 0;
        if (Config.Common.CREATE_SPOUT_DEVELOPING_ENABLED.get() && developingStep > 0) {
            List<? extends String> totalSteps = Config.Common.spoutDevelopingSequence(getType()).get();

            MutableComponent stepsComponent = Component.literal("");

            for (int i = 0; i < developingStep; i++) {
                stepsComponent.append(Component.literal("I").withStyle(ChatFormatting.GOLD));
            }

            for (int i = developingStep; i < totalSteps.size(); i++) {
                stepsComponent.append(Component.literal("I").withStyle(ChatFormatting.DARK_GRAY));
            }

            tooltipComponents.add(Component.translatable("item.exposure.film_roll.tooltip.developing_step", stepsComponent)
                    .withStyle(ChatFormatting.GOLD));
        }
    }
}
