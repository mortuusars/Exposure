package io.github.mortuusars.exposure.block.entity;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.block.FlashBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

public class FlashBlockEntity extends BlockEntity {
    private int ticks;
    public FlashBlockEntity(BlockPos pos, BlockState blockState) {
        super(Exposure.BlockEntityTypes.FLASH.get(), pos, blockState);
        ticks = 6;
    }

    public static <T extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockState blockState, T blockEntity) {
        if (blockEntity instanceof FlashBlockEntity flashBlockEntity)
            flashBlockEntity.tick();
    }

    protected void tick() {
        ticks--;
        if (ticks == 0) {
            BlockState blockState = Objects.requireNonNull(level).getBlockState(getBlockPos());
            level.setBlock(getBlockPos(), blockState.getValue(FlashBlock.WATERLOGGED) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }
}
