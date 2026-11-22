package io.github.mortuusars.exposure.world.item;

import io.github.mortuusars.exposure.world.camera.ExposureType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.List;
import java.util.function.Consumer;

public class DevelopedFilmItem extends Item implements FilmItem {
    private final ExposureType type;

    public DevelopedFilmItem(ExposureType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    @Override
    public ExposureType getType() {
        return type;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        int exposedFrames = getStoredFramesCount(stack);
        if (exposedFrames > 0) {
            tooltipComponents.accept(Component.translatable("item.exposure.developed_film.tooltip.frame_count", exposedFrames)
                    .withStyle(ChatFormatting.GRAY));
        }

        int frameSize = getFrameSize(stack);
        if (frameSize != getDefaultFrameSize(stack)) {
            tooltipComponents.accept(Component.translatable("item.exposure.film_roll.tooltip.frame_size",
                            Component.literal(String.format("%.1f", frameSize / 10f)))
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
