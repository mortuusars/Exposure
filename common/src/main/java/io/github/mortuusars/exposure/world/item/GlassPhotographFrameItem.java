package io.github.mortuusars.exposure.world.item;

import io.github.mortuusars.exposure.world.entity.GlassPhotographFrame;
import io.github.mortuusars.exposure.world.entity.PhotographFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class GlassPhotographFrameItem extends PhotographFrameItem {
    public GlassPhotographFrameItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull PhotographFrame createEntity(Level level, BlockPos pos, Direction direction) {
        return new GlassPhotographFrame(level, pos, direction);
    }
}