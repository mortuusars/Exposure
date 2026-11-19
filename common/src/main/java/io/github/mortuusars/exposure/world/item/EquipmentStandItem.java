package io.github.mortuusars.exposure.world.item;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.entity.stand.EquipmentStand;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
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

public class EquipmentStandItem extends Item {
    public EquipmentStandItem(Properties properties) {
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
        AABB aabb = Exposure.EntityTypes.EQUIPMENT_STAND.get().getDimensions().makeBoundingBox(pos.x(), pos.y(), pos.z());
        if (!level.noCollision(null, aabb) || !level.getEntities(null, aabb).isEmpty()) return InteractionResult.FAIL;

        if (level instanceof ServerLevel serverLevel) {
            Consumer<EquipmentStand> consumer = EntityType.createDefaultStackConfig(serverLevel, itemStack, context.getPlayer());
            EquipmentStand stand = Exposure.EntityTypes.EQUIPMENT_STAND.get()
                    .create(serverLevel, consumer, blockPos, MobSpawnType.SPAWN_EGG, true, true);
            if (stand == null) {
                return InteractionResult.FAIL;
            }

            stand.moveTo(stand.getX(), stand.getY(), stand.getZ(), 0.0F, 0.0F);
            serverLevel.addFreshEntityWithPassengers(stand);
            stand.playPlaceSound();
            stand.gameEvent(GameEvent.ENTITY_PLACE, context.getPlayer());
        }

        itemStack.shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public InteractionResult interactWithBoat(Player player, InteractionHand hand, Boat boat) {
        if (!player.isSecondaryUseActive()) return InteractionResult.PASS;
        ItemStack itemStack = player.getItemInHand(hand);
        if (!(itemStack.getItem() instanceof EquipmentStandItem)) return InteractionResult.PASS;
        if (boat.getPassengers().size() >= 2) return InteractionResult.PASS;

        if (player.level() instanceof ServerLevel serverLevel) {
            Consumer<EquipmentStand> consumer = EntityType.createDefaultStackConfig(serverLevel, itemStack, player);
            EquipmentStand stand = Exposure.EntityTypes.EQUIPMENT_STAND.get()
                    .create(serverLevel, consumer, boat.blockPosition(), MobSpawnType.SPAWN_EGG, true, true);
            if (stand == null) {
                return InteractionResult.FAIL;
            }

            stand.startRiding(boat);
            boat.positionRider(stand);

            serverLevel.addFreshEntityWithPassengers(stand);
            stand.playPlaceSound();
            stand.gameEvent(GameEvent.ENTITY_PLACE, player);

            itemStack.shrink(1);
            player.setItemInHand(hand, itemStack);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.CONSUME;
    }
}
