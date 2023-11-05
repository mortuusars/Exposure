package io.github.mortuusars.exposure.item;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.block.FlashBlock;
import io.github.mortuusars.exposure.camera.capture.Capture;
import io.github.mortuusars.exposure.camera.capture.CaptureManager;
import io.github.mortuusars.exposure.camera.capture.component.*;
import io.github.mortuusars.exposure.camera.capture.converter.DitheringColorConverter;
import io.github.mortuusars.exposure.camera.infrastructure.*;
import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderClient;
import io.github.mortuusars.exposure.menu.CameraAttachmentsMenu;
import io.github.mortuusars.exposure.network.packet.CameraInHandAddFrameServerboundPacket;
import io.github.mortuusars.exposure.sound.OnePerPlayerSounds;
import io.github.mortuusars.exposure.util.*;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.util.Mth;
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
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CameraItem extends Item {
    public record AttachmentType(String id, int slot, Predicate<ItemStack> stackValidator) {
    }

    public static final AttachmentType FILM_ATTACHMENT = new AttachmentType("Film", 0, stack -> stack.getItem() instanceof FilmRollItem);
    public static final AttachmentType FLASH_ATTACHMENT = new AttachmentType("Flash", 1, stack -> stack.is(Items.REDSTONE_LAMP));
    public static final AttachmentType LENS_ATTACHMENT = new AttachmentType("Lens", 2, stack -> stack.getItem() instanceof SpyglassItem);
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
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.CUSTOM;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(CameraItemClient.INSTANCE);
    }

    public boolean isActive(ItemStack stack) {
        return stack.getTag() != null && stack.getTag().getBoolean("Active");
    }

    public void setActive(ItemStack stack, boolean active) {
        stack.getOrCreateTag().putBoolean("Active", active);
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

    public boolean isShutterOpen(ItemStack stack, Level level) {
        return stack.getTag() != null
                && stack.getTag().getBoolean("ShutterOpen");
    }

    public boolean shouldShutterClose(ItemStack stack, Level level) {
        return stack.getTag() != null
                && stack.getTag().getBoolean("ShutterOpen")
                && stack.getTag().getLong("ShutterCloseTimestamp") <= level.getGameTime();
    }

    public void setShutterOpen(Level level, ItemStack stack, ShutterSpeed shutterSpeed, boolean exposingFrame) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean("ShutterOpen", true);
        tag.putInt("ShutterTicks", Math.max(shutterSpeed.getTicks(), 1));
        tag.putLong("ShutterCloseTimestamp", level.getGameTime() + Math.max(shutterSpeed.getTicks(), 1));
        if (exposingFrame)
            tag.putBoolean("ExposingFrame", true);
    }

    public void setShutterClosed(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove("ShutterOpen");
            tag.remove("ShutterTicks");
            tag.remove("ShutterCloseTimestamp");
            tag.remove("ExposingFrame");
        }
    }

    public void openShutter(Player player, ItemStack stack, ShutterSpeed shutterSpeed, boolean exposingFrame) {
        setShutterOpen(player.getLevel(), stack, shutterSpeed, exposingFrame);

        player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        playCameraSound(player, Exposure.SoundEvents.SHUTTER_OPEN.get(), exposingFrame ? 0.85f : 0.65f,
                exposingFrame ? 1.1f : 1.25f, 0.2f);
        if (shutterSpeed.getMilliseconds() > 500) // More than 1/2
            OnePerPlayerSounds.play(player, Exposure.SoundEvents.SHUTTER_TICKING.get(), SoundSource.PLAYERS, 1f, 1f);
    }

    public void closeShutter(Player player, ItemStack stack) {
        long closedAtTimestamp = stack.getTag() != null ? stack.getTag().getLong("ShutterCloseTimestamp") : -1;

        setShutterClosed(stack);

        if (player.getLevel().getGameTime() - closedAtTimestamp < 40) { // Skip effects if shutter "was closed" long ago
            player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
            boolean exposingFrame = stack.getTag() != null && stack.getTag().getBoolean("ExposingFrame");
            player.getCooldowns().addCooldown(this, 2);
            playCameraSound(player, Exposure.SoundEvents.SHUTTER_CLOSE.get(), exposingFrame ? 0.85f : 0.65f,
                    exposingFrame ? 1.1f : 1.25f, 0.2f);
            if (exposingFrame) {
                OnePerPlayerSounds.play(player, Exposure.SoundEvents.FILM_ADVANCE.get(), SoundSource.PLAYERS,
                        1f, player.getLevel().getRandom().nextFloat() * 0.15f + 0.93f);
            }
        }
    }

    public void playCameraSound(Player player, SoundEvent sound, float volume, float pitch) {
        playCameraSound(player, sound, volume, pitch, 0f);
    }

    public void playCameraSound(Player player, SoundEvent sound, float volume, float pitch, float pitchVariety) {
        if (pitchVariety > 0f)
            pitch = pitch - (pitchVariety / 2f) + (player.getRandom().nextFloat() * pitchVariety);
        player.getLevel().playSound(player, player, sound, SoundSource.PLAYERS, volume, pitch);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        if (!(entity instanceof Player player))
            return;

        if (isShutterOpen(stack, level)) {
            if (stack.getTag() != null && stack.getTag().contains("ShutterTicks")) {
                int ticks = stack.getTag().getInt("ShutterTicks");
                if (ticks <= 0)
                    closeShutter(player, stack);
                else {
                    ticks--;
                    stack.getTag().putInt("ShutterTicks", ticks);
                }
            }
        }

//        if (isShutterOpen(stack, level) && shouldShutterClose(stack, player.getLevel()))
//            closeShutter(player, stack);

        boolean inOffhand = player.getOffhandItem().equals(stack);
        boolean inHand = isSelected || inOffhand;

        if (!inHand) {
            deactivate(player, stack);
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !oldStack.getItem().equals(newStack.getItem()) /*|| slotChanged*/;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
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


        InteractionResult interactionResult = useCamera(player, hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
//        return switch (interactionResult) {
//            case SUCCESS -> InteractionResultHolder.success(player.getItemInHand(hand));
//            case CONSUME, CONSUME_PARTIAL -> InteractionResultHolder.consume(player.getItemInHand(hand));
//            case PASS -> InteractionResultHolder.pass(player.getItemInHand(hand));
//            case FAIL -> InteractionResultHolder.fail(player.getItemInHand(hand));
//        };
    }

    public InteractionResult useCamera(Player player, InteractionHand hand) {
        Level level = player.getLevel();

        if (player.getCooldowns().isOnCooldown(this))
            return InteractionResult.FAIL;

        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty() || stack.getItem() != this)
            return InteractionResult.PASS;

        boolean active = isActive(stack);

        if (!active && player.isSecondaryUseActive()) {
            if (isShutterOpen(stack, level)) {
                player.displayClientMessage(Component.translatable("item.exposure.camera.camera_attachments.fail.shutter_open"), true);
                return InteractionResult.FAIL;
            }

            openCameraAttachmentsGUI(player, hand);
            return InteractionResult.SUCCESS;
        }

        if (!active) {
            activate(player, stack);
            player.getCooldowns().addCooldown(this, 4);
            return InteractionResult.SUCCESS; // Consume to not play animation
        }

        if (isShutterOpen(stack, level))
            return InteractionResult.FAIL;

        Optional<ItemAndStack<FilmRollItem>> filmOpt = getFilm(stack);
        boolean exposingFilm = filmOpt.map(f -> f.getItem().canAddFrame(f.getStack())).orElse(false);
        boolean flashHasFired = shouldFlashFire(player, stack) && tryUseFlash(player, stack);

        ShutterSpeed shutterSpeed = getShutterSpeed(stack);

        openShutter(player, stack, shutterSpeed, exposingFilm);

        if (player instanceof ServerPlayer serverPlayer)
            Exposure.Advancements.CAMERA_TAKEN_SHOT.trigger(serverPlayer, new ItemAndStack<>(stack), flashHasFired, exposingFilm);

        if (exposingFilm)
            player.awardStat(Exposure.Stats.FILM_FRAMES_EXPOSED);

        if (level.isClientSide) {
            if (exposingFilm) {
                String exposureId = createExposureId(player);
                Capture capture = createCapture(player, stack, exposureId, flashHasFired);
                CaptureManager.enqueue(capture);

                CompoundTag frame = createFrameTag(player, stack, exposureId, capture, flashHasFired);

                exposeFilmFrame(stack, frame);

                // Send to server:
                CameraInHandAddFrameServerboundPacket.send(hand, frame);
            } else if (flashHasFired) {
                spawnClientsideFlashEffects(player, stack);
            }
        }

        return InteractionResult.CONSUME; // Consume to not play animation
    }

    public void exposeFilmFrame(ItemStack cameraStack, CompoundTag frame) {
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
            case AUTO -> LevelUtil.getLightLevelAt(player.getLevel(), player.blockPosition()) < 8;
        };
    }

    public boolean tryUseFlash(Player player, ItemStack cameraStack) {
        Level level = player.getLevel();
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

    protected CompoundTag createFrameTag(Player player, ItemStack cameraStack, String exposureId, Capture capture, boolean flash) {
        Level level = player.getLevel();

        CompoundTag tag = new CompoundTag();

        tag.putString("Id", exposureId);
        if (flash)
            tag.putBoolean("Flash", true);
        tag.putString("Timestamp", Util.getFilenameFormattedDateTime());
        tag.putString("Photographer", player.getScoreboardName());
        tag.putUUID("PhotographerId", player.getUUID());

        ListTag pos = new ListTag();
        pos.add(IntTag.valueOf(player.blockPosition().getX()));
        pos.add(IntTag.valueOf(player.blockPosition().getY()));
        pos.add(IntTag.valueOf(player.blockPosition().getZ()));
        tag.put("Pos", pos);

        tag.putString("Dimension", player.level.dimension().location().toString());

        player.level.getBiome(player.blockPosition()).unwrapKey().map(ResourceKey::location)
                .ifPresent(biome -> tag.putString("Biome", biome.toString()));

        int surfaceHeight = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, player.getBlockX(), player.getBlockZ());
        int skyLight = level.getBrightness(LightLayer.SKY, player.blockPosition());

        if (player.isUnderWater())
            tag.putBoolean("Underwater", true);

        if (player.getBlockY() < surfaceHeight && skyLight < 4)
            tag.putBoolean("InCave", true);
        else if (!player.isUnderWater()){
            Biome.Precipitation precipitation = level.getBiome(player.blockPosition()).value().getPrecipitation();
            if (level.isThundering() && precipitation != Biome.Precipitation.NONE)
                tag.putString("Weather", precipitation == Biome.Precipitation.SNOW ? "Snowstorm" : "Thunder");
            else if (level.isRaining() && precipitation != Biome.Precipitation.NONE)
                tag.putString("Weather", precipitation == Biome.Precipitation.SNOW ? "Snow" : "Rain");
            else
                tag.putString("Weather", "Clear");
        }

        tag.putInt("LightLevel", LevelUtil.getLightLevelAt(level, player.blockPosition()));
        tag.putFloat("SunPosition", level.getSunAngle(0));

        List<Entity> entitiesInFrame = EntitiesInFrame.get(player, ViewfinderClient.getCurrentFov(), 12);
        if (entitiesInFrame.size() > 0) {
            ListTag entities = new ListTag();

            for (Entity entity : entitiesInFrame) {
                CompoundTag entityInfoTag = createEntityInFrameInfo(entity, player, cameraStack, capture);
                if (entityInfoTag.isEmpty())
                    continue;

                entities.add(entityInfoTag);

                // Duplicate entity id as a separate field in the tag.
                // Can then be used by FTBQuests nbt matching (it's hard to match from a list), for example.
                tag.putBoolean(entityInfoTag.getString("Id"), true);
            }

            if (entities.size() > 0)
                tag.put("Entities", entities);
        }

        return tag;
    }

    protected CompoundTag createEntityInFrameInfo(Entity entity, Player player, ItemStack cameraStack, Capture capture) {
        CompoundTag tag = new CompoundTag();
        ResourceLocation entityRL = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (entityRL == null)
            return new CompoundTag();
        tag.putString("Id", entityRL.toString());

        ListTag pos = new ListTag();
        pos.add(IntTag.valueOf((int) entity.getX()));
        pos.add(IntTag.valueOf((int) entity.getY()));
        pos.add(IntTag.valueOf((int) entity.getZ()));
        tag.put("Pos", pos);

        return tag;
    }

    protected void openCameraAttachmentsGUI(Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            ItemStack cameraStack = player.getItemInHand(hand);
            NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
                @Override
                public @NotNull Component getDisplayName() {
                    return cameraStack.getHoverName();
                }

                @Override
                public @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
                    return new CameraAttachmentsMenu(containerId, playerInventory, cameraStack);
                }
            }, buffer -> buffer.writeItem(cameraStack));
        }
    }

    protected String createExposureId(Player player) {
        // This method is called only client-side and then gets sent to server in a packet
        // because gameTime is different between client/server (by 1 tick, as I've seen), and IDs won't match.
        return player.getName().getString() + "_" + player.getLevel().getGameTime();
    }

    public FocalRange getFocalRange(ItemStack cameraStack) {
        return getAttachment(cameraStack, LENS_ATTACHMENT).isEmpty() ? new FocalRange(18, 55) : new FocalRange(55, 200);
    }

    protected Capture createCapture(Player player, ItemStack cameraStack, String exposureId, boolean flash) {
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

        return new Capture(exposureId)
                .setFilmType(film.getItem().getType())
                .size(frameSize)
                .brightnessStops(brightnessStops)
                .components(components)
                .converter(new DitheringColorConverter());
    }

    /**
     * This method is called after we take a screenshot (or immediately if not capturing but should show effects). Otherwise, due to the delays (flash, etc) - particles would be captured as well.
     */
    public void spawnClientsideFlashEffects(@NotNull Player player, ItemStack cameraStack) {
        Preconditions.checkState(player.getLevel().isClientSide, "This methods should only be called client-side.");
        Level level = player.getLevel();
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

        if (attachmentType == LENS_ATTACHMENT) {
            float prevZoom = getFocalLength(cameraStack);
            FocalRange prevFocalRange = getFocalRange(cameraStack);
            FocalRange newFocalRange = attachmentStack.isEmpty() ? FocalRange.SHORT : FocalRange.LONG;
            float adjustedZoom = Mth.map(prevZoom, prevFocalRange.min(), prevFocalRange.max(), newFocalRange.min(), newFocalRange.max());
            setZoom(cameraStack, adjustedZoom);
        }
    }

    // ---

    /**
     * Returns all possible Shutter Speeds for this camera.
     */
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
