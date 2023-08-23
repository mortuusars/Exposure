package io.github.mortuusars.exposure.item;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.block.FlashBlock;
import io.github.mortuusars.exposure.camera.CaptureProperties;
import io.github.mortuusars.exposure.camera.ExposedFrame;
import io.github.mortuusars.exposure.camera.ExposureCapture;
import io.github.mortuusars.exposure.camera.component.*;
import io.github.mortuusars.exposure.camera.film.FilmType;
import io.github.mortuusars.exposure.camera.infrastructure.EntitiesInFrame;
import io.github.mortuusars.exposure.camera.infrastructure.Shutter;
import io.github.mortuusars.exposure.camera.modifier.ExposureModifiers;
import io.github.mortuusars.exposure.camera.modifier.IExposureModifier;
import io.github.mortuusars.exposure.config.Config;
import io.github.mortuusars.exposure.menu.CameraAttachmentsMenu;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.SyncCameraServerboundPacket;
import io.github.mortuusars.exposure.storage.saver.ExposureFileSaver;
import io.github.mortuusars.exposure.storage.saver.ExposureStorageSaver;
import io.github.mortuusars.exposure.util.CameraInHand;
import io.github.mortuusars.exposure.util.ItemAndStack;
import io.github.mortuusars.exposure.util.OnePerPlayerSounds;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.Tags;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public class CameraItem extends Item {
    public record AttachmentType(String id, int slot, Predicate<ItemStack> stackValidator) {
    }

    public static final AttachmentType FILM_ATTACHMENT = new AttachmentType("Film", 0, stack -> stack.getItem() instanceof FilmRollItem);
    public static final AttachmentType FLASH_ATTACHMENT = new AttachmentType("Flash", 1, stack -> stack.is(Items.REDSTONE_LAMP));
    public static final AttachmentType LENS_ATTACHMENT = new AttachmentType("Lens", 2, stack -> stack.getItem() instanceof SpyglassItem);
    public static final AttachmentType FILTER_ATTACHMENT = new AttachmentType("Filter", 3, stack -> stack.is(Tags.Items.GLASS_PANES));
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

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (context.getPlayer() != null)
            useCamera(context.getPlayer(), context.getHand());
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        useCamera(player, usedHand);
        return super.use(level, player, usedHand);
    }

    public void useCamera(Player player, InteractionHand hand) {
        if (player.getCooldowns().isOnCooldown(this))
            return;

        if (!Exposure.getCamera().isActive(player)) {
            if (player.isSecondaryUseActive())
                openCameraAttachmentsGUI(player, hand);
            else
                Exposure.getCamera().activate(player, hand);

            player.getCooldowns().addCooldown(this, 4);
            return;
        }

        if (Exposure.getCamera().getShutter().isOpen(player))
            return;

        CameraInHand camera = Exposure.getCamera().getCameraInHand(player);
        Preconditions.checkState(!camera.isEmpty());

        boolean hasFilmFrame = getFilm(camera.getStack()).map(f -> f.getItem().canAddFrame(f.getStack())).orElse(false);

        boolean flash = shouldFlashFire(player, camera);
        boolean flashHasFired = flash && tryUseFlash(player, camera);

        Exposure.getCamera().getShutter()
                .open(player, camera.getCamera(), getShutterSpeed(camera.getStack()), hasFilmFrame);
        player.getCooldowns().addCooldown(this, flash ? 15 : 4);

        if (player.getLevel().isClientSide) {
            CaptureProperties captureProperties = createCaptureProperties(player, camera, flashHasFired);
            if (hasFilmFrame) {
                ExposureCapture.enqueueCapture(captureProperties);

                ExposedFrame exposureFrame = createExposureFrame(player, captureProperties, camera.getCamera(), EntitiesInFrame.get(player));

                ItemAndStack<FilmRollItem> film = getFilm(camera.getStack()).orElseThrow();
                film.getItem().addFrame(film.getStack(), exposureFrame);
                setFilm(camera.getStack(), film.getStack());

                // Update camera serverside:
                Packets.sendToServer(new SyncCameraServerboundPacket(camera.getStack(), hand));
            } else {
                onShotTakenClientside(player, camera, captureProperties);
            }
        }
    }

    protected boolean shouldFlashFire(Player player, CameraInHand camera) {
        if (getAttachment(camera.getStack(), FLASH_ATTACHMENT).isEmpty())
            return false;

        FlashMode flashMode = getFlashMode(camera.getStack());

        if (flashMode == FlashMode.ON)
            return true;
        else if (flashMode == FlashMode.AUTO) {
            Level level = player.getLevel();

            level.updateSkyBrightness(); // This updates 'getSkyDarken' on the client. Without it always returns 0.
            int skyBrightness = level.getBrightness(LightLayer.SKY, player.blockPosition());
            int blockBrightness = level.getBrightness(LightLayer.BLOCK, player.blockPosition());
            int lightLevel = skyBrightness < 15 ?
                    Math.max(blockBrightness, (int) (skyBrightness * ((15 - level.getSkyDarken()) / 15f))) :
                    Math.max(blockBrightness, 15 - level.getSkyDarken());

            return lightLevel < 8;
        }

        return false;
    }

    public boolean tryUseFlash(Player player, CameraInHand camera) {
        Level level = player.getLevel();
        BlockPos playerHeadPos = player.blockPosition().above();
        @Nullable BlockPos flashPos = null;

        if (level.getBlockState(playerHeadPos).isAir() || level.getFluidState(playerHeadPos)
                .isSourceOfType(Fluids.WATER))
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

    protected ExposedFrame createExposureFrame(Player player, CaptureProperties captureProperties, ItemAndStack<CameraItem> camera, List<Entity> entitiesInFrame) {
        Vec3 shotPosition = player.position().add(0, player.getEyeY(), 0);

        List<ExposedFrame.EntityInfo> entitiesData = new ArrayList<>();
        for (Entity entity : EntitiesInFrame.get(player)) {
            ResourceLocation entityTypeKey = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
            CompoundTag tag = new CompoundTag();
            ListTag pos = new ListTag();
            pos.add(DoubleTag.valueOf(entity.getX()));
            pos.add(DoubleTag.valueOf(entity.getY()));
            pos.add(DoubleTag.valueOf(entity.getZ()));
            tag.put("Pos", pos);
            tag = addAdditionalEntityInFrameData(tag, player, entity, captureProperties, camera);
            entitiesData.add(new ExposedFrame.EntityInfo(entityTypeKey, tag));
        }

        ResourceLocation dimension = player.level.dimension().location();
        ResourceLocation biome = player.level.getBiome(player.blockPosition()).unwrapKey().map(ResourceKey::location)
                .orElse(null);

        return new ExposedFrame(captureProperties.id, player.getScoreboardName(), Util.getFilenameFormattedDateTime(),
                shotPosition, dimension, biome, captureProperties.flash, entitiesData);
    }

    protected CompoundTag addAdditionalEntityInFrameData(CompoundTag tag, Player player, Entity entityInFrame, CaptureProperties captureProperties, ItemAndStack<CameraItem> camera) {
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

    protected String getExposureId(Player player, Level level) {
        // This method called only client-side and then gets sent to server in a packet
        // because gameTime is different between client/server (by 1 tick, as I've seen), and IDs won't match.
        return player.getName().getString() + "_" + level.getGameTime();
    }

    public FocalRange getFocalRange(ItemStack cameraStack) {
        return getAttachment(cameraStack, LENS_ATTACHMENT).isEmpty() ? new FocalRange(18, 55) : new FocalRange(55, 200);
    }

    protected CaptureProperties createCaptureProperties(Player player, CameraInHand camera, boolean flash) {
        Preconditions.checkState(!camera.isEmpty(), "Camera cannot be empty.");
        String id = getExposureId(player, player.level);

        int frameSize = getFilm(camera.getStack()).map(f -> f.getItem().getFrameSize(f.getStack())).orElse(320);
        float cropFactor = Config.Client.VIEWFINDER_CROP_FACTOR.get().floatValue();

        float brightnessStops = getShutterSpeed(camera.getStack()).getStopsDifference(getDefaultShutterSpeed(camera.getStack()));
        return new CaptureProperties(id, frameSize, cropFactor, brightnessStops, flash, getExposureModifiers(player, camera),
                List.of(new ExposureStorageSaver(), ExposureFileSaver.withDefaultFolders()));
    }

    protected List<IExposureModifier> getExposureModifiers(Player player, CameraInHand camera) {
        List<IExposureModifier> modifiers = new ArrayList<>();

        modifiers.add(ExposureModifiers.BRIGHTNESS);

        getFilm(camera.getStack()).ifPresent(film -> {
            if (film.getItem().getType() == FilmType.BLACK_AND_WHITE)
                modifiers.add(ExposureModifiers.BLACK_AND_WHITE);
        });

        return modifiers;
    }

    public void onShutterOpen(Player player, Shutter.OpenShutter shutter) {
        player.getLevel()
                .playSound(player, player, Exposure.SoundEvents.SHUTTER_OPEN.get(), SoundSource.PLAYERS, shutter.exposingFrame() ? 0.85f : 0.65f,
                        player.getLevel().getRandom().nextFloat() * 0.15f + (shutter.exposingFrame() ? 1.1f : 1.25f));

        if (shutter.shutterSpeed().getMilliseconds() > 500) // More than 1/2
            OnePerPlayerSounds.play(player, Exposure.SoundEvents.SHUTTER_TICKING.get(), SoundSource.PLAYERS, 1f, 1f);
    }

    public void onShutterTick(Player player, Shutter.OpenShutter shutter) {

    }

    public void onShutterClosed(Player player, Shutter.OpenShutter shutter) {
        player.getLevel()
                .playSound(player, player, Exposure.SoundEvents.SHUTTER_CLOSE.get(), SoundSource.PLAYERS, shutter.exposingFrame() ? 0.85f : 0.65f,
                        player.getLevel().getRandom().nextFloat() * 0.15f + (shutter.exposingFrame() ? 1f : 1.2f));
        if (shutter.exposingFrame()) {
            OnePerPlayerSounds.play(player, Exposure.SoundEvents.FILM_ADVANCE.get(), SoundSource.PLAYERS,
                    1f, player.getLevel().getRandom().nextFloat() * 0.15f + 0.93f);
        }
    }

    /**
     * This method is called after we take a screenshot (or immediately if not capturing but should show effects). Otherwise, due to the delays (flash, etc) - particles would be captured as well.
     */
    public void onShotTakenClientside(@NotNull Player player, CameraInHand cameraInHand, CaptureProperties properties) {
        Preconditions.checkState(player.getLevel().isClientSide, "This methods should only be called client-side.");
        if (properties.flash) {
            Level level = player.getLevel();
            Vec3 pos = player.position();
            Vec3 lookAngle = player.getLookAngle();
            pos = pos.add(0, 1, 0).add(lookAngle.multiply(0.8f, 0.8f, 0.8f));

//            level.addParticle(ParticleTypes.FLASH, pos.x, pos.y, pos.z, 0, 0, 0);
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
            float prevZoom = getZoom(cameraStack);
            FocalRange prevFocalRange = getFocalRange(cameraStack);
            FocalRange newFocalRange = attachmentStack.isEmpty() ? FocalRange.SHORT : FocalRange.LONG;
            float adjustedZoom = Mth.map(prevZoom, prevFocalRange.min(), prevFocalRange.max(), newFocalRange.min(), newFocalRange.max());
            setZoom(cameraStack, adjustedZoom);
        }
    }

    // ---

    /**
     * Brightness of the exposure will not be changed on this shutter speed.
     */
    public ShutterSpeed getDefaultShutterSpeed(ItemStack cameraStack) {
        return SHUTTER_SPEEDS.get(10); // 1/60;
    }

    /**
     * Returns all possible Shutter Speeds for this camera.
     */
    public List<ShutterSpeed> getAllShutterSpeeds(ItemStack cameraStack) {
        return SHUTTER_SPEEDS;
    }

    public ShutterSpeed getShutterSpeed(ItemStack cameraStack) {
        return ShutterSpeed.loadOrDefault(cameraStack.getOrCreateTag(), getDefaultShutterSpeed(cameraStack));
    }

    public void setShutterSpeed(ItemStack cameraStack, ShutterSpeed shutterSpeed) {
        shutterSpeed.save(cameraStack.getOrCreateTag());
    }

    public float getZoom(ItemStack cameraStack) {
        return cameraStack.hasTag() ? cameraStack.getOrCreateTag().getFloat("Zoom") : getFocalRange(cameraStack).min();
    }

    public void setZoom(ItemStack cameraStack, float focalLength) {
        cameraStack.getOrCreateTag().putFloat("Zoom", focalLength);
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
