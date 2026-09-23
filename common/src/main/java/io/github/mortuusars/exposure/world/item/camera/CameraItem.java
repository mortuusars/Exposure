package io.github.mortuusars.exposure.world.item.camera;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.*;
import io.github.mortuusars.exposure.data.*;
import io.github.mortuusars.exposure.network.packet.clientbound.ActionS2CP;
import io.github.mortuusars.exposure.util.color.Color;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.camera.*;
import io.github.mortuusars.exposure.world.camera.capture.CaptureType;
import io.github.mortuusars.exposure.world.camera.component.FocalRange;
import io.github.mortuusars.exposure.world.camera.component.SelfTimer;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.world.camera.capture.CaptureParameters;
import io.github.mortuusars.exposure.world.camera.capture.Projection;
import io.github.mortuusars.exposure.world.camera.film.properties.FilmProperties;
import io.github.mortuusars.exposure.world.camera.frame.*;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import io.github.mortuusars.exposure.world.item.FilmRollItem;
import io.github.mortuusars.exposure.world.item.InterplanarProjectorItem;
import io.github.mortuusars.exposure.world.item.SensitiveFilmItem;
import io.github.mortuusars.exposure.world.item.component.StoredItemStack;
import io.github.mortuusars.exposure.world.inventory.CameraInHandAttachmentsMenu;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.clientbound.CaptureStartS2CP;
import io.github.mortuusars.exposure.network.packet.serverbound.OpenCameraAttachmentsInCreativePacketC2SP;
import io.github.mortuusars.exposure.server.CameraInstance;
import io.github.mortuusars.exposure.server.CameraInstances;
import io.github.mortuusars.exposure.world.level.LevelUtil;
import io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier;
import io.github.mortuusars.exposure.util.*;
import io.github.mortuusars.exposure.world.sound.Sound;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CameraItem extends Item {
    public static final int BASE_COOLDOWN = 2;
    public static final int PROJECT_COOLDOWN = 20;

    protected final Shutter shutter;
    protected final Timer timer;
    protected final Flash flash;
    protected final List<Attachment<?>> attachments;
    protected final List<ShutterSpeed> availableShutterSpeeds;

    public CameraItem(Properties properties) {
        super(properties);
        this.shutter = createShutter();
        this.timer = createTimer();
        this.flash = createFlash();
        this.attachments = defineAttachments();
        this.availableShutterSpeeds = defineShutterSpeeds();

        shutter.onOpen(this::onShutterOpen);
        shutter.onClosed(this::onShutterClosed);
    }

    protected Shutter createShutter() {
        return new Shutter();
    }

    protected Timer createTimer() {
        return new Timer();
    }

    protected Flash createFlash() {
        return new Flash();
    }

    protected @NotNull List<Attachment<?>> defineAttachments() {
        return List.of(Attachment.FILM, Attachment.FLASH, Attachment.LENS, Attachment.FILTER);
    }

    protected List<ShutterSpeed> defineShutterSpeeds() {
        return List.of(
                new ShutterSpeed("1/500"),
                new ShutterSpeed("1/250"),
                new ShutterSpeed("1/125"),
                new ShutterSpeed("1/60"),
                new ShutterSpeed("1/30"),
                new ShutterSpeed("1/15"),
                new ShutterSpeed("1/8"),
                new ShutterSpeed("1/4"),
                new ShutterSpeed("1/2"),
                new ShutterSpeed("1\"")
        );
    }

    public boolean hasAttachmentsMenu() {
        return true;
    }

    // --

    public Shutter getShutter() {
        return shutter;
    }

    public Timer getTimer() {
        return timer;
    }

    public Flash getFlash() {
        return flash;
    }

    public List<ShutterSpeed> getAvailableShutterSpeeds() {
        return availableShutterSpeeds;
    }

    public List<Attachment<?>> getAttachments() {
        return attachments;
    }

    public Attachment<?> getFilmAttachment() {
        return Attachment.FILM;
    }

    public @NotNull FilmProperties getFilmProperties(ItemStack stack) {
        ItemStack filmStack = getFilmAttachment().get(stack).getForReading();
        if (!(filmStack.getItem() instanceof SensitiveFilmItem filmItem)) {
            throw new IllegalStateException("Cannot take a photo without SensitiveFilmItem in the camera. stack: " + stack);
        }
        return filmItem.getFilmProperties(filmStack);
    }

    public SoundEvent getViewfinderOpenSound() {
        return Exposure.SoundEvents.VIEWFINDER_OPEN.get();
    }

    public SoundEvent getViewfinderCloseSound() {
        return Exposure.SoundEvents.VIEWFINDER_CLOSE.get();
    }

    public SoundEvent getReleaseButtonSound() {
        return Exposure.SoundEvents.CAMERA_RELEASE_BUTTON_CLICK.get();
    }

    public ResourceLocation getCaptureType(ItemStack stack) {
        return CaptureType.CAMERA;
    }

    public double getSelfieCameraDistance(ItemStack stack) {
        return Config.Server.SELFIE_CAMERA_DISTANCE.get();
    }

    public double getYPositionOffset(ItemStack stack) {
        return Config.Server.WAIST_LEVEL_VIEWFINDER.get() ? -0.35 : 0.0;
    }

    public float getScaleOnStand() {
        return 0.9f;
    }

    public float getCropFactor() {
        return 0.875f; // Crops viewfinder border
    }

    public FocalRange getFocalRange(RegistryAccess registryAccess, ItemStack stack) {
        return Attachment.LENS.map(stack, lensStack -> Lenses.getFocalRangeOrDefault(registryAccess, lensStack))
                .orElse(FocalRange.getDefault());
    }

    public double getFov(Level level, ItemStack stack) {
        double zoom = CameraSettings.ZOOM.getOrDefault(stack);
        FocalRange focalRange = getFocalRange(level.registryAccess(), stack);
        return focalRange.fovFromZoom(zoom);
    }

    /**
     * Fov of what's seen when looking through viewfinder.
     */
    public double getViewfinderFov(Level level, ItemStack stack) {
        return getFov(level, stack) * getCropFactor();
    }

    public PointOfView getPointOfView(CameraHolder holder, ItemStack stack) {
        if (isInSelfieMode(stack)) {
            return PointOfView.of(holder)
                    .reverseDirection()
                    .limitMaxDistance(holder, getSelfieCameraDistance(stack))
                    .rotateX(-CameraSettings.SELFIE_ROTATION_X.getOrDefault(stack))
                    .rotateY(-CameraSettings.SELFIE_ROTATION_Y.getOrDefault(stack));
        } else {
            return PointOfView.of(holder)
                    .move(0, getYPositionOffset(stack), 0);
        }
    }

    public Optional<Filter> getFilter(RegistryAccess registryAccess, ItemStack stack) {
        return Attachment.FILTER.map(stack, filter -> Filters.of(registryAccess, filter)).flatMap(Function.identity());
    }

    public Optional<ResourceLocation> getFilterShaderLocation(RegistryAccess registryAccess, ItemStack stack) {
        return getFilter(registryAccess, stack).map(Filter::shader);
    }

    protected Optional<ColorChannel> getChromaticChannel(ItemStack stack) {
        return Attachment.FILTER.map(stack, ColorChannel::fromFilterStack).orElse(Optional.empty());
    }

    protected Optional<Projection> getProjection(ItemStack stack) {
        return Attachment.FILTER.map(stack, (filterItem, filterStack) ->
                        filterItem instanceof InterplanarProjectorItem projectorItem
                                ? projectorItem.getProjection(filterStack)
                                : Optional.<Projection>empty())
                .orElse(Optional.empty());
    }

    // --

    public CameraId getOrCreateId(ItemStack stack) {
        CameraId cameraId = Exposure.DataComponents.getCameraId(stack);
        if (cameraId == null) {
            cameraId = CameraId.create();
            Exposure.DataComponents.setCameraId(stack,cameraId);
        }
        return cameraId;
    }

    public boolean isInSelfieMode(ItemStack stack) {
        return CameraSettings.SELFIE_MODE.getOrDefault(stack);
    }

    public boolean isActive(ItemStack stack) {
        return Exposure.DataComponents.getCameraActive(stack,false);
    }

    public void setActive(ItemStack stack, boolean active) {
        Exposure.DataComponents.setCameraActive(stack,active);
    }

    public boolean isDisassembled(ItemStack stack) {
        return Exposure.DataComponents.getCameraDisassembled(stack, false);
    }

    public void setDisassembled(ItemStack stack, boolean disassembled) {
        if (!disassembled) {
            Exposure.DataComponents.setCameraDisassembled(stack,null);
        } else {
            Exposure.DataComponents.setCameraDisassembled(stack,true);
        }
    }

    public long getLastActionTime(ItemStack stack) {
        return Exposure.DataComponents.getLastCameraActionTime(stack,-1L);
    }

    public void setLastActionTime(ItemStack stack, long lastActionTime) {
        Exposure.DataComponents.setCameraLastActionTime(stack, lastActionTime);
    }

    public void actionPerformed(ItemStack stack, CameraHolder holder) {
        setLastActionTime(stack, holder.asHolderEntity().level().getGameTime());
        holder.asHolderEntity().gameEvent(GameEvent.ITEM_INTERACT_FINISH);
    }

    public @NotNull InteractionResultHolder<ItemStack> activateInHand(Player player, ItemStack stack, @NotNull InteractionHand hand) {
        player.setActiveExposureCamera(new CameraInHand(player, getOrCreateId(stack), hand));
        if (player.level().isClientSide) {
            Minecrft.releaseUseButton(); // Releasing use key to not take a shot immediately, if right click is still held.
        }
        return activate(player, stack);
    }

    public @NotNull InteractionResultHolder<ItemStack> activateOnStand(Player player, ItemStack stack, CameraStandEntity cameraStand) {
        player.setActiveExposureCamera(new CameraOnStand(player, cameraStand, getOrCreateId(stack)));
        if (player.level().isClientSide) {
            Minecrft.releaseUseButton(); // Releasing use key to not take a shot immediately, if right click is still held.
        }
        return activate(player, stack);
    }

    public @NotNull InteractionResultHolder<ItemStack> activate(Entity entity, ItemStack stack) {
        setActive(stack, true);
        setDisassembled(stack, false);
        Sound.play(entity, getViewfinderOpenSound(), entity.getSoundSource(), 0.35f, 0.9f, 0.2f);
        entity.gameEvent(GameEvent.EQUIP);
        return InteractionResultHolder.consume(stack);
    }

    public @NotNull InteractionResultHolder<ItemStack> deactivate(Entity entity, ItemStack stack) {
        setActive(stack, false);
        CameraSettings.SELFIE_MODE.set(stack, false);
        Sound.play(entity, getViewfinderCloseSound(), entity.getSoundSource(), 0.35f, 0.9f, 0.2f);
        entity.gameEvent(GameEvent.EQUIP);
        return InteractionResultHolder.consume(stack);
    }

    public int calculateCooldownAfterShot(ItemStack stack, CaptureParameters captureParameters) {
        if (captureParameters.projection().isPresent()) return PROJECT_COOLDOWN;
        if (captureParameters.getFlash()) return getFlash().getCooldown();
        return BASE_COOLDOWN;
    }

    // --

    public boolean isBarVisible(@NotNull ItemStack stack) {
        return Config.Client.CAMERA_SHOW_FILM_BAR_ON_ITEM.get()
                && Attachment.FILM.map(stack, FilmRollItem::isBarVisible).orElse(false);
    }

    public int getBarWidth(@NotNull ItemStack stack) {
        return Attachment.FILM.map(stack, FilmRollItem::getBarWidth).orElse(0);
    }

    public int getBarColor(@NotNull ItemStack stack) {
        return Attachment.FILM.map(stack, FilmRollItem::getBarColor).orElse(0);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level context, List<Component> components, TooltipFlag tooltipFlag) {
        if (Config.Client.CAMERA_SHOW_FILM_FRAMES_IN_TOOLTIP.get()) {
            Attachment.FILM.ifPresent(stack, (filmItem, filmStack) -> {
                int exposed = filmItem.getStoredFramesCount(filmStack);
                int max = filmItem.getMaxFrameCount(filmStack);
                components.add(Component.translatable("item.exposure.camera.tooltip.film_roll_frames", exposed, max));
            });
        }

        if (Config.Client.CAMERA_SHOW_TOOLTIP_DETAILS.get()) {
            if (stack.getEntityRepresentation() instanceof CameraStandEntity) {
                if (Screen.hasShiftDown()) {
                    components.add(Component.translatable("item.exposure.camera.tooltip.details_attachments_screen_on_stand"));
                    components.add(Component.translatable("item.exposure.camera.tooltip.details_hotswap_on_stand"));
                } else
                    components.add(Component.translatable("tooltip.exposure.hold_for_details"));
                return;
            }

            boolean rClickAttachments = Config.Server.CAMERA_GUI_RIGHT_CLICK_OPEN_ATTACHMENTS.get();
            boolean rClickHotswap = Config.Server.CAMERA_GUI_RIGHT_CLICK_HOTSWAP.get();

            if (rClickAttachments || rClickHotswap) {
                if (Screen.hasShiftDown()) {
                    if (rClickAttachments)
                        components.add(Component.translatable("item.exposure.camera.tooltip.details_attachments_screen"));
                    if (rClickHotswap)
                        components.add(Component.translatable("item.exposure.camera.tooltip.details_hotswap"));
                } else
                    components.add(Component.translatable("tooltip.exposure.hold_for_details"));
            }
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY) return false;

        if (getShutter().isOpen(stack)) {
            player.playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 0.9f, 1f);
            player.displayClientMessage(Component.translatable("item.exposure.camera.camera_attachments.fail.shutter_open")
                    .withStyle(ChatFormatting.RED), true);
            return true;
        }

        if (otherStack.isEmpty() && Config.Server.CAMERA_GUI_RIGHT_CLICK_OPEN_ATTACHMENTS.get()) {
            if (!(slot.container instanceof Inventory)) {
                return false; // Cannot open when not in player's inventory
            }

            if (player.isCreative() && player.level().isClientSide()) {
                Packets.sendToServer(new OpenCameraAttachmentsInCreativePacketC2SP(slot.getContainerSlot()));
                return true;
            }

            openCameraAttachments(player, slot.getContainerSlot(), true);
            return true;
        }

        if (Config.Server.CAMERA_GUI_RIGHT_CLICK_HOTSWAP.get()) {
            if (hotswap(player, stack, otherStack, access) != InteractionResult.PASS) {
                return true;
            }
        }

        return false;
    }

    public InteractionResult handleStandSneakInteraction(CameraStandEntity stand, Player player, InteractionHand hand, ItemStack cameraStack) {
        ItemStack itemInHand = player.getItemInHand(hand);
        int slot = hand == InteractionHand.OFF_HAND ? Inventory.SLOT_OFFHAND : player.getInventory().selected;
        SlotAccess access = SlotAccess.forContainer(player.getInventory(), slot);
        return hotswap(stand, cameraStack, itemInHand, access);
    }

    protected InteractionResult hotswap(CameraHolder holder, ItemStack stack, ItemStack otherStack, SlotAccess access) {
        for (Attachment<?> attachment : getAttachments()) {
            StoredItemStack storedStack = attachment.get(stack);
            int maxCount = attachment.maxCount().get();

            // Remove
            if (otherStack.isEmpty()) {
                if (storedStack.isEmpty()) return InteractionResult.FAIL;

                access.set(storedStack.getCopy());
                attachment.set(stack, ItemStack.EMPTY);
                attachment.playRemoveSoundSided(holder.asHolderEntity());
                return InteractionResult.SUCCESS;
            }

            if (attachment.matches(otherStack)) {
                // Insertion
                if (storedStack.isEmpty() || ItemStack.isSameItemSameTags(storedStack.getForReading(), otherStack)) {
                    int availableCount = Math.max(0, maxCount - storedStack.getForReading().getCount());
                    if (availableCount == 0) {
                        holder.asHolderEntity().playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 0.9f, 1f);
                        return InteractionResult.FAIL; // No space
                    }

                    ItemStack insertedStack = otherStack.split(availableCount);
                    insertedStack.setCount(insertedStack.getCount() + storedStack.getForReading().getCount());
                    attachment.set(stack, insertedStack);
                    access.set(otherStack);
                    attachment.playInsertSoundSided(holder.asHolderEntity());
                    return InteractionResult.SUCCESS;
                }

                // Swap

                if (otherStack.getCount() > maxCount) {
                    holder.asHolderEntity().playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 0.9f, 1f);
                    return InteractionResult.FAIL; // Cannot swap when holding more than can be inserted
                }

                ItemStack returnedStack = storedStack.getCopy();
                attachment.set(stack, otherStack);
                access.set(returnedStack);
                attachment.playInsertSoundSided(holder.asHolderEntity());
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    public InteractionResultHolder<ItemStack> openCameraAttachments(@NotNull Player player, ItemStack stack, boolean openedFromGUI) {
        Preconditions.checkArgument(stack.getItem() instanceof CameraItem, "%s is not a CameraItem.", stack);

        int cameraSlot = getMatchingSlotInInventory(player.getInventory(), stack);
        if (cameraSlot < 0) {
            Exposure.LOGGER.error("Cannot open camera attachments: slot index is not found for item '{}'.", stack);
            return InteractionResultHolder.fail(stack);
        }

        return openCameraAttachments(player, cameraSlot, openedFromGUI);
    }

    public InteractionResultHolder<ItemStack> openCameraAttachments(@NotNull Player player, int slotIndex, boolean openedFromGUI) {
        Preconditions.checkArgument(slotIndex >= 0,
                "slotIndex '%s' is invalid. Should be larger than 0", slotIndex);
        ItemStack stack = player.getInventory().getItem(slotIndex);
        Preconditions.checkArgument(stack.getItem() instanceof CameraItem,
                "Item in slotIndex '%s' is not a CameraItem but '%s'.", slotIndex, stack);

        if (getShutter().isOpen(stack)) {
            player.displayClientMessage(Component.translatable("item.exposure.camera.camera_attachments.fail.shutter_open")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }

        getOrCreateId(stack);

        if (player instanceof ServerPlayer serverPlayer) {
            getTimer().stop(stack);

            MenuProvider menuProvider = new MenuProvider() {
                @Override
                public @NotNull Component getDisplayName() {
                    return stack.hasCustomHoverName()
                            ? stack.getHoverName() : Component.translatable("container.exposure.camera");
                }

                @Override
                public @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
                    return new CameraInHandAttachmentsMenu(containerId, playerInventory, slotIndex, openedFromGUI);
                }
            };

            PlatformHelper.openMenu(serverPlayer, menuProvider, buffer -> {
                buffer.writeInt(slotIndex);
                buffer.writeBoolean(openedFromGUI);
            });
        }

        setDisassembled(stack, true);
        Sound.play(player, Exposure.SoundEvents.CAMERA_GENERIC_CLICK.get(), SoundSource.PLAYERS, 0.9f, 0.9f, 0.2f);

        return InteractionResultHolder.success(stack);
    }

    // --

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!(entity instanceof CameraHolder holder)) return;

        tick(holder, stack);

        if (level.isClientSide && entity instanceof Player player) {
            boolean matchesActive = player.getActiveExposureCameraOptional()
                    .map(camera -> camera.idMatches(getOrCreateId(stack)))
                    .orElse(false);
            if (isActive(stack) && !matchesActive) {
                setActive(stack, false);
            }
        }
    }

    public boolean tick(CameraHolder holder, ItemStack stack) {
        Level level = holder.asHolderEntity().level();
        if (!(level instanceof ServerLevel serverLevel)) return false;

        boolean shutterStateChanged = getShutter().tick(holder, serverLevel, stack);
        boolean timerChanged = getTimer().tick(holder, serverLevel, stack);

        if (Config.Server.TIMER_ATTRACTS_MOB_ATTENTION.get()
                && (getTimer().isTicking(holder, stack) || getTimer().getTicksSinceLastRelease(holder, stack) < 10)) {
            grabAttentionOfNearbyMobs(holder, stack);
        }

        boolean projectionChanged = CameraInstances.getOptional(stack).map(instance -> {
            CameraInstance.ProjectionState state = instance.getProjectionState(level);
            switch (state) {
                case SUCCESSFUL, FAILED, TIMED_OUT -> {
                    handleProjectionResult(serverLevel, holder, stack, state, instance.getProjectionError(level));
                    instance.stopWaitingForProjection();
                    return true;
                }
            }
            return false;
        }).orElse(false);

        if (ExposureServer.debugHighlightEntitiesInFrame && isActive(stack)) {
            testEntitiesInFrame(stack, level, holder);
        }

        return shutterStateChanged || timerChanged || projectionChanged;
    }

    protected void grabAttentionOfNearbyMobs(CameraHolder holder, ItemStack stack) {
        Entity holderEntity = holder.asHolderEntity();
        Vec3 pos = isInSelfieMode(stack)
                ? holderEntity.getEyePosition().add(holderEntity.getLookAngle().scale(Config.Server.SELFIE_CAMERA_DISTANCE.get()))
                : holderEntity.getEyePosition();

        holderEntity.level().getEntities(holderEntity, new AABB(holderEntity.blockPosition())
                        .inflate(Config.Server.TIMER_ATTENTION_RADIUS.get()))
                .stream()
                .filter(entity -> entity instanceof Mob)
                .map(entity -> ((Mob) entity))
                .filter(mob -> canGrabAttentionOf(holder, mob))
                .forEach(mob -> {
                    // Each entity has slightly different delay until looking
                    long startLookingTick = getTimer().getStartTick(stack) + (mob.getId() % 15);
                    if (mob.level().getGameTime() > startLookingTick) {
                        mob.lookAt(EntityAnchorArgument.Anchor.EYES, pos);
                    }
                });
    }

    protected boolean canGrabAttentionOf(CameraHolder holder, Mob mob) {
        return mob.isAlive()
                && !mob.isDeadOrDying()
                && !mob.isSleeping()
                && !mob.getType().is(Exposure.Tags.Entities.IGNORES_CAMERA)
                && (mob.getTarget() == null || mob.getTarget().equals(holder))
                && !mob.hasEffect(MobEffects.BLINDNESS)
                && mob.hasLineOfSight(holder.asHolderEntity());
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (hand == InteractionHand.MAIN_HAND
                && player.getOffhandItem().getItem() instanceof CameraItem offhandCameraItem
                && offhandCameraItem.isActive(player.getOffhandItem())) {
            return InteractionResultHolder.pass(stack);
        }

        if (!isActive(stack)) {
            return player.isSecondaryUseActive()
                    ? openCameraAttachments(player, stack, false)
                    : activateInHand(player, stack, hand);
        }

        return release(player, stack);
    }

    public boolean canTakePhoto(CameraHolder holder, ItemStack stack) {
        return !isOnCooldown(holder, stack)
                && !getTimer().isTicking(holder, stack)
                && !getShutter().isOpen(stack)
                && Attachment.FILM.map(stack, FilmRollItem::canAddFrame).orElse(false)
                && CameraInstances.canReleaseShutter(CameraId.ofStack(stack));
    }

    public boolean isOnCooldown(CameraHolder holder, ItemStack stack) {
        if (holder.asHolderEntity() instanceof Player player) {
            return player.getCooldowns().isOnCooldown(this);
        } else if (holder instanceof CameraStandEntity stand) {
            return stand.isOnCooldown();
        }
        return false;
    }

    public float getCooldownPercent(CameraHolder holder, ItemStack stack) {
        if (holder.asHolderEntity() instanceof Player player) {
            return player.getCooldowns().isOnCooldown(stack.getItem())
                    ? player.getCooldowns().getCooldownPercent(stack.getItem(), 0)
                    : 0;
        } else if (holder instanceof CameraStandEntity stand) {
            return stand.isOnCooldown()
                    ? stand.getCooldownPercent()
                    : 0;
        }
        return 0;
    }

    public @NotNull InteractionResultHolder<ItemStack> release(CameraHolder holder, ItemStack stack) {
        Entity entity = holder.asHolderEntity();
        Level level = entity.level();

        Sound.playSided(entity, getReleaseButtonSound(), entity.getSoundSource(), 0.3f, 1f, 0.1f);

        if (level.isClientSide || !canTakePhoto(holder, stack)) {
            return InteractionResultHolder.consume(stack);
        }

        if (getTimer().getEndTick(stack) != level.getGameTime()) {
            SelfTimer selfTimer = CameraSettings.SELF_TIMER.getOrDefault(stack);
            if (selfTimer != SelfTimer.OFF) {
                getTimer().set(holder, stack, selfTimer.getTicks());
                return InteractionResultHolder.consume(stack);
            }
        }

        holder.getServerPlayerExecutingExposure().ifPresentOrElse(
                player -> takePhoto(holder, player, stack),
                () -> Exposure.LOGGER.error("Cannot start capture: photographer '{}' does not have valid executing player.", holder));

        return InteractionResultHolder.consume(stack);
    }

    protected void takePhoto(CameraHolder holder, ServerPlayer executingPlayer, ItemStack stack) {
        ServerLevel level = executingPlayer.serverLevel();
        Entity entity = holder.asHolderEntity();

        ShutterSpeed shutterSpeed = CameraSettings.SHUTTER_SPEED.getOrDefault(stack);

        getShutter().open(holder, level, stack, shutterSpeed);

        CameraId cameraId = getOrCreateId(stack);
        String exposureId = ExposureIdentifier.createId(executingPlayer);
        int lightLevel = LevelUtil.getLightLevelAt(level, entity.blockPosition());
        boolean flash = getFlash().isAvailable(stack)
                && getFlash().shouldFire(stack, lightLevel)
                && getFlash().fire(holder, level, stack);

        CaptureParameters captureParameters = new CaptureParameters.Builder(exposureId)
                .setCameraID(cameraId)
                .setCameraHolder(holder)
                .setFov(getFov(level, stack))
                .setCropFactor(getCropFactor())
                .setFilter(getFilterShaderLocation(level.registryAccess(), stack).orElse(null))
                .setProjection(getProjection(stack))
                .setChromaticChannel(getChromaticChannel(stack))
                .setFilmProperties(getFilmProperties(stack))
                .extraData(CaptureParameters.SHUTTER_SPEED, CameraSettings.SHUTTER_SPEED.getOrDefault(stack))
                .extraData(CaptureParameters.FLASH, flash)
                .extraData(CaptureParameters.LIGHT_LEVEL, lightLevel)
                .build();

        if (shutterSpeed.shouldCauseTickingSound() || captureParameters.projection().isPresent()) {
            int duration = Math.max(shutterSpeed.getDurationTicks(), captureParameters.projection()
                    .map(l -> Config.Server.PROJECT_TIMEOUT_TICKS.get()).orElse(0));
            Sound.playShutterTicking(entity, cameraId, duration);
        }

        CameraInstances.createOrUpdate(cameraId, instance -> {
            int cooldown = calculateCooldownAfterShot(stack, captureParameters);
            instance.setDeferredCooldown(cooldown);

            captureParameters.projection().ifPresent(fileLoading -> {
                instance.waitForProjection(level.getGameTime() + Config.Server.PROJECT_TIMEOUT_TICKS.get());
            });
        });

        addNewFrame(level, holder, stack, captureParameters);

        ExposureServer.exposureRepository().expect(executingPlayer, exposureId);
        Packets.sendToClient(new CaptureStartS2CP(getCaptureType(stack), captureParameters), executingPlayer);
    }

    protected void onShutterOpen(CameraHolder holder, ServerLevel serverLevel, ItemStack stack) {
        holder.getExposureCameraOperator().ifPresent(operator -> {
            if (operator instanceof ServerPlayer player) {
                Packets.sendToClient(ActionS2CP.SHUTTER_OPENED, player);
            }
        });
    }

    protected void onShutterClosed(CameraHolder holder, ServerLevel serverLevel, ItemStack stack) {
        if (holder instanceof Player player) {
            int cooldown = CameraInstances.getOptional(stack).map(CameraInstance::getDeferredCooldown).orElse(BASE_COOLDOWN);
            player.getCooldowns().addCooldown(this, cooldown);
        } else if (holder instanceof CameraStandEntity stand) {
            int cooldown = CameraInstances.getOptional(stack).map(CameraInstance::getDeferredCooldown).orElse(BASE_COOLDOWN);
            stand.startCooldown(cooldown);
        }

        Attachment.FILM.ifPresent(stack, (filmItem, filmStack) -> {
            SoundEvent sound = filmItem.isFull(filmStack)
                    ? Exposure.SoundEvents.FILM_ADVANCE_LAST.get()
                    : Exposure.SoundEvents.FILM_ADVANCE.get();

            float fullness = filmItem.getFullness(filmStack);
            String id = holder.asHolderEntity().getId() + getOrCreateId(stack).uuid().toString();
            Sound.playUnique(id, holder.asHolderEntity(), sound, SoundSource.PLAYERS, 1f, 0.85f + 0.2f * fullness);
        });
    }


    // --

    public void addNewFrame(ServerLevel level, CameraHolder holder, ItemStack stack, CaptureParameters captureParameters) {
        boolean projecting = captureParameters.projection().isPresent();

        PointOfView pov = getPointOfView(holder, stack);
        double fov = getViewfinderFov(level, stack);

        List<BlockPos> positionsInFrame = !projecting ? getPositionsInFrame(holder, pov, fov) : Collections.emptyList();
        List<LivingEntity> entitiesInFrame = !projecting ? EntitiesInFrame.get(holder, pov, fov) : Collections.emptyList();

        Frame frame = createFrame(holder, level, stack, captureParameters, positionsInFrame, entitiesInFrame);
        addFrameToFilm(stack, frame);
        onFrameAdded(holder, level, stack, frame, positionsInFrame, entitiesInFrame);
    }

    public Frame createFrame(CameraHolder holder, ServerLevel level, ItemStack stack, CaptureParameters captureParameters,
                             List<BlockPos> positionsInFrame, List<LivingEntity> entitiesInFrame) {
        return Frame.create()
                .setIdentifier(ExposureIdentifier.id(captureParameters.exposureId()))
                .setType(captureParameters.filmProperties().type())
                .setPhotographer(new Photographer(holder))
                .setEntitiesInFrame(entitiesInFrame.stream()
                        .limit(Exposure.MAX_ENTITIES_IN_FRAME)
                        .map(entity -> EntityInFrame.of(holder.asHolderEntity(), entity, data -> {
                            PlatformHelper.postModifyEntityInFrameExtraDataEvent(holder, stack, entity, data);
                        }))
                        .toList())
                .addExtraData(Frame.SHUTTER_SPEED, CameraSettings.SHUTTER_SPEED.getOrDefault(stack))
                .addExtraData(Frame.TIMESTAMP, UnixTimestamp.Seconds.now())
                .updateExtraData(data -> addFrameExtraData(holder, level, stack, captureParameters, positionsInFrame, entitiesInFrame, data))
                .toImmutable();
    }

    protected void addFrameExtraData(CameraHolder holder, ServerLevel level, ItemStack camera, CaptureParameters params,
                                     List<BlockPos> positionsInFrame, List<LivingEntity> entitiesInFrame, CompoundTag data) {
        Entity cameraHolder = holder.asHolderEntity();
        boolean projecting = params.projection().isPresent();

        if (projecting) {
            NbtType.put(data,Frame.PROJECTED, true);
            return;
        }

        if (params.getFlash()) {
            NbtType.put(data,Frame.FLASH, true);
        }
        if (isInSelfieMode(camera)) {
            NbtType.put(data,Frame.SELFIE, true);
        }
        if (holder instanceof CameraStandEntity) {
            NbtType.put(data,Frame.ON_STAND, true);
        }

        double zoom = CameraSettings.ZOOM.getOrDefault(camera);
        FocalRange focalRange = getFocalRange(level.registryAccess(), camera);
        int focalLength = (int) focalRange.focalLengthFromZoom(zoom);
        NbtType.put(data,Frame.FOCAL_LENGTH, focalLength);

        params.extraData().get(CaptureParameters.LIGHT_LEVEL)
                .ifPresent(lightLevel -> NbtType.put(data,Frame.LIGHT_LEVEL, lightLevel));

        if (params.filmProperties().type() == ExposureType.BLACK_AND_WHITE) {
            params.singleChannel().ifPresent(channel ->
                    NbtType.put(data,Frame.COLOR_CHANNEL, channel));
        }

        NbtType.put(data,Frame.POSITION, cameraHolder.position());
        NbtType.put(data,Frame.PITCH, cameraHolder.getXRot());
        NbtType.put(data,Frame.YAW, cameraHolder.getYRot());

        NbtType.put(data,Frame.DAY_TIME, (int) level.getDayTime());
        NbtType.put(data,Frame.DIMENSION, level.dimension().location());

        BlockPos blockPos = cameraHolder.blockPosition();

        int surfaceHeight = level.getHeight(Heightmap.Types.WORLD_SURFACE, cameraHolder.getBlockX(), cameraHolder.getBlockZ());
        level.updateSkyBrightness();
        int skyLight = level.getBrightness(LightLayer.SKY, blockPos);

        if (cameraHolder.isUnderWater()) {
            NbtType.put(data,Frame.UNDERWATER, true);
        }
        if (cameraHolder.getBlockY() < Math.min(level.getSeaLevel(), surfaceHeight) && skyLight == 0) {
            NbtType.put(data,Frame.IN_CAVE, true);
        } else if (!cameraHolder.isUnderWater()) {
            Biome.Precipitation precipitation = level.getBiome(blockPos).value().getPrecipitationAt(blockPos);
            if (level.isThundering() && precipitation != Biome.Precipitation.NONE)
                NbtType.put(data,Frame.WEATHER, precipitation == Biome.Precipitation.SNOW ? "Snowstorm" : "Thunder");
            else if (level.isRaining() && precipitation != Biome.Precipitation.NONE)
                NbtType.put(data,Frame.WEATHER, precipitation == Biome.Precipitation.SNOW ? "Snow" : "Rain");
            else
                NbtType.put(data,Frame.WEATHER, "Clear");
        }

        // Most common biome:
        positionsInFrame.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .flatMap(pos -> level.getBiome(pos).unwrapKey().map(ResourceKey::location))
                .ifPresent(biome -> NbtType.put(data,Frame.BIOME, biome));

        List<ResourceLocation> structures = positionsInFrame.stream()
                .map(pos -> LevelUtil.getStructuresAt(level, pos))
                .flatMap(List::stream)
                .collect(Collectors.toSet()) // Remove duplicates
                .stream()
                .toList();
        if (!structures.isEmpty()) {
            NbtType.put(data,Frame.STRUCTURES, structures);
        }

        PlatformHelper.postModifyFrameExtraDataEvent(holder, camera, params, positionsInFrame, entitiesInFrame, data);
    }

    /**
     * Fires 5 rays from camera and obtains positions where they landed. <br>
     * First ray is in the center (equals to look direction).<br>
     * Next are: top left, top right, bottom left, bottom right.
     * These 4 are roughly in positions where rule of thirds cross points are.
     */
    public List<BlockPos> getPositionsInFrame(CameraHolder cameraHolder, PointOfView pov, double fov) {
        // offset roughly corresponds to rule of thirds distance
        float offsetDegrees = (float) ((fov * getCropFactor()) / 4.3);

        return Vec3Util.getProbeVectors(pov.dir(), offsetDegrees).stream()
                .map(direction -> {
                    Vec3 endPos = pov.pos().add(direction.scale(100));
                    return cameraHolder.asHolderEntity().level().clip(
                            new ClipContext(pov.pos(), endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, cameraHolder.asHolderEntity()));
                })
                .filter(hit -> hit.getType() != HitResult.Type.MISS)
                .map(BlockHitResult::getBlockPos)
                .toList();
    }

    public void addFrameToFilm(ItemStack stack, Frame frame) {
        Attachment.FILM.ifPresentOrElse(stack, (filmItem, filmStack) -> {
            ItemStack updatedFilmStack = filmStack.copy();
            filmItem.addFrame(updatedFilmStack, frame);
            Attachment.FILM.set(stack, updatedFilmStack);
        }, () -> Exposure.LOGGER.error("Cannot add frame: no film attachment is present."));
    }

    public void onFrameAdded(CameraHolder holder, ServerLevel level, ItemStack stack, Frame frame,
                             List<BlockPos> positionsInFrame, List<LivingEntity> entitiesInFrame) {
        Entity executor = holder.getPlayerExecutingExposure().map(pl -> (Entity) pl).orElse(holder.asHolderEntity());
        ExposureServer.frameHistory().add(executor, frame);

        entitiesInFrame.forEach(entity -> entityCaptured(holder, stack, entity));

        holder.getPlayerAwardedForExposure()
                .filter(player -> player instanceof ServerPlayer)
                .ifPresent(player -> {
                    ServerPlayer serverPlayer = (ServerPlayer) player;
                    serverPlayer.awardStat(Exposure.Stats.FILM_FRAMES_EXPOSED);
                    Exposure.ExposureCriteriaTriggers.FRAME_EXPOSED.trigger(
                            serverPlayer, holder, stack, frame, positionsInFrame, entitiesInFrame);
                });

        PlatformHelper.postFrameAddedEvent(holder, stack, frame, positionsInFrame, entitiesInFrame);
    }

    protected void entityCaptured(CameraHolder cameraHolder, ItemStack stack, LivingEntity entity) {
        if (cameraHolder.asHolderEntity() instanceof ServerPlayer player && entity instanceof EnderMan enderMan) {
            boolean lookingAtAngryEnderMan = player.equals(enderMan.getTarget()) && enderMan.isLookingAtMe(player);

            if (lookingAtAngryEnderMan) {
                // I wanted to implement this in a predicate,
                // but it's tricky because EntitySubPredicates do not get the player in their 'match' method.
                // So it's just easier to hardcode it like this.
                Exposure.ExposureCriteriaTriggers.PHOTOGRAPH_ENDERMAN_EYES.trigger(player);
            }
        }
    }

    public static ItemStack transmuteCopy(ItemStack original,Item newItem) {
        ItemStack stack = new ItemStack(newItem,original.getCount());
        stack.setTag(original.getTag());
        return stack;
    }

    public void handleProjectionResult(ServerLevel level, CameraHolder holder, ItemStack stack,
                                       CameraInstance.ProjectionState projectionState, Optional<TranslatableError> error) {
        StoredItemStack filter = Attachment.FILTER.get(stack);
        if (filter.isEmpty()) return;
        if (!(filter.getItem() instanceof InterplanarProjectorItem interplanarProjector)) return;
        if (!interplanarProjector.isConsumable(filter.getForReading())) return;

        Entity entity = holder.asHolderEntity();

        if (projectionState == CameraInstance.ProjectionState.FAILED || projectionState == CameraInstance.ProjectionState.TIMED_OUT) {

            ItemStack filterStack = transmuteCopy(filter.getCopy(),Exposure.Items.BROKEN_INTERPLANAR_PROJECTOR.get());
            error.ifPresent(err -> Exposure.DataComponents.setInterplanarProjectorErrorCode(filterStack, err.code()));
            Attachment.FILTER.set(stack, filterStack);
            Sound.play(entity, Exposure.SoundEvents.BSOD.get());
            if (getShutter().isOpen(stack)) {
                getShutter().close(holder, level, stack);
            }
            return;
        }

        ItemStack filterStack = filter.getCopy();
        filterStack.shrink(1);
        Attachment.FILTER.set(stack, filterStack);

        if (projectionState == CameraInstance.ProjectionState.SUCCESSFUL) {
            RandomSource randomSource = level.random;
            holder.getServerPlayerAwardedForExposure()
                    .ifPresent(player -> Exposure.ExposureCriteriaTriggers.SUCCESSFULLY_PROJECT_IMAGE.trigger(player));
            Sound.play(entity, Exposure.SoundEvents.INTERPLANAR_PROJECT.get(), entity.getSoundSource(), 0.8f, 1.1f);
            for (int i = 0; i < 16; i++) {
                level.sendParticles(ParticleTypes.PORTAL, entity.getX(), entity.getY() + 1.2, entity.getZ(), 2,
                        randomSource.nextGaussian() * 0.3, randomSource.nextGaussian() * 0.3, randomSource.nextGaussian() * 0.3, 0.01);
            }
        }
    }

    // -- Util

    protected int getMatchingSlotInInventory(Inventory inventory, ItemStack stack) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).equals(stack)) {
                return i;
            }
        }
        return -1;
    }

    protected void testEntitiesInFrame(ItemStack stack, Level level, CameraHolder holder) {
        PointOfView pov = getPointOfView(holder, stack);

        double fov = getViewfinderFov(level, stack);
        List<LivingEntity> entities = EntitiesInFrame.get(holder.asHolderEntity(), pov, fov);
        for (LivingEntity livingEntity : entities) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 2, 1, true, false, false));
        }
    }

    protected void testPositionsInFrame(ItemStack stack, Level level, Player player) {
        if (level.isClientSide && level.getGameTime() % 2 == 0) {
            List<BlockPos> positionsInFrame = getPositionsInFrame(player, getPointOfView(player, stack), getViewfinderFov(level, stack));
            for (BlockPos pos : positionsInFrame) {
                level.addAlwaysVisibleParticle(ParticleTypes.EXPLOSION, true, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0, 0);
            }
        }
    }

    // --

    public static int getGlassTintColor(ItemStack stack, int tintIndex) {
        if (tintIndex == 1) {
            boolean shutterOpen = stack.getItem() instanceof CameraItem cameraItem && cameraItem.getShutter().isOpen(stack);

            StoredItemStack filter = Attachment.FILTER.get(stack);
            if (filter.isEmpty()) return shutterOpen ? 0xFF333333 : -1;
            if (filter.getForReading().getItem() instanceof BlockItem item && item.getBlock() instanceof StainedGlassPaneBlock pane) {
                return shutterOpen
                        ? Color.argb(pane.getColor().getTextColor()).multiply(0.2f).withAlpha(255).getARGB()
                        : pane.getColor().getTextColor();
            }
            if (filter.getForReading().is(Exposure.Items.INTERPLANAR_PROJECTOR.get()))
                return shutterOpen ? 0xFF051A0F : 0xFF50B27E;
            if (filter.getForReading().is(Exposure.Items.BROKEN_INTERPLANAR_PROJECTOR.get()))
                return shutterOpen ? 0xFF003D76 : 0xFF54ADFF;
            return -1;
        }

        return -1;
    }
}