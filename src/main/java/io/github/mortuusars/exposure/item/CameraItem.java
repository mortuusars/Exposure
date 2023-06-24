package io.github.mortuusars.exposure.item;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.camera.Camera;
import io.github.mortuusars.exposure.camera.CaptureProperties;
import io.github.mortuusars.exposure.camera.ExposureFrame;
import io.github.mortuusars.exposure.camera.IExposureModifier;
import io.github.mortuusars.exposure.camera.film.FilmType;
import io.github.mortuusars.exposure.camera.modifier.ExposureModifiers;
import io.github.mortuusars.exposure.camera.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.client.ClientOnlyLogic;
import io.github.mortuusars.exposure.item.attachment.CameraAttachments;
import io.github.mortuusars.exposure.item.attachment.Film;
import io.github.mortuusars.exposure.menu.CameraMenu;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMaps;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

import java.util.*;

public class CameraItem extends Item {
    public static final int FILM = 0;
    public static final int LENS = 1;
    public static final int FILTER = 2;

    public static final Int2ObjectSortedMap<String> SLOTS = new Int2ObjectRBTreeMap<>(
            new int[] {0, 1, 2},
            new String[] {"Film", "Lens", "Filter"}
    );

    public CameraItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.SPYGLASS;
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

    protected void useCamera(Player player, InteractionHand hand) {
        if (player.isSecondaryUseActive() && !Viewfinder.isActive()) {
            if (player instanceof ServerPlayer serverPlayer)
                openCameraGUI(serverPlayer, hand);

            return;
        }

        if (!player.getLevel().isClientSide)
            return;

        ItemStack cameraStack = player.getItemInHand(hand);

        if (Viewfinder.isActive())
            tryTakeShot(player, hand);
        else
            Viewfinder.activate(cameraStack, hand);
    }

    protected void openCameraGUI(ServerPlayer serverPlayer, InteractionHand hand) {
        ItemStack cameraStack = serverPlayer.getItemInHand(hand);
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

    public CameraAttachments getAttachments(ItemStack cameraStack) {
        validateCameraStack(cameraStack);
        return new CameraAttachments(cameraStack);
    }

    protected void validateCameraStack(ItemStack cameraStack) {
        Preconditions.checkArgument(!cameraStack.isEmpty(), "cameraStack is empty.");
        Preconditions.checkArgument(cameraStack.getItem() instanceof CameraItem,  cameraStack + " is not a CameraItem.");
    }

    protected boolean tryTakeShot(Player player, InteractionHand hand) {
        Level level = player.level;
        ItemStack cameraStack = player.getItemInHand(hand);
        Optional<Film> filmOpt = getAttachments(cameraStack).getFilm();

        if (filmOpt.isEmpty()) {
            onShutterReleased(player, hand, cameraStack);
            return false;
        }

        Film film = filmOpt.get();

        int emptyFrame = film.getItem().getEmptyFrame(film.getStack());

        if (emptyFrame == -1) {
            player.displayClientMessage(Component.translatable("item.exposure.camera.no_empty_frames"), true);
            player.getLevel().playSound(player, player, SoundEvents.UI_BUTTON_CLICK, SoundSource.PLAYERS, 1f,
                    player.getLevel().getRandom().nextFloat() * 0.2f + 1.1f);
            return false;
        }

        level.playSound(player, player, SoundEvents.UI_LOOM_SELECT_PATTERN, SoundSource.PLAYERS, 1f,
                level.getRandom().nextFloat() * 0.2f + 1.1f);

        if (player.level.isClientSide) {
            CaptureProperties captureProperties = createCaptureProperties(player, hand);
            Camera.capture(captureProperties);

            onShutterReleased(player, hand, cameraStack);

            film.getItem().setFrame(film.getStack(), emptyFrame, new ExposureFrame(captureProperties.id));
            getAttachments(cameraStack).setFilm(film.getStack());

            clientsideUpdateCameraInInventory(cameraStack, hand);
        }

        return true;
    }

    protected void onShutterReleased(Player player, InteractionHand hand, ItemStack cameraStack) {
        player.getCooldowns().addCooldown(this, 10);

        // TODO: shutter open sound
        player.getLevel().playSound(player, player, SoundEvents.UI_BUTTON_CLICK, SoundSource.PLAYERS, 1f,
                player.getLevel().getRandom().nextFloat() * 0.2f + 1.1f);
    }

    protected String getExposureId(Player player, Level level) {
        // This method called only client-side and then gets sent to server in a packet
        // because gameTime is different between client/server (by 1 tick, as I've seen), and IDs won't match.
        return player.getName().getString() + "_" + level.getGameTime();
    }

    public MinMaxBounds<Integer> getFocalRange(ItemStack cameraStack) {
        CameraAttachments attachments = getAttachments(cameraStack);
        ItemStack lensStack = attachments.getAttachment(SLOTS.get(LENS));
        return lensStack.isEmpty() ? MinMaxBounds.Ints.between(18, 55) : MinMaxBounds.Ints.between(55, 200);
    }

    protected CaptureProperties createCaptureProperties(Player player, InteractionHand hand) {
        String id = getExposureId(player, player.level);

        // TODO: Crop Factor config
        float cropFactor = 1.142f;

        CameraAttachments attachments = getAttachments(player.getItemInHand(hand));
        int frameSize = attachments.getFilm().map(f -> f.getItem().getFrameSize()).orElse(-1);

        return new CaptureProperties(id, frameSize, cropFactor, 1f, getExposureModifiers(player, hand));
    }

    protected List<IExposureModifier> getExposureModifiers(Player player, InteractionHand hand) {
        List<IExposureModifier> modifiers = new ArrayList<>();

        CameraAttachments attachments = getAttachments(player.getItemInHand(hand));
        attachments.getFilm().ifPresent(f -> {
            if (f.getItem().getType() == FilmType.BLACK_AND_WHITE)
                modifiers.add(ExposureModifiers.BLACK_AND_WHITE);
        });

        return modifiers;
    }

    public void attachmentsChanged(Player player, ItemStack cameraStack, int slot, ItemStack attachmentStack) {
        getAttachments(cameraStack).setAttachment(SLOTS.get(slot), attachmentStack);

        if (!attachmentStack.isEmpty())
            player.playSound(SoundEvents.COMPARATOR_CLICK);
    }

    public void clientsideUpdateCameraInInventory(ItemStack cameraStack, InteractionHand hand) {
        ClientOnlyLogic.updateAndSendCameraStack(cameraStack, hand);
    }
}
