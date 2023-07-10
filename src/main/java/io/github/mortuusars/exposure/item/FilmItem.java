package io.github.mortuusars.exposure.item;

import io.github.mortuusars.exposure.camera.ExposureFrame;
import io.github.mortuusars.exposure.camera.film.FilmType;
import io.github.mortuusars.exposure.client.GUI;
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

public class FilmItem extends Item {
    private static final String FRAMES_TAG = "Frames";

    private final FilmType filmType;
    private final int frameSize;
    private final int frameCount;

    public FilmItem (FilmType filmType, int frameSize, int frameCount, Properties properties) {
        super(properties);
        this.filmType = filmType;
        this.frameSize = frameSize;
        this.frameCount = frameCount;
    }

    public FilmType getType() {
        return filmType;
    }

    public int getFrameSize() {
        return frameSize;
    }

    public int getMaxFrameCount() {
        return frameCount;
    }

    public List<ExposureFrame> getExposedFrames(ItemStack filmStack) {
        if (!filmStack.hasTag() || !filmStack.getOrCreateTag().contains(FRAMES_TAG, Tag.TAG_LIST))
            return Collections.emptyList();

        List<ExposureFrame> frames = new ArrayList<>();

        for (Tag frameTag : filmStack.getOrCreateTag().getList(FRAMES_TAG, Tag.TAG_COMPOUND)) {
            frames.add(new ExposureFrame((CompoundTag) frameTag));
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
            throw new IllegalStateException("Cannot add more frames than film could have. Size: " + listTag.size());

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
            GUI.showExposureViewScreen(film);

        return InteractionResultHolder.success(film);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        int exposedFrames = getExposedFrames(stack).size();
        if (exposedFrames == 0)
            return;

        int totalFrames = getMaxFrameCount();
        tooltipComponents.add(Component.translatable("item.exposure.film.frames_tooltip", exposedFrames, totalFrames).withStyle(ChatFormatting.GRAY));
    }
}
