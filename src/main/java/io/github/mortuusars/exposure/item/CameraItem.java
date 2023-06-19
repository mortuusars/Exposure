package io.github.mortuusars.exposure.item;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.Camera;
import io.github.mortuusars.exposure.camera.CaptureProperties;
import io.github.mortuusars.exposure.camera.ExposureFrame;
import io.github.mortuusars.exposure.camera.IExposureModifier;
import io.github.mortuusars.exposure.camera.film.FilmType;
import io.github.mortuusars.exposure.camera.modifier.ExposureModifiers;
import io.github.mortuusars.exposure.camera.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.client.GUI;
import io.github.mortuusars.exposure.menu.CameraMenu;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.ServerboundUpdateCameraPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringUtil;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CameraItem extends Item {
    public CameraItem(Properties properties) {
        super(properties);
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
        if (player.isSecondaryUseActive()) {

            if (player instanceof ServerPlayer serverPlayer) {
                openCameraGUI(serverPlayer, hand);
            }

            return;
//            if (tryLoadFilmRoll(player, hand))
//                return;

//            if (player.getLevel().isClientSide) {
//                if (Viewfinder.isActive())
//                    Viewfinder.setActive(false);
//                else {
//                    ItemStack itemInHand = player.getItemInHand(hand);
//
//                    ItemStack film = getLoadedFilm(itemInHand);
//                    List<ExposureFrame> frames = ((FilmItem) film.getItem()).getFrames(film).stream()
//                            .filter(frame -> !StringUtil.isNullOrEmpty(frame.id)).toList();
//                    if (frames.size() > 0)
//                        GUI.showExposureViewScreen(film);
//                    else
//                        player.displayClientMessage(Component.translatable("item.camera.no_exposures"), true);
//                }
//            }
        }

        if (Viewfinder.isActive()) {
            if (!hasLoadedFilm(player.getItemInHand(hand))) {
                player.displayClientMessage(Component.translatable("item.exposure.camera.no_film_loaded")
                        .withStyle(ChatFormatting.RED), true);
                return;
            }

            tryTakeShot(player, hand);
        }
        else
            Viewfinder.setActive(true);
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

    public boolean hasLoadedFilm(ItemStack cameraStack) {
        return cameraStack.getTag() != null && cameraStack.getTag().contains("Film", Tag.TAG_COMPOUND);
    }

    public void setFilm(ItemStack cameraStack, ItemStack filmStack) {
        cameraStack.getOrCreateTag().put("Film", filmStack.save(new CompoundTag()));
    }

    public ItemStack getLoadedFilm(ItemStack cameraStack) {
        if (!hasLoadedFilm(cameraStack))
            return ItemStack.EMPTY;

        CompoundTag film = cameraStack.getOrCreateTag().getCompound("Film");
        ItemStack filmStack = ItemStack.of(film);

        if (!(filmStack.getItem() instanceof FilmItem)) {
            Exposure.LOGGER.error(filmStack + " is not a FilmItem.");
            return ItemStack.EMPTY;
        }

        return filmStack;
    }

    public boolean tryLoadFilmRoll(Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);
        if (itemInHand.getItem() instanceof CameraItem && !hasLoadedFilm(itemInHand)) {
            InteractionHand otherHand = InteractionHand.values()[(hand.ordinal() + 1) % 2];
            ItemStack otherHandItem = player.getItemInHand(otherHand);
            if (otherHandItem.getItem() instanceof FilmItem) {
                itemInHand.getOrCreateTag().put("Film", otherHandItem.save(new CompoundTag()));

                otherHandItem.shrink(1);
                player.level.playSound(player, player, SoundEvents.UI_LOOM_SELECT_PATTERN, SoundSource.PLAYERS, 1f, 1f);

                return true;
            }
        }

        return false;
    }

    public ItemStack getLens(ItemStack cameraStack) {
        CompoundTag lensTag = cameraStack.getOrCreateTag().getCompound("Lens");
        return ItemStack.of(lensTag);
    }

    public void setLens(ItemStack cameraStack, ItemStack lensStack) {
        cameraStack.getOrCreateTag().put("Lens", lensStack.save(new CompoundTag()));
    }

    protected boolean tryTakeShot(Player player, InteractionHand hand) {
        Level level = player.level;
        ItemStack cameraStack = player.getItemInHand(hand);
        ItemStack film = getLoadedFilm(cameraStack);

        if (!(film.getItem() instanceof FilmItem filmItem))
            throw new IllegalStateException("Loaded film is not a film item: " + film);

        int emptyFrame = filmItem.getEmptyFrame(film);

        if (emptyFrame == -1) {
            player.displayClientMessage(Component.translatable("item.exposure.camera.no_empty_frames"), true);
            level.playSound(player, player, SoundEvents.UI_BUTTON_CLICK, SoundSource.PLAYERS, 1f,
                    level.getRandom().nextFloat() * 0.2f + 1.1f);
            return false;
        }

        level.playSound(player, player, SoundEvents.UI_LOOM_SELECT_PATTERN, SoundSource.PLAYERS, 1f,
                level.getRandom().nextFloat() * 0.2f + 1.1f);

        if (player.level.isClientSide) {
            CaptureProperties captureProperties = createCaptureProperties(player, hand);
            Camera.capture(captureProperties);

            //TODO: Update camera on the server
            Packets.sendToServer(new ServerboundUpdateCameraPacket(captureProperties.id, hand, emptyFrame));
        }

        return true;
    }

    protected String getExposureId(Player player, Level level) {
        // This method called only client-side and then gets sent to server in a packet
        // because gameTime is different between client/server (by 1 tick, as I've seen), and IDs won't match.
        return player.getName().getString() + "_" + level.getGameTime();
    }

    protected CaptureProperties createCaptureProperties(Player player, InteractionHand hand) {
        String id = getExposureId(player, player.level);

        // TODO: Crop Factor config
        float cropFactor = 1.142f;

        ItemStack film = getLoadedFilm(player.getItemInHand(hand));
        int frameSize = ((FilmItem) film.getItem()).getFrameSize();

        return new CaptureProperties(id, frameSize, cropFactor, 1f, getExposureModifiers(player, hand));
    }

    protected List<IExposureModifier> getExposureModifiers(Player player, InteractionHand hand) {
        List<IExposureModifier> modifiers = new ArrayList<>();

        ItemStack film = getLoadedFilm(player.getItemInHand(hand));
        if (((FilmItem) film.getItem()).getType() == FilmType.BLACK_AND_WHITE)
            modifiers.add(ExposureModifiers.BLACK_AND_WHITE);

        return modifiers;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.SPYGLASS;
    }
}
