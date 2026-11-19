package io.github.mortuusars.exposure.world.item.camera;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.block.FlashBlock;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.sound.Sound;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Flash {
    public SoundEvent getSound() {
        return Exposure.SoundEvents.FLASH.get();
    }

    public int getCooldown() {
        return 10; //Ticks
    }

    public boolean isAvailable(ItemStack camera) {
        return Attachment.FLASH.isPresent(camera);
    }

    public boolean shouldFire(ItemStack camera, int lightLevel) {
        return switch (CameraSettings.FLASH_MODE.getOrDefault(camera)) {
            case OFF -> false;
            case ON -> true;
            case AUTO -> lightLevel < 8;
        };
    }

//    public boolean fire(CameraHolder holder, ServerLevel level, ItemStack stack) {
//        Entity entity = holder.asHolderEntity();
//
//        List<BlockPos> flashPositions = findPositions(holder, 8);
//        if (flashPositions.isEmpty()) {
//            return false;
//        }
//
//        for (BlockPos pos : flashPositions) {
//            level.setBlock(pos, Exposure.Blocks.FLASH.get().defaultBlockState()
//                  .setValue(FlashBlock.WATERLOGGED, level.getFluidState(pos).isSourceOfType(Fluids.WATER)), Block.UPDATE_ALL_IMMEDIATE);
////            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK_MARKER, Blocks.LIGHT.defaultBlockState()),
////                  pos.getX() + 0.5f,
////                  pos.getY() + 0.5f,
////                  pos.getZ() + 0.5f,
////                  1, 0, 0, 0, 0
////            );
//        }
//
//        Sound.play(entity, getSound(), entity.getSoundSource());
//
//        entity.gameEvent(GameEvent.PRIME_FUSE);
//        holder.getServerPlayerAwardedForExposure().ifPresent(player -> player.awardStat(Exposure.Stats.FLASHES_TRIGGERED.get()));
//        sendParticlesToOtherPlayers(holder, level);
//        return true;
//    }

    public boolean fire(CameraHolder holder, ServerLevel level, ItemStack stack) {
        Entity entity = holder.asHolderEntity();

        @Nullable BlockPos flashPosition = findPosition(holder);
        if (flashPosition == null) {
            return false;
        }

        level.setBlock(flashPosition, Exposure.Blocks.FLASH.get().defaultBlockState()
                .setValue(FlashBlock.WATERLOGGED, level.getFluidState(flashPosition)
                        .isSourceOfType(Fluids.WATER)), Block.UPDATE_ALL_IMMEDIATE);

        Sound.play(entity, getSound(), entity.getSoundSource());

        entity.gameEvent(GameEvent.PRIME_FUSE);
        holder.getServerPlayerAwardedForExposure().ifPresent(player -> player.awardStat(Exposure.Stats.FLASHES_TRIGGERED.get()));

        sendParticlesToOtherPlayers(holder, level);

        return true;
    }

//    protected List<BlockPos> findPositions(CameraHolder holder, int distance) {
//        Entity entity = holder.asHolderEntity();
//        BlockPos headPos = BlockPos.containing(entity.getEyePosition());
//
//        List<BlockPos> positions = new ArrayList<>();
//
////        if (isPositionSuitable(entity.level(), headPos)) {
////            positions.add(headPos);
////        }
////        else {
////            for (Direction direction : Direction.values()) {
////                BlockPos relative = headPos.relative(direction);
////                if (isPositionSuitable(entity.level(), relative)) {
////                    positions.add(relative);
////                    break;
////                }
////            }
////        }
//
//        for (int i = 0; i < distance; i++) {
//            Vec3 further = holder.asHolderEntity().getEyePosition().add(holder.asHolderEntity().getLookAngle().scale(i));
//            BlockPos furtherPos = BlockPos.containing(further);
//            if (isPositionSuitable(entity.level(), furtherPos)) {
//                positions.add(furtherPos);
//            } else {
//                for (Direction direction : Direction.values()) {
//                    BlockPos relative = furtherPos.relative(direction);
//                    if (isPositionSuitable(entity.level(), relative)) {
//                        positions.add(relative);
//                        break;
//                    }
//                }
//            }
//        }
//
//        return positions;
//    }

    protected @Nullable BlockPos findPosition(CameraHolder holder) {
        Entity entity = holder.asHolderEntity();
        BlockPos headPos = BlockPos.containing(entity.getEyePosition());;
        if (isPositionSuitable(entity.level(), headPos))
            return headPos;
        else {
            for (Direction direction : Direction.values()) {
                BlockPos relative = headPos.relative(direction);
                if (isPositionSuitable(entity.level(), relative)) {
                    return relative;
                }
            }
        }
        return null;
    }

    protected boolean isPositionSuitable(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || (state.is(Blocks.WATER) && level.getFluidState(pos).isSourceOfType(Fluids.WATER));
    }

    // --

    protected void sendParticlesToOtherPlayers(CameraHolder holder, ServerLevel level) {
        Vec3 pos = getFlashEffectsPosition(holder);

        @Nullable ServerPlayer executingPlayer = holder.getServerPlayerExecutingExposure().orElse(null);

        level.players().stream()
                .filter(player -> !player.equals(executingPlayer) && player.distanceTo(holder.asHolderEntity()) < 128)
                .forEach(player -> {
                    level.sendParticles(player, ParticleTypes.FLASH, false,
                            pos.x, pos.y, pos.z, 0, 0, 0, 0, 0);
                    level.sendParticles(player, ParticleTypes.END_ROD, false,
                            pos.x, pos.y, pos.z, 4, 0.2, 0.2, 0.2, 0.1);
                });
    }

    protected Vec3 getFlashEffectsPosition(CameraHolder holder) {
        return holder.asHolderEntity().position()
                .add(0, 1.1, 0)
                .add(holder.asHolderEntity().getLookAngle().scale(0.8));
    }
}