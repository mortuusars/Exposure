package io.github.mortuusars.exposure.item;

import io.github.mortuusars.exposure.camera.infrastructure.FilmType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DevelopedFilmItem extends Item implements IFilmItem {
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
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        int exposedFrames = getExposedFramesCount(stack);
        if (exposedFrames > 0) {
            tooltipComponents.add(Component.translatable("item.exposure.developed_film.tooltip.frame_count", exposedFrames)
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
