package io.github.mortuusars.exposure.block;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.block.entity.LightroomBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class LightroomBlock extends Block implements EntityBlock {
    public LightroomBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof LightroomBlockEntity lightroomBlockEntity))
            return InteractionResult.FAIL;

        // TODO: Stat
//        player.awardStat(Wares.Stats.INTERACT_WITH_DELIVERY_TABLE);

        if (player instanceof ServerPlayer serverPlayer)
            NetworkHooks.openScreen(serverPlayer, lightroomBlockEntity, pos);

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LightroomBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (!level.isClientSide && blockEntityType == Exposure.BlockEntityTypes.LIGHTROOM.get())
            return LightroomBlockEntity::serverTick;

        return null;
    }
}
