package io.github.mortuusars.exposure.world.item;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class CameraStandItem extends Item {
    public CameraStandItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Direction direction = context.getClickedFace();
        if (direction == Direction.DOWN) return InteractionResult.FAIL;

        Level level = context.getLevel();
        BlockPlaceContext blockPlaceContext = new BlockPlaceContext(context);
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        ItemStack itemStack = context.getItemInHand();
        Vec3 pos = Vec3.atBottomCenterOf(blockPos);
        AABB aabb = Exposure.EntityTypes.CAMERA_STAND.get().getDimensions().makeBoundingBox(pos.x(), pos.y(), pos.z());
        if (!level.noCollision(null, aabb) || !level.getEntities(null, aabb).isEmpty()) return InteractionResult.FAIL;

        if (level instanceof ServerLevel serverLevel) {
            Consumer<CameraStandEntity> consumer = EntityType.createDefaultStackConfig(serverLevel, itemStack, context.getPlayer());
            CameraStandEntity cameraStand = Exposure.EntityTypes.CAMERA_STAND.get()
                    .create(serverLevel, consumer, blockPos, EntitySpawnReason.SPAWN_ITEM_USE, true, true);
            if (cameraStand == null) {
                return InteractionResult.FAIL;
            }

            if (context.getPlayer() != null) {
                cameraStand.setOwnerPlayer(context.getPlayer());
            }
            cameraStand.snapTo(cameraStand.getX(), cameraStand.getY(), cameraStand.getZ(), 0.0F, 0.0F);
            serverLevel.addFreshEntityWithPassengers(cameraStand);
            cameraStand.playPlaceSound();
            cameraStand.gameEvent(GameEvent.ENTITY_PLACE, context.getPlayer());
        }

        itemStack.shrink(1);
        if (level.isClientSide()) {
            return InteractionResult.CONSUME;
        } else {
            return InteractionResult.SUCCESS;
        }
    }

    public InteractionResult interactWithBoat(Player player, InteractionHand hand, AbstractBoat boat) {
        if (!player.isSecondaryUseActive()) return InteractionResult.PASS;
        ItemStack itemStack = player.getItemInHand(hand);
        if (!(itemStack.getItem() instanceof CameraStandItem)) return InteractionResult.PASS;
        if (boat.getPassengers().size() >= 2) return InteractionResult.PASS;

        if (player.level() instanceof ServerLevel serverLevel) {
            Consumer<CameraStandEntity> consumer = EntityType.createDefaultStackConfig(serverLevel, itemStack, player);
            CameraStandEntity cameraStand = Exposure.EntityTypes.CAMERA_STAND.get()
                    .create(serverLevel, consumer, boat.blockPosition(), EntitySpawnReason.SPAWN_ITEM_USE, true, true);
            if (cameraStand == null) {
                return InteractionResult.FAIL;
            }

            cameraStand.setOwnerPlayer(player);

            cameraStand.startRiding(boat);
            boat.positionRider(cameraStand);

            serverLevel.addFreshEntityWithPassengers(cameraStand);
            cameraStand.playPlaceSound();
            cameraStand.gameEvent(GameEvent.ENTITY_PLACE, player);

            itemStack.shrink(1);
            player.setItemInHand(hand, itemStack);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.CONSUME;
    }
}
