package io.github.mortuusars.exposure.block;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.block.entity.LightroomBlockEntity;
import io.github.mortuusars.exposure.item.DevelopedFilmItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LightroomBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;
    public LightroomBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(@NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        if (pStack.hasCustomHoverName()) {
            BlockEntity blockentity = pLevel.getBlockEntity(pPos);
            if (blockentity instanceof LightroomBlockEntity lightroomBlockEntity)
                lightroomBlockEntity.setCustomName(pStack.getHoverName());
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof LightroomBlockEntity lightroomBlockEntity) {
                if (level instanceof ServerLevel serverLevel) {
                    Containers.dropContents(serverLevel, pos, lightroomBlockEntity);
                    lightroomBlockEntity.dropStoredExperience(null);
                }

                level.updateNeighbourForOutputSignal(pos, this);
            }

            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getAnalogOutputSignal(@NotNull BlockState blockState, Level level, @NotNull BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof LightroomBlockEntity lightroomBlockEntity) {
            ItemStack filmStack = lightroomBlockEntity.getItem(LightroomBlockEntity.FILM_SLOT);
            if (filmStack.isEmpty() || !filmStack.hasTag() || !(filmStack.getItem() instanceof DevelopedFilmItem developedFilmItem))
                return 0;

            int exposedFrames = developedFilmItem.getExposedFramesCount(filmStack);
            int currentFrame = lightroomBlockEntity.getSelectedFrame();

            return Mth.floor((currentFrame + 1f) / exposedFrames * 14.0F) + 1;
        }
        else
            return 0;
    }


    @SuppressWarnings("deprecation")
    @Override
    public @NotNull BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull InteractionResult use(@NotNull BlockState blockState, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof LightroomBlockEntity lightroomBlockEntity))
            return InteractionResult.FAIL;

        player.awardStat(Exposure.Stats.INTERACT_WITH_LIGHTROOM);

        if (player instanceof ServerPlayer serverPlayer)
            NetworkHooks.openScreen(serverPlayer, lightroomBlockEntity, pos);

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Block block, @NotNull BlockPos fromPos, boolean pIsMoving) {
        if (!level.isClientSide) {
            if (!state.getValue(LIT)) {
                for (Direction direction : Direction.values()) {
                    BlockPos relative = pos.relative(direction);
                    if (relative.equals(fromPos) && level.getSignal(relative, direction) > 0
                            && level.getBlockEntity(pos) instanceof LightroomBlockEntity lightroomBlockEntity) {
                        lightroomBlockEntity.startPrintingProcess(true);
                        return;
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new LightroomBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
        if (!level.isClientSide && blockEntityType == Exposure.BlockEntityTypes.LIGHTROOM.get())
            return LightroomBlockEntity::serverTick;

        return null;
    }
}
