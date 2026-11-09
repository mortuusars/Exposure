package io.github.mortuusars.exposure.world.entity;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.PlatformHelper;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.clientbound.CameraStandSetRotationsS2CP;
import io.github.mortuusars.exposure.network.packet.clientbound.CameraStandStopControllingS2CP;
import io.github.mortuusars.exposure.world.camera.CameraId;
import io.github.mortuusars.exposure.world.inventory.CameraOnStandAttachmentsMenu;
import io.github.mortuusars.exposure.world.item.InterplanarProjectorItem;
import io.github.mortuusars.exposure.world.item.camera.Attachment;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import io.github.mortuusars.exposure.world.sound.Sound;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class CameraStandEntity extends Entity implements CameraHolder {
    protected static final EntityDataAccessor<Integer> DATA_ID_HURT =
            SynchedEntityData.defineId(CameraStandEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> DATA_ID_HURTDIR =
            SynchedEntityData.defineId(CameraStandEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Float> DATA_ID_DAMAGE =
            SynchedEntityData.defineId(CameraStandEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<ItemStack> DATA_ID_CAMERA =
            SynchedEntityData.defineId(CameraStandEntity.class, EntityDataSerializers.ITEM_STACK);
    protected static final EntityDataAccessor<Integer> DATA_ID_COOLDOWN_TIME =
            SynchedEntityData.defineId(CameraStandEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> DATA_ID_COOLDOWN =
            SynchedEntityData.defineId(CameraStandEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> DATA_ID_OPERATOR_ID =
            SynchedEntityData.defineId(CameraStandEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Boolean> DATA_ID_MALFUNCTIONED =
            SynchedEntityData.defineId(CameraStandEntity.class, EntityDataSerializers.BOOLEAN);

    protected static final Predicate<Entity> RIDABLE_MINECARTS = entity -> entity instanceof AbstractMinecart
            && ((AbstractMinecart) entity).getMinecartType() == AbstractMinecart.Type.RIDEABLE;

    protected CameraStandRedstoneControl redstoneControl = new CameraStandRedstoneControl(this);
    protected UUID ownerPlayerId = Util.NIL_UUID;

    public CameraStandEntity(EntityType<? extends CameraStandEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ID_HURT, 0);
        builder.define(DATA_ID_HURTDIR, 1);
        builder.define(DATA_ID_DAMAGE, 0.0F);
        builder.define(DATA_ID_CAMERA, ItemStack.EMPTY);
        builder.define(DATA_ID_OPERATOR_ID, -1);
        builder.define(DATA_ID_COOLDOWN_TIME, 0);
        builder.define(DATA_ID_COOLDOWN, 0);
        builder.define(DATA_ID_MALFUNCTIONED, false);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> dataAccessor) {
        if (dataAccessor.equals(DATA_ID_CAMERA)) {
            ItemStack camera = getCamera();
            if (!camera.isEmpty()) {
                camera.setEntityRepresentation(this);
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("CooldownTime", getCooldownTime());
        tag.putInt("Cooldown", getCooldown());
        tag.putBoolean("Malfunctioned", isMalfunctioned());
        if (!getCamera().isEmpty()) {
            tag.put("Camera", getCamera().save(registryAccess()));
        }

        redstoneControl.save(tag);

        if (!ownerPlayerId.equals(Util.NIL_UUID)) {
            tag.putUUID("Owner", ownerPlayerId);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setCooldownTime(tag.getInt("CooldownTime"));
        setCooldown(tag.getInt("Cooldown"));
        setMalfunctioned(tag.getBoolean("Malfunctioned"));
        setCamera(ItemStack.parseOptional(registryAccess(), tag.getCompound("Camera")));

        redstoneControl.load(tag);

        if (tag.contains("Owner", CompoundTag.TAG_INT_ARRAY)) {
            ownerPlayerId = tag.getUUID("Owner");
        }
    }

    // -- Camera

    public ItemStack getCamera() {
        return getEntityData().get(DATA_ID_CAMERA);
    }

    public void setCamera(ItemStack cameraStack) {
        if (!isClientSide() && (cameraStack.isEmpty() || !CameraId.ofStack(getCamera()).matches(cameraStack))) {
            level().players().stream()
                    .filter(pl -> pl instanceof ServerPlayer && pl.containerMenu instanceof CameraOnStandAttachmentsMenu)
                    .forEach(pl -> ((ServerPlayer) pl).closeContainer());
        }

        getEntityData().set(DATA_ID_CAMERA, cameraStack);
    }

    public boolean isCameraActive() {
        return getCamera().getItem() instanceof CameraItem cameraItem && cameraItem.isActive(getCamera());
    }

    public Optional<Player> getOwnerPlayer() {
        return Optional.ofNullable(level().getPlayerByUUID(ownerPlayerId));
    }

    public UUID getOwnerPlayerUuid() {
        return ownerPlayerId;
    }

    public void setOwnerPlayer(Player player) {
        this.ownerPlayerId = player.getUUID();
    }

    public Optional<? extends Player> getClosestPlayerInRange() {
        return level().players().stream()
                .min(Comparator.comparingDouble(player -> player.distanceTo(this)))
                .filter(this::isInRangeForPhoto);
    }

    public int getCooldownTime() {
        return getEntityData().get(DATA_ID_COOLDOWN_TIME);
    }

    public void setCooldownTime(int cooldown) {
        getEntityData().set(DATA_ID_COOLDOWN_TIME, cooldown);
    }

    public int getCooldown() {
        return getEntityData().get(DATA_ID_COOLDOWN);
    }

    public void setCooldown(int cooldown) {
        getEntityData().set(DATA_ID_COOLDOWN, cooldown);
    }

    public void startCooldown(int cooldown) {
        setCooldownTime(cooldown);
        setCooldown(cooldown);
    }

    public boolean isOnCooldown() {
        return getCooldown() > 0;
    }

    public float getCooldownPercent() {
        float cooldownTime = getCooldownTime();
        float cooldown = getCooldown();
        return Mth.clamp(cooldown / cooldownTime, 0.0F, 1.0F);
    }

    public boolean isMalfunctioned() {
        return getEntityData().get(DATA_ID_MALFUNCTIONED);
    }

    public void setMalfunctioned(boolean malfunctioned) {
        getEntityData().set(DATA_ID_MALFUNCTIONED, malfunctioned);
    }

    // -- Operator

    public int getOperatorId() {
        return getEntityData().get(DATA_ID_OPERATOR_ID);
    }

    public @Nullable CameraOperator operator() {
        int id = getOperatorId();
        return id >= 0 && level().getEntity(id) instanceof CameraOperator operator ? operator : null;
    }

    public void setOperator(@Nullable CameraOperator operator) {
        getEntityData().set(DATA_ID_OPERATOR_ID, operator != null ? operator.asOperatorEntity().getId() : -1);
    }

    // -- Holder

    public boolean isInRangeForPhoto(Player player) {
        return player.distanceTo(this) < Config.Server.CAMERA_STAND_WORKING_RANGE.get();
    }

    @Override
    public Optional<Player> getPlayerExecutingExposure() {
        if (Config.Server.CAMERA_STAND_FALLBACK_TO_OTHER_PLAYERS.get()) {
            return getOwnerPlayer().filter(this::isInRangeForPhoto).or(this::getClosestPlayerInRange);
        } else {
            return getOwnerPlayer().filter(this::isInRangeForPhoto);
        }
    }

    @Override
    public Optional<Player> getPlayerAwardedForExposure() {
        return getOwnerPlayer().or(this::getClosestPlayerInRange);
    }

    @Override
    public @NotNull Entity getExposureAuthorEntity() {
        return getOwnerPlayer().or(this::getClosestPlayerInRange).map(pl -> (Entity) pl).orElse(this);
    }

    @Override
    public Optional<CameraOperator> getExposureCameraOperator() {
        return Optional.ofNullable(operator());
    }

    // -- Interact

    public boolean isInInteractionRange(LivingEntity entity) {
        double range = entity.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
        return this.getBoundingBox().distanceToSqr(entity.getEyePosition()) < range * range;
    }

    @Override
    public @NotNull InteractionResult interact(Player player, InteractionHand hand) {
        if (!canUse(player)) {
            player.displayClientMessage(Component.translatable("gui.exposure.camera_stand.error.in_use")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        if (isMalfunctioned()) {
            if (!isClientSide()) {
                setMalfunctioned(false);
                player.displayClientMessage(Component.translatable("gui.exposure.camera_stand.malfunction_fixed"), true);
                playSound(SoundEvents.SMITHING_TABLE_USE, 0.9f, 1.3f);
                showRepairingParticles();
            }

            return InteractionResult.SUCCESS;
        }

        ItemStack handStack = player.getItemInHand(hand);
        ItemStack cameraStack = getCamera();

        if (handStack.is(Items.DEBUG_STICK) && !cameraStack.isEmpty()) {
            malfunction();
            return InteractionResult.SUCCESS;
        }

        if (cameraStack.isEmpty() && handStack.getItem() instanceof CameraItem) {
            setCamera(handStack);
            player.setItemInHand(hand, ItemStack.EMPTY);

            if (!isClientSide()) {
                playCameraSetSound();
            }

            // Set initial camera direction.
            if (getYRot() == 0 && getXRot() == 0) { // If rotations are at 0 - player likely hasn't chosen a direction yet.
                setYRot(Mth.wrapDegrees(player.getYRot()));
                if (!isClientSide()) {
                    syncRotationToClients();
                }
            }

            return InteractionResult.SUCCESS;
        }

        if (cameraStack.isEmpty()) return InteractionResult.PASS;
        if (!(cameraStack.getItem() instanceof CameraItem cameraItem)) return InteractionResult.PASS;

        if (PlatformHelper.isCreateDeployer(player, hand)) {
            if (Config.Server.CREATE_DEPLOYER_STAND_HOTSWAP.get()) {
                return handleSneakInteraction(player, hand, cameraItem, cameraStack, handStack);
            }
            return InteractionResult.CONSUME;
        }

        if (player.isSecondaryUseActive()) {
            return handleSneakInteraction(player, hand, cameraItem, cameraStack, handStack);
        }

        startControlling(player, cameraItem);
        return InteractionResult.CONSUME; // To not cause arm swing, which causes viewfinder use/attack animation.
    }

    protected InteractionResult handleSneakInteraction(Player player, InteractionHand hand, CameraItem cameraItem, ItemStack cameraStack, ItemStack handStack) {
        if (isOnCooldown()) return InteractionResult.FAIL;
        if (cameraItem.getShutter().isOpen(cameraStack)) return InteractionResult.FAIL;

        if (handStack.isEmpty() && cameraItem.hasAttachmentsMenu()) {
            return openAttachmentsMenu(player, hand);
        }

        InteractionResult result = cameraItem.handleStandSneakInteraction(this, player, hand, cameraStack);
        if (result == InteractionResult.FAIL) {
            return InteractionResult.FAIL;
        }

        if (result == InteractionResult.PASS) {
            return cameraItem.hasAttachmentsMenu()
                    ? openAttachmentsMenu(player, hand)
                    : InteractionResult.PASS;
        }

        cameraItem.getTimer().stop(cameraStack);

        forceUpdate();
        return result;
    }

    public boolean canUse(Player player) {
        return (operator() == null || player.equals(operator())) && level().players().stream()
                .filter(pl -> !pl.equals(player))
                .noneMatch(pl -> pl.containerMenu instanceof CameraOnStandAttachmentsMenu);
    }

    public InteractionResult openAttachmentsMenu(Player player, InteractionHand hand) {
        if (!canUse(player)) {
            player.displayClientMessage(Component.translatable("gui.exposure.camera_stand.error.in_use")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        ItemStack cameraStack = getCamera();

        if (cameraStack.isEmpty() || !(cameraStack.getItem() instanceof CameraItem cameraItem))
            return InteractionResult.FAIL;

        if (cameraItem.getShutter().isOpen(cameraStack)) {
            player.displayClientMessage(Component.translatable("item.exposure.camera.camera_attachments.fail.shutter_open")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        cameraItem.getOrCreateId(cameraStack);

        if (player instanceof ServerPlayer serverPlayer) {
            setOperator(serverPlayer);
            cameraItem.getTimer().stop(cameraStack);

            MenuProvider menuProvider = new MenuProvider() {
                @Override
                public @NotNull Component getDisplayName() {
                    return cameraStack.get(DataComponents.CUSTOM_NAME) != null
                            ? cameraStack.getHoverName() : Component.translatable("container.exposure.camera");
                }

                @Override
                public @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
                    return new CameraOnStandAttachmentsMenu(containerId, playerInventory, CameraStandEntity.this);
                }
            };

            forceUpdate();

            PlatformHelper.openMenu(serverPlayer, menuProvider,
                    buffer -> buffer.writeInt(getId()));
        }

        cameraItem.setDisassembled(cameraStack, true);
        Sound.play(player, Exposure.SoundEvents.CAMERA_GENERIC_CLICK.get(), SoundSource.PLAYERS, 0.9f, 0.9f, 0.2f);

        return InteractionResult.SUCCESS;
    }

    public void startControlling(Player player, CameraItem cameraItem) {
        cameraItem.activateOnStand(player, getCamera(), this);
        setOperator(player);

        if (getOwnerPlayerUuid().equals(Util.NIL_UUID)) {
            setOwnerPlayer(player);
        }

        if (isClientSide()) {
            CameraClient.setCameraEntity(this);
            Minecrft.stopPlayerMovement();
        } else {
            forceUpdate();
        }
    }

    public void stopControlling() {
        CameraOperator operator = operator();

        if (getCamera().getItem() instanceof CameraItem cameraItem && cameraItem.isActive(getCamera())) {
            Entity operatorEntity = operator != null ? operator.asOperatorEntity() : this;
            cameraItem.deactivate(operatorEntity, this, getCamera());
        }

        if (operator != null) {
            if (isClientSide()) {
                CameraClient.resetCameraEntity();
            } else if (operator instanceof ServerPlayer serverPlayer) {
                // This method is usually called on both sides, but in the case of client/server desync,
                // (which is sometimes happening on contraptions)
                // this will ensure that client will be updated properly, otherwise cameraEntity will not be set back to player.
                Packets.sendToClient(new CameraStandStopControllingS2CP(this.getId()), serverPlayer);
            }
        }

        setOperator(null);

        if (!isClientSide()) {
            forceUpdate();
        }
    }

    public void release() {
        if (!isMalfunctioned() && !isOnCooldown() && getCamera().getItem() instanceof CameraItem cameraItem) {
            getPlayerExecutingExposure()
                    .filter(player -> !shouldMalfunction(player))
                    .ifPresentOrElse(
                            player -> cameraItem.release(this, getCamera()),
                            this::malfunction);
            forceUpdate();
        }
    }

    protected boolean shouldMalfunction(Player player) {
        if (!Config.Server.CAMERA_STAND_FALLBACK_TO_OTHER_PLAYERS_PROJECTOR.get()
                && Attachment.FILTER.get(getCamera()).getItem() instanceof InterplanarProjectorItem
                && !player.getUUID().equals(getOwnerPlayerUuid())) {
            return true;
        }

        return false;
    }

    protected void malfunction() {
        if (getCamera().isEmpty()) return;
        if (Config.Server.CAMERA_STAND_RANGE_MALFUNCTION.isFalse()) return;
        stopControlling();
        setMalfunctioned(true);
        playSound(Exposure.SoundEvents.CAMERA_GENERIC_CLICK.get());
    }

    // --

    @Override
    public void tick() {
        super.tick();
        travel();
        checkForBoats();
        checkForMinecarts();
        pushEntities();

        if (getHurtTime() > 0) {
            setHurtTime(getHurtTime() - 1);
        }

        if (getDamage() > 0.0F) {
            setDamage(getDamage() - 1.0F);
        }

        int cooldown = getCooldown();
        if (cooldown > 0) {
            setCooldown(cooldown - 1);
        }

        if (isClientSide()) {
            return;
        }

        redstoneControl.tick();

        if (getCamera().getItem() instanceof CameraItem cameraItem && cameraItem.tick(this, getCamera())) {
            forceUpdate();
        }

        @Nullable CameraOperator operator = operator();
        if (operator == null) {
            if (getCamera().getItem() instanceof CameraItem cameraItem && cameraItem.isActive(getCamera())) {
                cameraItem.deactivate(this, this, getCamera());
            }
        } else {
            if (!isInInteractionRange(operator.asOperatorEntity())) {
                stopControlling();
            } else if (!isCameraActive() && !(operator instanceof Player player
                    && player.containerMenu instanceof CameraOnStandAttachmentsMenu)) {
                stopControlling();
            }
        }
    }

    protected void travel() {
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();

        this.applyGravity();

        if (this.isInWater() && this.getFluidHeight(FluidTags.WATER) > 0.1F) {
            this.setUnderwaterMovement();
        } else if (this.isInLava() && this.getFluidHeight(FluidTags.LAVA) > 0.1F) {
            this.setUnderLavaMovement();
        }

        if (this.level().isClientSide) {
            this.noPhysics = false;
        } else {
            this.noPhysics = !this.level().noCollision(this, this.getBoundingBox().deflate(1.0E-7));
            if (this.noPhysics) {
                this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
            }
        }

        if (!this.onGround() || this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-5F || (this.tickCount + this.getId()) % 4 == 0) {
            this.move(MoverType.SELF, this.getDeltaMovement());
            float f = 0.98F;
            if (this.onGround()) {
                f = this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction() * 0.98F;
            }

            this.setDeltaMovement(this.getDeltaMovement().multiply(f, 0.98, f));
            if (this.onGround()) {
                Vec3 vec32 = this.getDeltaMovement();
                if (vec32.y < 0.0) {
                    this.setDeltaMovement(vec32.multiply(1.0, -0.5, 1.0));
                }
            }
        }
    }

    protected void setUnderwaterMovement() {
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.x * 0.9F, vec3.y * 0.23F, vec3.z * 0.9F);
    }

    protected void setUnderLavaMovement() {
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.x * 0.5F, vec3.y * 0.1F, vec3.z * 0.5F);
    }

    protected void checkForBoats() {
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

    protected void checkForMinecarts() {
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
        for (Entity entity : this.level().getEntities(this, this.getBoundingBox(), RIDABLE_MINECARTS)) {
            if (this.distanceToSqr(entity) <= 0.2) {
                entity.push(this);
            }
        }
    }

    // -- Hurt

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isRemoved()) return true;
        if (isInvulnerableTo(source)) return false;

        markHurt();

        if (!getCamera().isEmpty()) {
            if (!isClientSide()) {
                @Nullable ItemEntity itemEntity = spawnAtLocation(getCamera(), getEyeHeight());
                if (itemEntity != null) {
                    itemEntity.setPickUpDelay(5);
                }
                playCameraRemoveSound();
            }
            setCamera(ItemStack.EMPTY);

            if (source.isCreativePlayer()) {
                return true; // Prevent discard at the same hit as removing the camera.
            }

            amount = 1.0f; // Prevent one-hit harvesting.
        }

        if (!isClientSide()) {
            setHurtDir(-getHurtDir());
            setHurtTime(10);
            markHurt();
            setDamage(getDamage() + amount * 10.0F);
            gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());
            playHitSound();

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

    public void setHurtTime(int hurtTime) {
        this.entityData.set(DATA_ID_HURT, hurtTime);
    }

    public void setHurtDir(int hurtDir) {
        this.entityData.set(DATA_ID_HURTDIR, hurtDir);
    }

    public void setDamage(float damage) {
        this.entityData.set(DATA_ID_DAMAGE, damage);
    }

    public float getDamage() {
        return this.entityData.get(DATA_ID_DAMAGE);
    }

    public int getHurtTime() {
        return this.entityData.get(DATA_ID_HURT);
    }

    public int getHurtDir() {
        return this.entityData.get(DATA_ID_HURTDIR);
    }

    protected void destroy(DamageSource source) {
        this.destroy(this.getDropItem());
    }

    public void destroy(Item dropItem) {
        this.kill();
        if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            ItemStack itemStack = new ItemStack(dropItem);
            itemStack.set(DataComponents.CUSTOM_NAME, this.getCustomName());
            this.spawnAtLocation(itemStack, 0.5f);
        }
    }

    protected void showBreakingParticles() {
        if (this.level() instanceof ServerLevel) {
            ((ServerLevel) this.level())
                    .sendParticles(
                            new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.defaultBlockState()),
                            this.getX(),
                            this.getY(0.25f),
                            this.getZ(),
                            6,
                            (this.getBbWidth() / 6.0F),
                            (this.getBbHeight() / 6.0F),
                            (this.getBbWidth() / 6.0F),
                            0.05
                    );
        }
    }

    protected void showRepairingParticles() {
        if (this.level() instanceof ServerLevel) {
            ((ServerLevel) this.level())
                    .sendParticles(
                            new BlockParticleOption(ParticleTypes.BLOCK, Blocks.HOPPER.defaultBlockState()),
                            this.getX(),
                            this.getY(0.75f),
                            this.getZ(),
                            4,
                            (this.getBbWidth() / 8.0F),
                            (this.getBbHeight() / 8.0F),
                            (this.getBbWidth() / 8.0F),
                            0.05
                    );
        }
    }

    protected Item getDropItem() {
        return Exposure.Items.CAMERA_STAND.get();
    }

    // --

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
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                Exposure.SoundEvents.CAMERA_STAND_PLACE.get(), this.getSoundSource(), 0.8F, 1.0F);
    }

    public void playHitSound() {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                Exposure.SoundEvents.CAMERA_STAND_HIT.get(), this.getSoundSource(), 0.8F, 1.0F);
    }

    public void playBreakSound() {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                Exposure.SoundEvents.CAMERA_STAND_BREAK.get(), this.getSoundSource(), 1.0F, 1.0F);
    }

    public void playCameraSetSound() {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                Exposure.SoundEvents.CAMERA_STAND_SET_CAMERA.get(), this.getSoundSource(), 0.8F, 1.0F);
    }

    public void playCameraRemoveSound() {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                Exposure.SoundEvents.CAMERA_STAND_REMOVE_CAMERA.get(), this.getSoundSource(), 0.8F, 1.0F);
    }

    /**
     * Forces data sync to the client.
     */
    public void forceUpdate() {
        getEntityData().set(DATA_ID_CAMERA, getCamera(), true);
    }

    public boolean isClientSide() {
        return level().isClientSide;
    }

    @Override
    public boolean isControlledByLocalInstance() {
        return operator() instanceof Player player && player.isLocalPlayer() || super.isControlledByLocalInstance();
    }

    // --

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        this.setPos(x, y, z);
        // this method is called when client receives packet from server,
        // and it makes camera rotation jump around and be janky.
        // we sync rotations manually instead
        // this.setRot(yRot, xRot);
    }

    public void syncRotationToClientsIfNeeded() {
        if (isClientSide()) return;
        if (operator() != null || (tickCount % 10 == 0)) {
            syncRotationToClients();
        }
    }

    public void syncRotationToClients() {
        Packets.sendToClients(new CameraStandSetRotationsS2CP(this.getId(), getYRot(), getXRot()), pl -> !pl.equals(operator()));
    }
}
