package io.github.mortuusars.exposure.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FlashBlock extends Block implements SimpleWaterloggedBlock {
    public FlashBlock(Properties properties) {
        super(properties);
    }

    public boolean canReplaceBlockAt(Level level, BlockPos pos) {
        BlockState stateAtPos = level.getBlockState(pos);
        return stateAtPos.isAir() || stateAtPos.is(Blocks.WATER);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.removeBlock(pos, false);
    }
}
