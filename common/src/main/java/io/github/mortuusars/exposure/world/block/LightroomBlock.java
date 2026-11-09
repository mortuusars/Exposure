package io.github.mortuusars.exposure.world.block;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.block.entity.Lightroom;
import io.github.mortuusars.exposure.world.block.entity.LightroomBlockEntity;
import io.github.mortuusars.exposure.world.item.StackedPhotographsItem;
import io.github.mortuusars.mortaar.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LightroomBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty PRINTING = BooleanProperty.create("printing");
    public static final BooleanProperty REFRACTED = BooleanProperty.create("refracted");

    public LightroomBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PRINTING, false)
                .setValue(REFRACTED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, PRINTING, REFRACTED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

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

    @Override
    public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(@NotNull BlockState blockState, Level level, @NotNull BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof LightroomBlockEntity lightroomBlockEntity) {
            ItemStack resultStack = lightroomBlockEntity.getItem(Lightroom.RESULT_SLOT);

            if (resultStack.getItem() instanceof StackedPhotographsItem stackedPhotographsItem) {
                int photographsCount = stackedPhotographsItem.getPhotographs(resultStack).size();
                return (int) ((photographsCount / (float) stackedPhotographsItem.getStackLimit() * 14) + 1);
            } else if (!resultStack.isEmpty()) {
                return 1;
            }
        }

        return 0;
    }

    @Override
    public @NotNull BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof LightroomBlockEntity lightroomBlockEntity))
            return super.useWithoutItem(state, level, pos, player, hitResult);

        player.awardStat(Exposure.Stats.INTERACT_WITH_LIGHTROOM);

        if (player instanceof ServerPlayer serverPlayer) {
            lightroomBlockEntity.setLastPlayer(serverPlayer);
            lightroomBlockEntity.setChanged(); // Updates state for client. Without this GUI buttons have some problems. (PrintButton active state)
            Platform.openMenu(serverPlayer, lightroomBlockEntity, buffer -> buffer.writeBlockPos(pos));
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Block block, @NotNull BlockPos fromPos, boolean pIsMoving) {
        if (!level.isClientSide) {
            if (!state.getValue(PRINTING)) {
                for (Direction direction : Direction.values()) {
                    BlockPos relative = pos.relative(direction);
                    if (relative.equals(fromPos) && level.getSignal(relative, direction) > 0
                            && level.getBlockEntity(pos) instanceof LightroomBlockEntity lightroomBlockEntity) {
                        lightroomBlockEntity.startPrintingProcess(true);
                        break;
                    }
                }
            }

            level.setBlock(pos, level.getBlockState(pos)
                    .setValue(LightroomBlock.REFRACTED, level.getBlockState(pos.above()).is(Exposure.Tags.Blocks.CHROMATIC_REFRACTORS)),
                    Block.UPDATE_CLIENTS);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return createBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return getBlockTicker(level, state, blockEntityType);
    }

    public static BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new LightroomBlockEntity(pos, state);
    }

    public static <T extends BlockEntity> BlockEntityTicker<T> getBlockTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (!level.isClientSide && blockEntityType.equals(Exposure.BlockEntityTypes.LIGHTROOM.get()))
            return LightroomBlockEntity::serverTick;

        return null;
    }
}
