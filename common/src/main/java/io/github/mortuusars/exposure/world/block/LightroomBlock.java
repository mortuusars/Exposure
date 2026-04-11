package io.github.mortuusars.exposure.world.block;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.PlatformHelper;
import io.github.mortuusars.exposure.world.block.entity.Lightroom;
import io.github.mortuusars.exposure.world.block.entity.LightroomBlockEntity;
import io.github.mortuusars.exposure.world.item.StackedPhotographsItem;
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
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LightroomBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
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
    public void destroy(@NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockState state) {
        if (level.getBlockEntity(pos) instanceof LightroomBlockEntity lightroomBlockEntity) {
            if (level instanceof ServerLevel serverLevel) {
                Containers.dropContents(serverLevel, pos, lightroomBlockEntity);
                lightroomBlockEntity.dropStoredExperience(null);
            }

            if (level instanceof Level realLevel) {
                realLevel.updateNeighbourForOutputSignal(pos, this);
            }
        }

        super.destroy(level, pos, state);
    }

    @Override
    public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(@NotNull BlockState blockState, Level level, @NotNull BlockPos pos, @NotNull Direction direction) {
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
            PlatformHelper.openMenu(serverPlayer, lightroomBlockEntity, buffer -> buffer.writeBlockPos(pos));
        }

        if (level.isClientSide()) {
            return InteractionResult.CONSUME;
        } else {
            return InteractionResult.SUCCESS;
        }
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block block, @Nullable Orientation orientation, boolean pIsMoving) {
        if (!level.isClientSide()) {
            if (!state.getValue(PRINTING)) {
                boolean neighborPowered = level.hasNeighborSignal(pos);
                // this changes mod behavior from 1.21.1, but i don't know how to make it work like it originally did.
                // it starts a print every time anything around it powers it, even if it's already powered (like 1.21.1 behavior).
                // but it also starts a print when something around it is turned off, but other sources are still powering it (not like 1.21.1 behavior).
                if (neighborPowered && level.getBlockEntity(pos) instanceof LightroomBlockEntity lightroomBlockEntity) {
                    lightroomBlockEntity.startPrintingProcess(true);
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
        if (!level.isClientSide() && blockEntityType.equals(Exposure.BlockEntityTypes.LIGHTROOM.get()))
            return LightroomBlockEntity::serverTick;

        return null;
    }
}
