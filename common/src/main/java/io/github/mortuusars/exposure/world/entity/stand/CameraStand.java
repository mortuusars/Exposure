package io.github.mortuusars.exposure.world.entity.stand;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.PlatformHelper;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.input.Mouse;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.entity.CameraOperator;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.mortaar.network.Packets;
import io.github.mortuusars.exposure.network.packet.clientbound.ClientboundCameraStandSetRotationsPacket;
import io.github.mortuusars.exposure.network.packet.clientbound.ClientboundCameraStandStopControllingPacket;
import io.github.mortuusars.exposure.world.inventory.CameraOnStandAttachmentsMenu;
import io.github.mortuusars.exposure.world.item.InterplanarProjectorItem;
import io.github.mortuusars.exposure.world.item.camera.Attachment;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import io.github.mortuusars.exposure.world.sound.Sound;
import io.github.mortuusars.mortaar.Platform;
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
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CameraStand extends EquipmentStand implements CameraHolder {
    protected static final EntityDataAccessor<ItemStack> DATA_ID_CAMERA =
          SynchedEntityData.defineId(CameraStand.class, EntityDataSerializers.ITEM_STACK);
    protected static final EntityDataAccessor<Integer> DATA_ID_COOLDOWN_TIME =
          SynchedEntityData.defineId(CameraStand.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> DATA_ID_COOLDOWN =
          SynchedEntityData.defineId(CameraStand.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> DATA_ID_OPERATOR_ID =
          SynchedEntityData.defineId(CameraStand.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Boolean> DATA_ID_MALFUNCTIONED =
          SynchedEntityData.defineId(CameraStand.class, EntityDataSerializers.BOOLEAN);

    protected UUID ownerPlayerId = Util.NIL_UUID;

    public CameraStand(EntityType<? extends CameraStand> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ID_CAMERA, ItemStack.EMPTY);
        builder.define(DATA_ID_OPERATOR_ID, -1);
        builder.define(DATA_ID_COOLDOWN_TIME, 0);
        builder.define(DATA_ID_COOLDOWN, 0);
        builder.define(DATA_ID_MALFUNCTIONED, false);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> dataAccessor) {
        if (dataAccessor.equals(DATA_ID_CAMERA)) {
            ItemStack item = getCamera();
            if (!item.isEmpty()) {
                item.setEntityRepresentation(this);
            }
        }
    }

    // -- Item

    public ItemStack getCamera() {
        return getEntityData().get(DATA_ID_CAMERA);
    }

    public void setCamera(ItemStack camera) {
        if (level() instanceof ServerLevel serverLevel
              && getCamera().getItem() instanceof CameraItem
              && !(camera.getItem() instanceof CameraItem)) {
            for (ServerPlayer player : serverLevel.players()) {
                if (player.containerMenu instanceof CameraOnStandAttachmentsMenu onStandAttachmentsMenu
                      && onStandAttachmentsMenu.getStand().getId() == getId()) {
                    player.closeContainer();
                }
            }
        }

        getEntityData().set(DATA_ID_CAMERA, camera);
    }

    public boolean isCameraActive() {
        return getCamera().getItem() instanceof CameraItem cameraItem && cameraItem.isActive(getCamera());
    }

    @Override
    protected ItemStack getPlacedItemToDrop() {
        return getCamera();
    }

    // --

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

    public UUID getOwnerPlayerUuid() {
        return ownerPlayerId;
    }

    public Optional<Player> getOwnerPlayer() {
        return Optional.ofNullable(level().getPlayerByUUID(getOwnerPlayerUuid()));
    }

    public void setOwnerPlayer(Player player) {
        this.ownerPlayerId = player.getUUID();
    }

    public Optional<? extends Player> getClosestPlayerInRange() {
        return level().players().stream()
              .min(Comparator.comparingDouble(player -> player.distanceTo(this)))
              .filter(this::isInRangeForPhoto);
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

        if (handStack.is(Items.DEBUG_STICK)) {
            malfunction();
            return InteractionResult.SUCCESS;
        }

//        if (getCamera().isEmpty() && !handStack.isEmpty()) {
//            setCamera(handStack);
//            player.setItemInHand(hand, ItemStack.EMPTY);
//
//            if (!isClientSide()) {
//                playItemSetSound();
//            }
//
//            // Set initial camera direction.
//            if (getYRot() == 0 && getXRot() == 0) { // If rotations are at 0 - player likely hasn't chosen a direction yet.
//                setYRot(Mth.wrapDegrees(player.getYRot()));
//                if (!isClientSide()) {
//                    syncRotationToClients();
//                }
//            }
//
//            return InteractionResult.SUCCESS;
//        }

        return !player.isSecondaryUseActive()
              ? handleRegularInteraction(player, hand)
              : handleSneakingInteraction(player, hand);
    }

    protected @NotNull InteractionResult handleRegularInteraction(Player player, InteractionHand hand) {
        if (PlatformHelper.isCreateDeployer(player, hand)) {
            return Config.Server.CREATE_DEPLOYER_STAND_HOTSWAP.get()
                  ? handleSneakingInteraction(player, hand)
                  : InteractionResult.CONSUME;
        }

        startControlling(player);
        return InteractionResult.CONSUME; // To not cause arm swing, which causes viewfinder use/attack animation.
    }

    protected InteractionResult handleSneakingInteraction(Player player, InteractionHand hand) {
        if (isOnCooldown()) return InteractionResult.FAIL;
        if (!(getCamera().getItem() instanceof CameraItem cameraItem)) return InteractionResult.PASS;

        ItemStack handStack = player.getItemInHand(hand);
        ItemStack cameraStack = getCamera();

        if (cameraItem.getShutter().isOpen(cameraStack)) {
            return InteractionResult.FAIL;
        }

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
                    return new CameraOnStandAttachmentsMenu(containerId, playerInventory, CameraStand.this);
                }
            };

            forceUpdate();

            Platform.openMenu(serverPlayer, menuProvider, buffer -> buffer.writeInt(getId()));
        }

        cameraItem.setDisassembled(cameraStack, true);
        Sound.play(player, Exposure.SoundEvents.CAMERA_GENERIC_CLICK.get(), SoundSource.PLAYERS, 0.9f, 0.9f, 0.2f);

        return InteractionResult.SUCCESS;
    }

    public void startControlling(Player player) {
        setOperator(player);

        if (getOwnerPlayerUuid().equals(Util.NIL_UUID)) {
            setOwnerPlayer(player);
        }

        if (isClientSide()) {
            Minecrft.stopPlayerMovement();
            Mouse.setControlledStand(this);
        } else {
            forceUpdate();
        }

        if (getCamera().getItem() instanceof CameraItem cameraItem) {
            cameraItem.activateOnStand(player, getCamera(), this);
            if (isClientSide()) {
                CameraClient.setCameraEntity(this);
            }
        }
    }

    public void stopControlling() {
        CameraOperator operator = operator();

        if (isClientSide()) {
            Mouse.setControlledStand(null);
        }

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
                Packets.sendToClient(new ClientboundCameraStandStopControllingPacket(this.getId()), serverPlayer);
            }
        }

        setOperator(null);

        if (!isClientSide()) {
            forceUpdate();
        }
    }

    public void activate() {
        if (!isOnCooldown() && !isMalfunctioned() && getCamera().getItem() instanceof CameraItem cameraItem) {
            getPlayerExecutingExposure()
                  .filter(player -> !shouldMalfunction(player))
                  .ifPresentOrElse(
                        player -> cameraItem.release(this, getCamera()),
                        this::malfunction);
            forceUpdate();
        }
    }

    public void activateByRedstone() {
        activate();
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

        int cooldown = getCooldown();
        if (cooldown > 0) {
            setCooldown(cooldown - 1);
        }

        if (isClientSide()) {
            return;
        }

        if (getCamera().getItem() instanceof CameraItem cameraItem && cameraItem.tick(this, getCamera())) {
            forceUpdate();
        }

        @Nullable CameraOperator operator = operator();
        if (operator == null) {
            if (getCamera().getItem() instanceof CameraItem cameraItem && cameraItem.isActive(getCamera())) {
                cameraItem.deactivate(this, this, getCamera());
            }
        } else {
            if (!isInInteractionRange(operator.asOperatorEntity())
                  || getCamera().isEmpty()
                  || (getCamera().getItem() instanceof CameraItem && !isCameraActive() && !(operator instanceof Player player
                  && player.containerMenu instanceof CameraOnStandAttachmentsMenu))) {
                stopControlling();
            }
        }
    }

    // -- Hurt

