package io.github.mortuusars.exposure.item;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.PlatformHelper;
import io.github.mortuusars.exposure.block.FlashBlock;
import io.github.mortuusars.exposure.camera.capture.Capture;
import io.github.mortuusars.exposure.camera.capture.CaptureManager;
import io.github.mortuusars.exposure.camera.capture.component.*;
import io.github.mortuusars.exposure.camera.capture.converter.DitheringColorConverter;
import io.github.mortuusars.exposure.camera.infrastructure.*;
import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderClient;
import io.github.mortuusars.exposure.menu.CameraAttachmentsMenu;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.client.StartExposureS2CP;
import io.github.mortuusars.exposure.network.packet.server.CameraInHandAddFrameC2SP;
import io.github.mortuusars.exposure.sound.OnePerPlayerSounds;
import io.github.mortuusars.exposure.util.CameraInHand;
import io.github.mortuusars.exposure.util.ItemAndStack;
import io.github.mortuusars.exposure.util.LevelUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class CameraItem extends Item {
    public record AttachmentType(String id, int slot, Predicate<ItemStack> stackValidator) {
        @Override
        public String toString() {
            return "AttachmentType{" +
                    "id='" + id + '\'' +
                    ", slot=" + slot +
                    '}';
        }
    }

    public static final AttachmentType FILM_ATTACHMENT = new AttachmentType("Film", 0, stack -> stack.getItem() instanceof FilmRollItem);
    public static final AttachmentType FLASH_ATTACHMENT = new AttachmentType("Flash", 1, stack -> stack.is(Exposure.Tags.Items.FLASHES));
    public static final AttachmentType LENS_ATTACHMENT = new AttachmentType("Lens", 2, stack -> stack.is(Exposure.Tags.Items.LENSES));
    public static final AttachmentType FILTER_ATTACHMENT = new AttachmentType("Filter", 3, stack -> stack.is(Exposure.Tags.Items.FILTERS));
    public static final List<AttachmentType> ATTACHMENTS = List.of(
            FILM_ATTACHMENT,
            FLASH_ATTACHMENT,
            LENS_ATTACHMENT,
            FILTER_ATTACHMENT);

    public static final List<ShutterSpeed> SHUTTER_SPEEDS = List.of(
            new ShutterSpeed("15\""),
            new ShutterSpeed("8\""),
            new ShutterSpeed("4\""),
            new ShutterSpeed("2\""),
            new ShutterSpeed("1\""),
            new ShutterSpeed("2"),
            new ShutterSpeed("4"),
            new ShutterSpeed("8"),
            new ShutterSpeed("15"),
            new ShutterSpeed("30"),
            new ShutterSpeed("60"),
            new ShutterSpeed("125"),
            new ShutterSpeed("250"),
            new ShutterSpeed("500")
    );

    public CameraItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 1000;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> components, @NotNull TooltipFlag isAdvanced) {
        if (Config.Client.CAMERA_SHOW_OPEN_WITH_SNEAK_IN_TOOLTIP.get()) {
            components.add(Component.translatable("item.exposure.camera.sneak_to_open_tooltip").withStyle(ChatFormatting.GRAY));
        }
    }

    public boolean isActive(ItemStack stack) {
        return stack.getTag() != null && stack.getTag().getBoolean("Active");
    }

    public void setActive(ItemStack stack, boolean active) {
        stack.getOrCreateTag().putBoolean("Active", active);
        setSelfieMode(stack, false);
    }

    public void activate(Player player, ItemStack stack) {
        if (!isActive(stack)) {
            setActive(stack, true);
            player.gameEvent(GameEvent.EQUIP); // Sends skulk vibrations
            playCameraSound(player, Exposure.SoundEvents.VIEWFINDER_OPEN.get(), 0.35f, 0.9f, 0.2f);
        }
    }

    public void deactivate(Player player, ItemStack stack) {
        if (isActive(stack)) {
            setActive(stack, false);
            player.gameEvent(GameEvent.EQUIP);
            playCameraSound(player, Exposure.SoundEvents.VIEWFINDER_CLOSE.get(), 0.35f, 0.9f, 0.2f);
        }
    }

    public boolean isInSelfieMode(ItemStack stack) {
        return stack.getTag() != null && stack.getTag().getBoolean("Selfie");
    }

    public void setSelfieMode(ItemStack stack, boolean selfie) {
        stack.getOrCreateTag().putBoolean("Selfie", selfie);
    }

    public void setSelfieModeWithEffects(Player player, ItemStack stack, boolean selfie) {
        setSelfieMode(stack, selfie);
        player.level().playSound(player, player, Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(),  SoundSource.PLAYERS, 1f, 1.5f);
    }

    public boolean isShutterOpen(ItemStack stack) {
        return stack.getTag() != null && stack.getTag().getBoolean("ShutterOpen");
    }

    public void setShutterOpen(Level level, ItemStack stack, ShutterSpeed shutterSpeed, boolean exposingFrame, boolean flashHasFired) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean("ShutterOpen", true);
        tag.putInt("ShutterTicks", Math.max(shutterSpeed.getTicks(), 1));
        tag.putLong("ShutterCloseTimestamp", level.getGameTime() + Math.max(shutterSpeed.getTicks(), 1));
        if (exposingFrame)
            tag.putBoolean("ExposingFrame", true);
        if (flashHasFired)
            tag.putBoolean("FlashHasFired", true);
    }

    public void setShutterClosed(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove("ShutterOpen");
            tag.remove("ShutterTicks");
            tag.remove("ShutterCloseTimestamp");
            tag.remove("ExposingFrame");
            tag.remove("FlashHasFired");
        }
    }

    public void openShutter(Player player, ItemStack stack, ShutterSpeed shutterSpeed, boolean exposingFrame, boolean flashHasFired) {
        setShutterOpen(player.level(), stack, shutterSpeed, exposingFrame, flashHasFired);

        player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        playCameraSound(player, Exposure.SoundEvents.SHUTTER_OPEN.get(), exposingFrame ? 0.7f : 0.5f,
                exposingFrame ? 1.1f : 1.25f, 0.2f);
        if (shutterSpeed.getMilliseconds() > 500) // More than 1/2
            OnePerPlayerSounds.play(player, Exposure.SoundEvents.SHUTTER_TICKING.get(), SoundSource.PLAYERS, 1f, 1f);
    }

    public void closeShutter(Player player, ItemStack stack) {
        long closedAtTimestamp = stack.getTag() != null ? stack.getTag().getLong("ShutterCloseTimestamp") : -1;
        boolean exposingFrame = stack.getTag() != null && stack.getTag().getBoolean("ExposingFrame");
        boolean flashHasFired = stack.getTag() != null && stack.getTag().getBoolean("FlashHasFired");

        setShutterClosed(stack);

        if (player.level().getGameTime() - closedAtTimestamp < 50) { // Skip effects if shutter "was closed" long ago
            player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
            player.getCooldowns().addCooldown(this, flashHasFired ? 10 : 2);
            playCameraSound(player, Exposure.SoundEvents.SHUTTER_CLOSE.get(), 0.7f, 1.1f, 0.2f);
            if (exposingFrame) {
                ItemAndStack<FilmRollItem> film = getFilm(stack).orElseThrow();

                float fullness = (float) film.getItem().getExposedFramesCount(film.getStack()) / film.getItem().getMaxFrameCount(film.getStack());
                boolean lastFrame = fullness == 1f;

                if (lastFrame)
                    OnePerPlayerSounds.play(player, Exposure.SoundEvents.FILM_ADVANCE_LAST.get(), SoundSource.PLAYERS, 1f, 1f);
                else {
                    OnePerPlayerSounds.play(player, Exposure.SoundEvents.FILM_ADVANCE.get(), SoundSource.PLAYERS,
                            1f, 0.9f + 0.1f * fullness);
                }
            }
        }
    }

    @SuppressWarnings("unused")
    public void playCameraSound(Player player, SoundEvent sound, float volume, float pitch) {
        playCameraSound(player, sound, volume, pitch, 0f);
    }

    public void playCameraSound(Player player, SoundEvent sound, float volume, float pitch, float pitchVariety) {
        if (pitchVariety > 0f)
            pitch = pitch - (pitchVariety / 2f) + (player.getRandom().nextFloat() * pitchVariety);
        player.level().playSound(player, player, sound, SoundSource.PLAYERS, volume, pitch);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        if (!(entity instanceof Player player))
            return;

        if (isShutterOpen(stack)) {
            if (stack.getTag() != null && stack.getTag().contains("ShutterTicks")) {
                int ticks = stack.getTag().getInt("ShutterTicks");
                if (ticks <= 0)
                    closeShutter(player, stack);
                else {
                    ticks--;
                    stack.getTag().putInt("ShutterTicks", ticks);
                }
            }
            else {
                closeShutter(player, stack);
            }
        }

        boolean inOffhand = player.getOffhandItem().equals(stack);
        boolean inHand = isSelected || inOffhand;

        if (!inHand) {
            deactivate(player, stack);
        }
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null) {
            InteractionHand hand = context.getHand();
            if (hand == InteractionHand.MAIN_HAND && CameraInHand.getActiveHand(player) == InteractionHand.OFF_HAND)
                return InteractionResult.PASS;

            return useCamera(player, hand);
        }
        return InteractionResult.CONSUME; // To not play attack animation.
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND && CameraInHand.getActiveHand(player) == InteractionHand.OFF_HAND)
            return InteractionResultHolder.pass(player.getItemInHand(hand));

        useCamera(player, hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    public InteractionResult useCamera(Player player, InteractionHand hand) {
        if (player.getCooldowns().isOnCooldown(this))
            return InteractionResult.FAIL;

        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty() || stack.getItem() != this)
            return InteractionResult.PASS;

        boolean active = isActive(stack);

        if (!active && player.isSecondaryUseActive()) {
            if (isShutterOpen(stack)) {
                player.displayClientMessage(Component.translatable("item.exposure.camera.camera_attachments.fail.shutter_open")
                        .withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }

            openCameraAttachmentsMenu(player, hand);
            return InteractionResult.SUCCESS;
        }

        if (!active) {
            activate(player, stack);
            player.getCooldowns().addCooldown(this, 4);

            if (player.level().isClientSide) {
                // Release use key after activating. Otherwise, right click will be still held and camera will take a shot
                CameraItemClientExtensions.releaseUseButton();
            }

            return InteractionResult.CONSUME; // Consume to not play animation
        }

        playCameraSound(player, Exposure.SoundEvents.CAMERA_RELEASE_BUTTON_CLICK.get(), 0.3f, 1f, 0.1f);

        Optional<ItemAndStack<FilmRollItem>> filmAttachment = getFilm(stack);

        if (filmAttachment.isEmpty())
            return InteractionResult.FAIL;

        ItemAndStack<FilmRollItem> film = filmAttachment.get();
        boolean exposingFilm = film.getItem().canAddFrame(film.getStack());

        if (!exposingFilm)
            return InteractionResult.FAIL;

        if (isShutterOpen(stack))
            return InteractionResult.FAIL;

        int lightLevel = LevelUtil.getLightLevelAt(player.level(), player.blockPosition());

        boolean flashHasFired = shouldFlashFire(player, stack) && tryUseFlash(player, stack);

        ShutterSpeed shutterSpeed = getShutterSpeed(stack);

        openShutter(player, stack, shutterSpeed, true, flashHasFired);

        player.awardStat(Exposure.Stats.FILM_FRAMES_EXPOSED);

        if (player instanceof ServerPlayer serverPlayer) {
            Packets.sendToClient(new StartExposureS2CP(createExposureId(player), hand, flashHasFired, lightLevel), serverPlayer);
        }

        return InteractionResult.CONSUME; // Consume to not play animation
    }

    public void exposeFrameClientside(Player player, InteractionHand hand, String exposureId, boolean flashHasFired, int lightLevel) {
        Preconditions.checkState(player.level().isClientSide, "Should only be called on client.");

        ItemStack cameraStack = player.getItemInHand(hand);

        CompoundTag frame = createFrameTag(player, cameraStack, exposureId, flashHasFired, lightLevel);

        Capture capture = createCapture(player, cameraStack, exposureId, frame, flashHasFired);
        CaptureManager.enqueue(capture);

        addFrameToFilm(cameraStack, frame);

        Packets.sendToServer(new CameraInHandAddFrameC2SP(hand, frame));
    }

    public void addFrameToFilm(ItemStack cameraStack, CompoundTag frame) {
        ItemAndStack<FilmRollItem> film = getFilm(cameraStack)
                .orElseThrow(() -> new IllegalStateException("Camera should have film inserted."));

        film.getItem().addFrame(film.getStack(), frame);
        setFilm(cameraStack, film.getStack());
    }

    protected boolean shouldFlashFire(Player player, ItemStack cameraStack) {
        if (getAttachment(cameraStack, FLASH_ATTACHMENT).isEmpty())
            return false;

        return switch (getFlashMode(cameraStack)) {
            case OFF -> false;
            case ON -> true;
            case AUTO -> LevelUtil.getLightLevelAt(player.level(), player.blockPosition()) < 8;
        };
    }

    @SuppressWarnings("unused")
    public boolean tryUseFlash(Player player, ItemStack cameraStack) {
        Level level = player.level();
        BlockPos playerHeadPos = player.blockPosition().above();
        @Nullable BlockPos flashPos = null;

        if (level.getBlockState(playerHeadPos).isAir() || level.getFluidState(playerHeadPos).isSourceOfType(Fluids.WATER))
            flashPos = playerHeadPos;
        else {
            for (Direction direction : Direction.values()) {
                BlockPos pos = playerHeadPos.relative(direction);
                if (level.getBlockState(pos).isAir() || level.getFluidState(pos).isSourceOfType(Fluids.WATER)) {
                    flashPos = pos;
                }
            }
        }

        if (flashPos == null)
            return false;

        level.setBlock(flashPos, Exposure.Blocks.FLASH.get().defaultBlockState()
                .setValue(FlashBlock.WATERLOGGED, level.getFluidState(flashPos)
                        .isSourceOfType(Fluids.WATER)), Block.UPDATE_ALL_IMMEDIATE);
        level.playSound(player, player, Exposure.SoundEvents.FLASH.get(), SoundSource.PLAYERS, 1f, 1f);

        player.gameEvent(GameEvent.PRIME_FUSE);
        player.awardStat(Exposure.Stats.FLASHES_TRIGGERED);

        // Send particles to other players:
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            Vec3 pos = player.position();
            pos = pos.add(0, 1, 0).add(player.getLookAngle().multiply(0.5, 0, 0.5));
            ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(ParticleTypes.FLASH, false,
                    pos.x, pos.y, pos.z, 0, 0, 0, 0, 0);
            for (ServerPlayer pl : serverLevel.players()) {
                if (!pl.equals(serverPlayer)) {
                    pl.connection.send(packet);
                    RandomSource r = serverLevel.getRandom();
                    for (int i = 0; i < 4; i++) {
                        pl.connection.send(new ClientboundLevelParticlesPacket(ParticleTypes.END_ROD, false,
                                pos.x + r.nextFloat() * 0.5f - 0.25f, pos.y + r.nextFloat() * 0.5f + 0.2f, pos.z + r.nextFloat() * 0.5f - 0.25f,
                                0, 0, 0, 0, 0));
                    }
                }
            }
        }
        return true;
    }

    protected CompoundTag createFrameTag(Player player, ItemStack cameraStack, String exposureId, boolean flash, int lightLevel) {
        Level level = player.level();

        CompoundTag tag = new CompoundTag();

        tag.putString(FrameData.ID, exposureId);
        tag.putString(FrameData.TIMESTAMP, Util.getFilenameFormattedDateTime());
        tag.putString(FrameData.PHOTOGRAPHER, player.getScoreboardName());
        tag.putUUID(FrameData.PHOTOGRAPHER_ID, player.getUUID());
        if (flash)
            tag.putBoolean(FrameData.FLASH, true);
        if (isInSelfieMode(cameraStack))
            tag.putBoolean(FrameData.SELFIE, true);

        ListTag pos = new ListTag();
        pos.add(IntTag.valueOf(player.blockPosition().getX()));
        pos.add(IntTag.valueOf(player.blockPosition().getY()));
        pos.add(IntTag.valueOf(player.blockPosition().getZ()));
        tag.put(FrameData.POSITION, pos);

        tag.putString(FrameData.DIMENSION, player.level().dimension().location().toString());

        player.level().getBiome(player.blockPosition()).unwrapKey().map(ResourceKey::location)
                .ifPresent(biome -> tag.putString(FrameData.BIOME, biome.toString()));

        int surfaceHeight = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, player.getBlockX(), player.getBlockZ());
        level.updateSkyBrightness();
        int skyLight = level.getBrightness(LightLayer.SKY, player.blockPosition());

        if (player.isUnderWater())
            tag.putBoolean(FrameData.UNDERWATER, true);

        if (player.getBlockY() < surfaceHeight && skyLight < 4)
            tag.putBoolean(FrameData.IN_CAVE, true);
        else if (!player.isUnderWater()){
            Biome.Precipitation precipitation = level.getBiome(player.blockPosition()).value().getPrecipitationAt(player.blockPosition());
            if (level.isThundering() && precipitation != Biome.Precipitation.NONE)
                tag.putString(FrameData.WEATHER, precipitation == Biome.Precipitation.SNOW ? "Snowstorm" : "Thunder");
            else if (level.isRaining() && precipitation != Biome.Precipitation.NONE)
                tag.putString(FrameData.WEATHER, precipitation == Biome.Precipitation.SNOW ? "Snow" : "Rain");
            else
                tag.putString(FrameData.WEATHER, "Clear");
        }

        tag.putInt(FrameData.LIGHT_LEVEL, lightLevel);
        tag.putFloat(FrameData.SUN_ANGLE, level.getSunAngle(0));

        List<Entity> entitiesInFrame = EntitiesInFrame.get(player, ViewfinderClient.getCurrentFov(), 12, isInSelfieMode(cameraStack));
        if (!entitiesInFrame.isEmpty()) {
            ListTag entities = new ListTag();

            for (Entity entity : entitiesInFrame) {
                CompoundTag entityInfoTag = createEntityInFrameInfo(entity, player, cameraStack);
                if (entityInfoTag.isEmpty())
                    continue;

                entities.add(entityInfoTag);

                // Duplicate entity id as a separate field in the tag.
                // Can then be used by FTBQuests nbt matching (it's hard to match from a list), for example.
                tag.putBoolean(entityInfoTag.getString(FrameData.ENTITY_ID), true);
            }

            if (!entities.isEmpty())
                tag.put(FrameData.ENTITIES_IN_FRAME, entities);
        }

        return tag;
    }

    protected CompoundTag createEntityInFrameInfo(Entity entity, Player photographer, ItemStack cameraStack) {
        CompoundTag tag = new CompoundTag();
        ResourceLocation entityRL = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());

        tag.putString(FrameData.ENTITY_ID, entityRL.toString());

        ListTag pos = new ListTag();
        pos.add(IntTag.valueOf((int) entity.getX()));
        pos.add(IntTag.valueOf((int) entity.getY()));
        pos.add(IntTag.valueOf((int) entity.getZ()));
        tag.put(FrameData.ENTITY_POSITION, pos);

        tag.putFloat(FrameData.ENTITY_DISTANCE, photographer.distanceTo(entity));

        if (entity instanceof Player player)
            tag.putString(FrameData.ENTITY_PLAYER_NAME, player.getScoreboardName());

        return tag;
    }

    protected void openCameraAttachmentsMenu(Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            ItemStack cameraStack = player.getItemInHand(hand);

            MenuProvider menuProvider = new MenuProvider() {
                @Override
                public @NotNull Component getDisplayName() {
                    return cameraStack.getHoverName();
                }

                @Override
                public @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
                    return new CameraAttachmentsMenu(containerId, playerInventory, cameraStack);
                }
            };

            PlatformHelper.openMenu(serverPlayer, menuProvider, buffer -> buffer.writeItem(cameraStack));
        }
    }

    protected String createExposureId(Player player) {
        // This method is called only server-side and then gets sent to client in a packet
        // because gameTime is different between client/server, and IDs won't match.
        return player.getName().getString() + "_" + player.level().getGameTime();
    }

    public FocalRange getFocalRange(ItemStack cameraStack) {
        return getAttachment(cameraStack, LENS_ATTACHMENT).map(FocalRange::fromStack).orElse(getDefaultFocalRange());
    }

    public FocalRange getDefaultFocalRange() {
        return FocalRange.getDefault();
    }

    @SuppressWarnings("unused")
    protected Capture createCapture(Player player, ItemStack cameraStack, String exposureId, CompoundTag frameData, boolean flash) {
        ItemAndStack<FilmRollItem> film = getFilm(cameraStack).orElseThrow();
        int frameSize = film.getItem().getFrameSize(film.getStack());
        float brightnessStops = getShutterSpeed(cameraStack).getStopsDifference(ShutterSpeed.DEFAULT);

        ArrayList<ICaptureComponent> components = new ArrayList<>();
        components.add(new BaseComponent());
        if (flash)
            components.add(new FlashComponent());
        if (brightnessStops != 0)
            components.add(new BrightnessComponent(brightnessStops));
        if (film.getItem().getType() == FilmType.BLACK_AND_WHITE)
            components.add(new BlackAndWhiteComponent());

        components.add(new ExposureStorageSaveComponent(exposureId, true));

        return new Capture(exposureId, frameData)
                .setFilmType(film.getItem().getType())
                .size(frameSize)
                .brightnessStops(brightnessStops)
                .components(components)
                .converter(new DitheringColorConverter());
    }

    /**
     * This method is called after we take a screenshot (or immediately if not capturing but should show effects). Otherwise, due to the delays (flash, etc) - particles would be captured as well.
     */
    @SuppressWarnings("unused")
    public void spawnClientsideFlashEffects(@NotNull Player player, ItemStack cameraStack) {
        Preconditions.checkState(player.level().isClientSide, "This methods should only be called client-side.");
        Level level = player.level();
        Vec3 pos = player.position();
        Vec3 lookAngle = player.getLookAngle();
        pos = pos.add(0, 1, 0).add(lookAngle.multiply(0.8f, 0.8f, 0.8f));

        RandomSource r = level.getRandom();
        for (int i = 0; i < 3; i++) {
            level.addParticle(ParticleTypes.END_ROD,
                    pos.x + r.nextFloat() - 0.5f,
                    pos.y + r.nextFloat() + 0.15f,
                    pos.z + r.nextFloat() - 0.5f,
                    lookAngle.x * 0.025f + r.nextFloat() * 0.025f,
                    lookAngle.y * 0.025f + r.nextFloat() * 0.025f,
                    lookAngle.z * 0.025f + r.nextFloat() * 0.025f);
        }
    }

    // ---

    @SuppressWarnings("unused")
    public List<AttachmentType> getAttachmentTypes(ItemStack cameraStack) {
        return ATTACHMENTS;
    }

    public Optional<AttachmentType> getAttachmentTypeForSlot(ItemStack cameraStack, int slot) {
        List<AttachmentType> attachmentTypes = getAttachmentTypes(cameraStack);
        for (AttachmentType attachmentType : attachmentTypes) {
            if (attachmentType.slot == slot)
                return Optional.of(attachmentType);
        }
        return Optional.empty();
    }

    public Optional<ItemAndStack<FilmRollItem>> getFilm(ItemStack cameraStack) {
        return getAttachment(cameraStack, FILM_ATTACHMENT).map(ItemAndStack::new);
    }

    public void setFilm(ItemStack cameraStack, ItemStack filmStack) {
        setAttachment(cameraStack, FILM_ATTACHMENT, filmStack);
    }

    public Optional<ItemStack> getAttachment(ItemStack cameraStack, AttachmentType attachmentType) {
        if (cameraStack.getTag() != null && cameraStack.getTag().contains(attachmentType.id, Tag.TAG_COMPOUND)) {
            ItemStack itemStack = ItemStack.of(cameraStack.getTag().getCompound(attachmentType.id));
            if (!itemStack.isEmpty())
                return Optional.of(itemStack);
        }
        return Optional.empty();
    }

    public void setAttachment(ItemStack cameraStack, AttachmentType attachmentType, ItemStack attachmentStack) {
        if (attachmentStack.isEmpty()) {
            if (cameraStack.getTag() != null)
                cameraStack.getOrCreateTag().remove(attachmentType.id);
        } else {
            Preconditions.checkState(attachmentType.stackValidator.test(attachmentStack),
                    attachmentStack + " is not valid for the '" + attachmentType + "' attachment type.");

            cameraStack.getOrCreateTag().put(attachmentType.id, attachmentStack.save(new CompoundTag()));
        }

        if (attachmentType == LENS_ATTACHMENT)
            setZoom(cameraStack, getFocalRange(cameraStack).min());
    }

    // ---

    /**
     * Returns all possible Shutter Speeds for this camera.
     */
    @SuppressWarnings("unused")
    public List<ShutterSpeed> getAllShutterSpeeds(ItemStack cameraStack) {
        return SHUTTER_SPEEDS;
    }

    public ShutterSpeed getShutterSpeed(ItemStack cameraStack) {
        return ShutterSpeed.loadOrDefault(cameraStack.getOrCreateTag());
    }

    public void setShutterSpeed(ItemStack cameraStack, ShutterSpeed shutterSpeed) {
        shutterSpeed.save(cameraStack.getOrCreateTag());
    }

    public float getFocalLength(ItemStack cameraStack) {
        return cameraStack.hasTag() ? cameraStack.getOrCreateTag().getFloat("Zoom") : getFocalRange(cameraStack).min();
    }

    public void setZoom(ItemStack cameraStack, double focalLength) {
        cameraStack.getOrCreateTag().putDouble("Zoom", focalLength);
    }

    public CompositionGuide getCompositionGuide(ItemStack cameraStack) {
        if (!cameraStack.hasTag() || !cameraStack.getOrCreateTag().contains("CompositionGuide", Tag.TAG_STRING))
            return CompositionGuides.NONE;

        return CompositionGuides.byIdOrNone(cameraStack.getOrCreateTag().getString("CompositionGuide"));
    }

    public void setCompositionGuide(ItemStack cameraStack, CompositionGuide guide) {
        cameraStack.getOrCreateTag().putString("CompositionGuide", guide.getId());
    }

    public FlashMode getFlashMode(ItemStack cameraStack) {
        if (!cameraStack.hasTag() || !cameraStack.getOrCreateTag().contains("FlashMode", Tag.TAG_STRING))
            return FlashMode.OFF;

        return FlashMode.byIdOrOff(cameraStack.getOrCreateTag().getString("FlashMode"));
    }

    public void setFlashMode(ItemStack cameraStack, FlashMode flashMode) {
        cameraStack.getOrCreateTag().putString("FlashMode", flashMode.getId());
    }
}
