package io.github.mortuusars.exposure.item;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.*;
import io.github.mortuusars.exposure.camera.component.CompositionGuide;
import io.github.mortuusars.exposure.camera.component.CompositionGuides;
import io.github.mortuusars.exposure.camera.component.FocalRange;
import io.github.mortuusars.exposure.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.camera.film.FilmType;
import io.github.mortuusars.exposure.camera.modifier.ExposureModifiers;
import io.github.mortuusars.exposure.camera.modifier.IExposureModifier;
import io.github.mortuusars.exposure.client.ClientOnlyLogic;
import io.github.mortuusars.exposure.item.attachment.CameraAttachments;
import io.github.mortuusars.exposure.menu.CameraMenu;
import io.github.mortuusars.exposure.util.CameraInHand;
import io.github.mortuusars.exposure.util.ItemAndStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CameraItem extends Item {
    public static final int FILM = 0;
    public static final int LENS = 1;
    public static final int FILTER = 2;

    public static final Int2ObjectSortedMap<String> SLOTS = new Int2ObjectRBTreeMap<>(
            new int[] { 0, 1, 2 },
            new String[] { "Film", "Lens", "Filter" }
    );

    public static final List<ShutterSpeed> SHUTTER_SPEED_VALUES = List.of(
            new ShutterSpeed(15f, 200),
            new ShutterSpeed(8f, 110),
            new ShutterSpeed(4f, 70),
            new ShutterSpeed(2f, 45),
            new ShutterSpeed(1f, 30),
            new ShutterSpeed(1/2f, 25),
            new ShutterSpeed(1/4f, 20),
            new ShutterSpeed(1/8f, 17),
            new ShutterSpeed(1/15f, 15),
            new ShutterSpeed(1/30f, 13),
            new ShutterSpeed(1/60f, 10),
            new ShutterSpeed(1/125f, 8),
            new ShutterSpeed(1/250f, 6),
            new ShutterSpeed(1/500f, 4),
            new ShutterSpeed(1/1000f, 2)
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
        if (!Exposure.getCamera().isActive(player)) {
            if (player.isSecondaryUseActive())
                openCameraGUI(player, hand);
            else
                Exposure.getCamera().activate(player, hand);

            return;
        }

        CameraInHand camera = Exposure.getCamera().getCameraInHand(player);
        Preconditions.checkState(!camera.isEmpty());

        Exposure.getCamera().getShutter().open(player, getShutterSpeed(camera.getStack()));
        player.getCooldowns().addCooldown(this, 4);

        CameraAttachments attachments = getAttachments(camera.getStack());
        Optional<ItemAndStack<FilmItem>> film = attachments.getFilm();

        if (film.isEmpty() || !film.get().getItem().canAddFrame(film.get().getStack()))
            return;

        if (player.level.isClientSide) {
            if (CameraCapture.isCapturing())
                return;

            CaptureProperties captureProperties = createCaptureProperties(player, hand);
            CameraCapture.capture(captureProperties);

//            onShutterReleased(player, hand, cameraStack);

            film.get().getItem().addFrame(film.get().getStack(), new ExposureFrame(captureProperties.id));
            getAttachments(camera.getStack()).setFilm(film.get().getStack());


            ClientOnlyLogic.updateAndSyncCameraStack(camera.getStack(), camera.getHand());
        }

        // TODO: Take Shot
    }

    public CameraAttachments getAttachments(ItemStack cameraStack) {
        validateCameraStack(cameraStack);
        return new CameraAttachments(cameraStack);
    }

    protected void validateCameraStack(ItemStack cameraStack) {
        Preconditions.checkArgument(!cameraStack.isEmpty(), "cameraStack is empty.");
        Preconditions.checkArgument(cameraStack.getItem() instanceof CameraItem,  cameraStack + " is not a CameraItem.");
    }

    public void tryTakeShot(Player player, InteractionHand hand) {
        Level level = player.level;
        ItemStack cameraStack = player.getItemInHand(hand);
        Optional<ItemAndStack<FilmItem>> filmOpt = getAttachments(cameraStack).getFilm();

        if (filmOpt.isEmpty()) {
//            onShutterReleased(player, hand, cameraStack);
            return;
        }

        ItemAndStack<FilmItem> film = filmOpt.get();

        if (!film.getItem().canAddFrame(film.getStack())) {
//            onShutterReleased(player, hand, cameraStack);
            return;
        }

        level.playSound(player, player, SoundEvents.UI_LOOM_SELECT_PATTERN, SoundSource.PLAYERS, 1f,
                level.getRandom().nextFloat() * 0.2f + 1.1f);

        if (player.level.isClientSide) {
            if (CameraCapture.isCapturing())
                return;

            CaptureProperties captureProperties = createCaptureProperties(player, hand);
            CameraCapture.capture(captureProperties);

//            onShutterReleased(player, hand, cameraStack);

            film.getItem().addFrame(film.getStack(), new ExposureFrame(captureProperties.id));
            getAttachments(cameraStack).setFilm(film.getStack());

//            CameraOld.updateAndSyncCameraInHand(cameraStack);
        }
    }


    protected void openCameraGUI(Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            ItemStack cameraStack = player.getItemInHand(hand);
            NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
                @Override
                public @NotNull Component getDisplayName() {
                    return cameraStack.getHoverName();
                }

                @Override
                public @NotNull AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                    return new CameraMenu(containerId, playerInventory, cameraStack);
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
        CameraAttachments attachments = getAttachments(cameraStack);
        ItemStack lensStack = attachments.getAttachment(SLOTS.get(LENS));
        return lensStack.isEmpty() ? new FocalRange(18, 55) : new FocalRange(55, 200);
    }

    protected CaptureProperties createCaptureProperties(Player player, InteractionHand hand) {
        String id = getExposureId(player, player.level);

        // TODO: Crop Factor config
        float cropFactor = 1.142f;

        ItemStack cameraStack = player.getItemInHand(hand);
        Preconditions.checkState(cameraStack.getItem() instanceof CameraItem, cameraStack + " is not a CameraItem.");

        CameraAttachments attachments = getAttachments(cameraStack);
        int frameSize = attachments.getFilm().map(f -> f.getItem().getFrameSize()).orElse(-1);

        return new CaptureProperties(id, frameSize, cropFactor, getShutterSpeed(cameraStack), getExposureModifiers(player, hand));
    }

    protected List<IExposureModifier> getExposureModifiers(Player player, InteractionHand hand) {
        List<IExposureModifier> modifiers = new ArrayList<>();

        modifiers.add(ExposureModifiers.BRIGHTNESS);

        CameraAttachments attachments = getAttachments(player.getItemInHand(hand));
        attachments.getFilm().ifPresent(f -> {
            if (f.getItem().getType() == FilmType.BLACK_AND_WHITE)
                modifiers.add(ExposureModifiers.BLACK_AND_WHITE);
        });

        return modifiers;
    }

    public void attachmentsChanged(Player player, ItemStack cameraStack, int slot, ItemStack attachmentStack) {
        // Adjust zoom for new focal range to the same percentage:
        if (slot == LENS) {
            float prevZoom = getZoom(cameraStack);
            FocalRange prevFocalRange = getFocalRange(cameraStack);
            FocalRange newFocalRange = attachmentStack.isEmpty() ? FocalRange.SHORT : FocalRange.LONG;

            float adjustedZoom = Mth.map(prevZoom, prevFocalRange.min(), prevFocalRange.max(), newFocalRange.min(), newFocalRange.max());
            setZoom(cameraStack, adjustedZoom);
        }

        getAttachments(cameraStack).setAttachment(SLOTS.get(slot), attachmentStack);

        if (player.getLevel().isClientSide && player.containerMenu instanceof CameraMenu cameraMenu && cameraMenu.initialized) {
            if (slot == LENS) {
                player.playSound(attachmentStack.isEmpty() ? SoundEvents.SPYGLASS_STOP_USING : SoundEvents.SPYGLASS_USE);
            }
        }
    }

    public ShutterSpeed getDefaultShutterSpeed(ItemStack cameraStack) {
        return SHUTTER_SPEED_VALUES.get(10); // 1/60;
    }

    public List<ShutterSpeed> getAllShutterSpeeds(ItemStack cameraStack) {
        return SHUTTER_SPEED_VALUES;
    }

    public ShutterSpeed getShutterSpeed(ItemStack cameraStack) {
        return ShutterSpeed.loadOrDefault(cameraStack.getOrCreateTag().getCompound("ShutterSpeed"),
                getDefaultShutterSpeed(cameraStack));
    }

    public void setShutterSpeed(ItemStack cameraStack, ShutterSpeed shutterSpeed) {
        cameraStack.getOrCreateTag().put("ShutterSpeed", shutterSpeed.save(new CompoundTag()));
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
