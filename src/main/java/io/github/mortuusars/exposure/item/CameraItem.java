package io.github.mortuusars.exposure.item;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.CaptureProperties;
import io.github.mortuusars.exposure.camera.ExposureCapture;
import io.github.mortuusars.exposure.camera.ExposureFrame;
import io.github.mortuusars.exposure.camera.component.CompositionGuide;
import io.github.mortuusars.exposure.camera.component.CompositionGuides;
import io.github.mortuusars.exposure.camera.component.FocalRange;
import io.github.mortuusars.exposure.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.camera.film.FilmType;
import io.github.mortuusars.exposure.camera.infrastructure.EntitiesInFrame;
import io.github.mortuusars.exposure.camera.modifier.ExposureModifiers;
import io.github.mortuusars.exposure.camera.modifier.IExposureModifier;
import io.github.mortuusars.exposure.menu.CameraAttachmentsMenu;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.ServerboundSyncCameraPacket;
import io.github.mortuusars.exposure.storage.saver.ExposureStorageSaver;
import io.github.mortuusars.exposure.util.CameraInHand;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpyglassItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class CameraItem extends Item {
    public record AttachmentType(String id, int slot, Predicate<ItemStack> stackValidator) {}

    public static final AttachmentType FILM_ATTACHMENT = new AttachmentType("Film", 0, stack -> stack.getItem() instanceof FilmItem);
    public static final AttachmentType LENS_ATTACHMENT = new AttachmentType("Lens", 1, stack -> stack.getItem() instanceof SpyglassItem);
    public static final AttachmentType FILTER_ATTACHMENT = new AttachmentType("Filter", 2, stack -> stack.is(Tags.Items.GLASS_PANES));
    public static final List<AttachmentType> ATTACHMENTS = List.of(
            FILM_ATTACHMENT,
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
        return UseAnim.BLOCK;
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

            return;
        }

        if (Exposure.getCamera().getShutter().isOpen(player))
            return;

        CameraInHand camera = Exposure.getCamera().getCameraInHand(player);
        Preconditions.checkState(!camera.isEmpty());

        Exposure.getCamera().getShutter().open(player, getShutterSpeed(camera.getStack()));
        player.getCooldowns().addCooldown(this, 3);

        Optional<ItemAndStack<FilmItem>> film = getFilm(camera.getStack());

        if (film.isEmpty() || !film.get().getItem().canAddFrame(film.get().getStack()))
            return;

        if (player.getLevel().isClientSide) {
            if (ExposureCapture.isCapturing())
                return;

            CaptureProperties captureProperties = createCaptureProperties(player, camera);
            ExposureCapture.enqueueCapture(captureProperties);

            ExposureFrame exposureFrame = createExposureFrame(player, captureProperties, camera.getCamera(), EntitiesInFrame.get(player));

            film.get().getItem().addFrame(film.get().getStack(), exposureFrame);

            setFilm(camera.getStack(), film.get().getStack());

            // Update camera serverside:
            Packets.sendToServer(new ServerboundSyncCameraPacket(camera.getStack(), hand));
        }
    }

    protected ExposureFrame createExposureFrame(Player player, CaptureProperties captureProperties, ItemAndStack<CameraItem> camera, List<Entity> entitiesInFrame) {
        Vec3 shotPosition = player.position().add(0, player.getEyeY(), 0);

        List<ExposureFrame.EntityInfo> entitiesData = new ArrayList<>();
        for (Entity entity : EntitiesInFrame.get(player)) {
            ResourceLocation entityTypeKey = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
            CompoundTag tag = new CompoundTag();
            ListTag pos = new ListTag();
            pos.add(DoubleTag.valueOf(entity.getX()));
            pos.add(DoubleTag.valueOf(entity.getY()));
            pos.add(DoubleTag.valueOf(entity.getZ()));
            tag.put("Pos", pos);
            tag = addAdditionalEntityInFrameData(tag, player, entity, captureProperties, camera);
            entitiesData.add(new ExposureFrame.EntityInfo(entityTypeKey, tag));
        }

        ResourceLocation dimension = player.level.dimension().location();
        ResourceLocation biome = player.level.getBiome(player.blockPosition()).unwrapKey().map(ResourceKey::location).orElse(null);

        return new ExposureFrame(captureProperties.id, player.getScoreboardName(), Util.getFilenameFormattedDateTime(),
                shotPosition, dimension, biome, entitiesData);
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

    protected CaptureProperties createCaptureProperties(Player player, CameraInHand camera) {
        Preconditions.checkState(!camera.isEmpty(), "Camera cannot be empty.");
        String id = getExposureId(player, player.level);

        // TODO: Crop Factor config
        float cropFactor = 1.142f;

        int frameSize = getFilm(camera.getStack()).map(f -> f.getItem().getFrameSize(f.getStack())).orElseThrow();

        float brightnessStops = getShutterSpeed(camera.getStack()).getStopsDifference(getDefaultShutterSpeed(camera.getStack()));
        return new CaptureProperties(id, frameSize, cropFactor, brightnessStops, getExposureModifiers(player, camera),
                List.of(new ExposureStorageSaver()));
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

    public Optional<ItemAndStack<FilmItem>> getFilm(ItemStack cameraStack) {
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
        }
        else {
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
}
