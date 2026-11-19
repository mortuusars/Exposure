package io.github.mortuusars.exposure.world.entity.stand;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Predicate;

public class EquipmentStand extends Entity {
    protected static final EntityDataAccessor<Float> DATA_ID_DAMAGE =
          SynchedEntityData.defineId(EquipmentStand.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Integer> DATA_ID_HURT_TIME =
          SynchedEntityData.defineId(EquipmentStand.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> DATA_ID_HURT_DIRECTION =
          SynchedEntityData.defineId(EquipmentStand.class, EntityDataSerializers.INT);

    protected static final Predicate<Entity> RIDABLE_MINECARTS = entity -> entity instanceof AbstractMinecart
          && ((AbstractMinecart) entity).getMinecartType() == AbstractMinecart.Type.RIDEABLE;
    public static final Logger LOGGER = LogUtils.getLogger();

    protected EquipmentStandRedstoneControl redstoneControl = new EquipmentStandRedstoneControl(this);

    public EquipmentStand(EntityType<? extends EquipmentStand> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ID_HURT_TIME, 0);
        builder.define(DATA_ID_HURT_DIRECTION, 1);
        builder.define(DATA_ID_DAMAGE, 0.0F);
    }

    // -- Properties

    public float getDamage() {
        return entityData.get(DATA_ID_DAMAGE);
    }

    public void setDamage(float damage) {
        entityData.set(DATA_ID_DAMAGE, damage);
    }

    public int getHurtTime() {
        return entityData.get(DATA_ID_HURT_TIME);
    }

    public void setHurtTime(int hurtTime) {
        entityData.set(DATA_ID_HURT_TIME, hurtTime);
    }

    public int getHurtDirection() {
        return entityData.get(DATA_ID_HURT_DIRECTION);
    }

    public void setHurtDirection(int hurtDir) {
        entityData.set(DATA_ID_HURT_DIRECTION, hurtDir);
    }

    protected Item getDropItem() {
        return Exposure.Items.EQUIPMENT_STAND.get();
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Nullable
    @Override
    public ItemStack getPickResult() {
        return new ItemStack(getDropItem());
    }

    @Override
    protected double getDefaultGravity() {
        return 0.08;
    }

    @Override
    public @NotNull SoundSource getSoundSource() {
        return SoundSource.BLOCKS;
    }

    public void playPlaceSound() {
        playSound(Exposure.SoundEvents.CAMERA_STAND_PLACE.get(), 0.8F, 1.0F);
    }

    public void playHitSound() {
        playSound(Exposure.SoundEvents.CAMERA_STAND_HIT.get(), 0.8F, 1.0F);
    }

    public void playBreakSound() {
        playSound(Exposure.SoundEvents.CAMERA_STAND_BREAK.get(), 1.0F, 1.0F);
    }

    public void playItemSetSound() {
        playSound(Exposure.SoundEvents.CAMERA_STAND_SET_CAMERA.get(), 0.8F, 1.0F);
    }

    public void playItemRemoveSound() {
        playSound(Exposure.SoundEvents.CAMERA_STAND_REMOVE_CAMERA.get(), 0.8F, 1.0F);
    }

    public void playSound(SoundEvent event, float volume, float pitch) {
        level().playSound(null, getX(), getY(), getZ(), event, getSoundSource(), volume, pitch);
    }

    public boolean isClientSide() {
        return level().isClientSide;
    }

    // -- Physics

    // Movement largely copied from LivingEntity (and simplified a bit), to make it move like an Armor Stand
    public void travel() {
        double gravity = getGravity();
        boolean falling = getDeltaMovement().y <= 0.0;

        FluidState fluidState = level().getFluidState(blockPosition());
        if (isInWater()) {
            double y = getY();
            float waterMovementEfficiency = onGround() ? 1f : 0.5f;
            if (!onGround()) {
                waterMovementEfficiency *= 0.5f;
            }

            float waterSlowdown = 1.05f + ((0.54600006f - 0.8f) * waterMovementEfficiency);

            move(MoverType.SELF, getDeltaMovement());
            Vec3 deltaMovement = getDeltaMovement();

            setDeltaMovement(deltaMovement.multiply(waterSlowdown, 0.8F, waterSlowdown));
            Vec3 fluidMovement = getFluidFallingAdjustedMovement(gravity, falling, getDeltaMovement());
            setDeltaMovement(fluidMovement);
            if (horizontalCollision && isFree(fluidMovement.x, fluidMovement.y + 0.6F - getY() + y, fluidMovement.z)) {
                setDeltaMovement(fluidMovement.x, 0.3F, fluidMovement.z);
            }
        } else {
            BlockPos belowPos = getBlockPosBelowThatAffectsMyMovement();
            float friction = level().getBlockState(belowPos).getBlock().getFriction();
            float fxx = onGround() ? friction * 0.91F : 0.91F;
            Vec3 calculatedMovement = handleRelativeFrictionAndCalculateMovement(Vec3.ZERO, friction);
            double y = calculatedMovement.y;
            if (!level().isClientSide || level().isLoaded(belowPos)) {
                y -= gravity;
            } else if (getY() > level().getMinBuildHeight()) {
                y = -0.1;
            } else {
                y = 0.0;
            }

            setDeltaMovement(calculatedMovement.x * fxx, y * 0.98F, calculatedMovement.z * fxx);
        }
    }

    public Vec3 getFluidFallingAdjustedMovement(double gravity, boolean isFalling, Vec3 deltaMovement) {
        if (gravity != 0.0 && !isSprinting()) {
            double y;
            if (isFalling && Math.abs(deltaMovement.y - 0.005) >= 0.003 && Math.abs(deltaMovement.y - gravity / 16.0) < 0.003) {
                y = -0.003;
            } else {
                y = deltaMovement.y - gravity / 16.0;
            }

            return new Vec3(deltaMovement.x, y, deltaMovement.z);
        } else {
            return deltaMovement;
        }
    }

    public Vec3 handleRelativeFrictionAndCalculateMovement(Vec3 deltaMovement, float friction) {
        moveRelative(0, deltaMovement);
        setDeltaMovement(getDeltaMovement());
        move(MoverType.SELF, getDeltaMovement());
        return getDeltaMovement();
    }

    protected void maybeEnterBoat() {
        if (!isClientSide() && !isPassenger()) {
            List<Entity> boats = level().getEntities(this, getBoundingBox().inflate(0.4F, 0.2F, 0.4F),
                  e -> e instanceof Boat);
            for (Entity entity : boats) {
                Boat boat = ((Boat) entity);
                if (boat.getPassengers().size() < 2 && boat.hasEnoughSpaceFor(this)) {
                    this.startRiding(boat);
                }
            }
        }
    }

    protected void maybeEnterMinecart() {
        if (!isClientSide() && !isPassenger()) {
            List<Entity> minecarts = level().getEntities(this, getBoundingBox().inflate(0.4F, 0.2F, 0.4F),
                  e -> e instanceof AbstractMinecart cart && cart.getMinecartType() == AbstractMinecart.Type.RIDEABLE);
            for (Entity entity : minecarts) {
                AbstractMinecart minecart = ((AbstractMinecart) entity);
                if (!minecart.isVehicle()) {
                    this.startRiding(minecart);
                }
            }
        }
    }

    protected void pushEntities() {
        //TODO: Needs testing. RIDABLE_MINECARTS predicate seems weird, but maybe its intentional?
        for (Entity entity : this.level().getEntities(this, this.getBoundingBox(), RIDABLE_MINECARTS)) {
            if (this.distanceToSqr(entity) <= 0.2) {
                entity.push(this);
            }
        }
    }

    // --

    @Override
    public void tick() {
        super.tick();
        travel();
        maybeEnterBoat();
        maybeEnterMinecart();
        pushEntities();
        if (getHurtTime() > 0) setHurtTime(getHurtTime() - 1);
        if (getDamage() > 0.0F) setDamage(getDamage() - 1.0F);
    }

    // -- Hurt

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isRemoved()) return true;
        if (isInvulnerableTo(source)) return false;

        if (getType() != Exposure.EntityTypes.EQUIPMENT_STAND.get()) {
            ItemStack itemToDrop = getPlacedItemToDrop();
            if (!itemToDrop.isEmpty()) {
                if (!isClientSide()) {
                    @Nullable ItemEntity itemEntity = spawnAtLocation(itemToDrop, getEyeHeight());
                    if (itemEntity != null) {
                        itemEntity.setPickUpDelay(5);
                    }
                    playItemRemoveSound();
                }
            }

            @Nullable EquipmentStand equipmentStand = convertTo(Exposure.EntityTypes.EQUIPMENT_STAND.get());
            if (equipmentStand == null) return true;
            if (source.isCreativePlayer()) return true; // Prevent discard at the same hit as removing the camera

            return equipmentStand.hurt(source, 1.0f); // 1.0f prevents immediate breaking, but makes the stand shake
        }

        if (!isClientSide()) {
            setHurtDirection(-getHurtDirection());
            setHurtTime(10);
            markHurt();
            setDamage(getDamage() + amount * 10.0F);
            gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());
            playHitSound();

            //TODO: breaking in creative looks weird (immediately disappear).
            // Armor Stand should behave the same. But maybe we could do better?
            if (source.isCreativePlayer()) {
                discard();
                showBreakingParticles();
                playBreakSound();
            } else if (getDamage() > 10.0F) {
                destroy(source);
                showBreakingParticles();
                playBreakSound();
            }
        }

