package io.github.mortuusars.exposure.world.entity;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.item.PhotographFrameItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class GlassPhotographFrame extends PhotographFrame {
    public GlassPhotographFrame(EntityType<? extends PhotographFrame> entityType, Level level) {
        super(entityType, level);
    }

    public GlassPhotographFrame(Level level, BlockPos pos, Direction facingDirection) {
        super(Exposure.EntityTypes.CLEAR_PHOTOGRAPH_FRAME.get(), level, pos, facingDirection);
    }

    protected GlassPhotographFrame(EntityType<? extends PhotographFrame> entityType, Level level, BlockPos pos, Direction facingDirection) {
        super(entityType, level, pos, facingDirection);
    }

    @Override
    public PhotographFrameItem getBaseFrameItem() {
        return Exposure.Items.CLEAR_PHOTOGRAPH_FRAME.get();
    }

    @Override
    public boolean isFrameInvisible() {
        return super.isFrameInvisible() || !getItem().isEmpty();
    }
}