//    @Override
//    public boolean hurt(DamageSource source, float amount) {
//        if (isRemoved()) return true;
//        if (isInvulnerableTo(source)) return false;
//
//
//
//        markHurt();
//
//        if (!getCamera().isEmpty()) {
//            if (!isClientSide()) {
//                @Nullable ItemEntity itemEntity = spawnAtLocation(getCamera(), getEyeHeight());
//                if (itemEntity != null) {
//                    itemEntity.setPickUpDelay(5);
//                }
//                playCameraRemoveSound();
//            }
//            setCamera(ItemStack.EMPTY);
//
//            if (source.isCreativePlayer()) {
//                return true; // Prevent discard at the same hit as removing the camera.
//            }
//
//            amount = 1.0f; // Prevent one-hit harvesting.
//        }
//
//        if (!isClientSide()) {
//            setHurtDirection(-getHurtDirection());
//            setHurtTime(10);
//            markHurt();
//            setDamage(getDamage() + amount * 10.0F);
//            gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());
//            playHitSound();
//
//            if (source.isCreativePlayer()) {
//                discard();
//                showBreakingParticles();
//                playBreakSound();
//            } else if (getDamage() > 10.0F) {
//                destroy(source);
//                showBreakingParticles();
//                playBreakSound();
//            }
//
//        }
//
//        return true;
//    }

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

    // --

    /**
     * Forces data sync to the client.
     */
    public void forceUpdate() {
        getEntityData().set(DATA_ID_CAMERA, getCamera(), true);
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
        Packets.sendToClients(new ClientboundCameraStandSetRotationsPacket(this.getId(), getYRot(), getXRot()), pl -> !pl.equals(operator()));
    }

    // -- Save/Load

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        if (!getCamera().isEmpty()) {
            tag.put("Camera", getCamera().save(registryAccess()));
        }
        tag.putInt("CooldownTime", getCooldownTime());
        tag.putInt("Cooldown", getCooldown());
        tag.putBoolean("Malfunctioned", isMalfunctioned());

        if (!ownerPlayerId.equals(Util.NIL_UUID)) {
            tag.putUUID("Owner", ownerPlayerId);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        setCamera(ItemStack.parseOptional(registryAccess(), tag.getCompound("Camera")));
        setCooldownTime(tag.getInt("CooldownTime"));
        setCooldown(tag.getInt("Cooldown"));
        setMalfunctioned(tag.getBoolean("Malfunctioned"));

        if (tag.contains("Owner", CompoundTag.TAG_INT_ARRAY)) {
            ownerPlayerId = tag.getUUID("Owner");
        }
    }
}