        return true;
    }

    protected ItemStack getPlacedItemToDrop() {
        LOGGER.warn("Missing an item to drop when converting to an empty Equipment Stand. " +
              "This is likely caused by the descendants not overriding 'getPlacedItemToDrop' method.");
        return ItemStack.EMPTY;
    }

    public void destroy(DamageSource source) {
        destroy(getDropItem());
    }

    public void destroy(Item droppedItem) {
        kill();
        if (level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            ItemStack droppedStack = new ItemStack(droppedItem);
            droppedStack.set(DataComponents.CUSTOM_NAME, getCustomName());
            spawnAtLocation(droppedStack, 0.5f);
        }
    }

    protected void showBreakingParticles() {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.defaultBlockState()),
                  getX(), getY(0.25f), getZ(), 6, (getBbWidth() / 6.0F), (getBbHeight() / 6.0F), (getBbWidth() / 6.0F), 0.05);
        }
    }

    @Nullable
    public <T extends EquipmentStand> T convertTo(EntityType<T> entityType) {
        if (isRemoved()) return null;

        T stand = entityType.create(level());
        if (stand == null) return null;

        stand.copyPosition(this);
        if (hasCustomName()) {
            stand.setCustomName(getCustomName());
            stand.setCustomNameVisible(isCustomNameVisible());
        }

        stand.setInvulnerable(isInvulnerable());

        level().addFreshEntity(stand);
        if (isPassenger()) {
            Entity entity = getVehicle();
            stopRiding();
            stand.startRiding(entity, true);
        }

        discard();
        return stand;
    }

    // -- Interact

    public boolean isInInteractionRange(LivingEntity entity) {
        double range = entity.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
        return this.getBoundingBox().distanceToSqr(entity.getEyePosition()) < range * range;
    }

    @Override
    public @NotNull InteractionResult interact(Player player, InteractionHand hand) {
        return !player.isSecondaryUseActive()
              ? handleRegularInteraction(player, hand)
              : handleSneakingInteraction(player, hand);
    }

    protected @NotNull InteractionResult handleRegularInteraction(Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);
        if (itemInHand.getItem() instanceof CameraItem) {
            @Nullable CameraStand cameraStand = convertTo(Exposure.EntityTypes.CAMERA_STAND.get());
            if (cameraStand == null) {
                LOGGER.error("Failed to convert Equipment Stand into a Camera Stand.");
                return InteractionResult.FAIL;
            }

            cameraStand.setCamera(itemInHand);
            player.setItemInHand(hand, ItemStack.EMPTY);

            //TODO: move to onItemPlaced(player, hand)
            cameraStand.setYRot(Mth.wrapDegrees(player.getYRot()));
            if (!isClientSide()) {
                cameraStand.playItemSetSound();
                cameraStand.syncRotationToClients();
                cameraStand.setOwnerPlayer(player);
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    protected InteractionResult handleSneakingInteraction(Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    public void activate() {
    }

    public void activateByRedstone() {
        activate();
    }

    // -- Save/Load

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        redstoneControl.save(tag);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        redstoneControl.load(tag);
    }
}

