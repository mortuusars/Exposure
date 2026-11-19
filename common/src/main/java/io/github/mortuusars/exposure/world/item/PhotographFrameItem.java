package io.github.mortuusars.exposure.world.item;

import io.github.mortuusars.exposure.world.entity.PhotographFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

public class PhotographFrameItem extends Item {
    public PhotographFrameItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        BlockPos clickedPos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        BlockPos resultPos = clickedPos.relative(direction);
        Player player = context.getPlayer();
        ItemStack itemInHand = context.getItemInHand();
        if (player == null || player.level().isOutsideBuildHeight(resultPos) || !player.mayUseItemAt(resultPos, direction, itemInHand))
            return InteractionResult.FAIL;

        Level level = context.getLevel();
        PhotographFrame frameEntity = createEntity(level, resultPos, direction);

        CustomData customData = itemInHand.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
        if (!customData.isEmpty()) {
            EntityType.updateCustomEntityTag(level, player, frameEntity, customData);
        }

        for (int i = 2; i >= 0; i--) {
            frameEntity.setSize(i);
            if (frameEntity.survives()) {
                if (!level.isClientSide) {
                    frameEntity.playPlacementSound();
                    level.gameEvent(player, GameEvent.ENTITY_PLACE, frameEntity.position());
                    level.addFreshEntity(frameEntity);
                }

                frameEntity.setFrameItem((player.isCreative() ? itemInHand.copy() : itemInHand).split(1));

                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return InteractionResult.FAIL;
    }

    public @NotNull PhotographFrame createEntity(Level level, BlockPos pos, Direction direction) {
        return new PhotographFrame(level, pos, direction);
    }
}
