package io.github.mortuusars.exposure.item;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.Camera;
import io.github.mortuusars.exposure.camera.ExposureFrame;
import io.github.mortuusars.exposure.camera.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.client.GUI;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.ServerboundUpdateCameraPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

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
            if (tryLoadFilmRoll(player, hand))
                return;

            if (player.getLevel().isClientSide) {
                if (Viewfinder.isActive())
                    Viewfinder.setActive(false);
                else {
                    ItemStack itemInHand = player.getItemInHand(hand);
                    String lastShot = itemInHand.getOrCreateTag().getString("lastShot");

                    ItemStack film = getLoadedFilm(itemInHand);
                    List<ExposureFrame> frames = ((FilmItem) film.getItem()).getFrames(film).stream()
                            .filter(frame -> !StringUtil.isNullOrEmpty(frame.id)).toList();
                    if (frames.size() > 0)
                        GUI.showExposureViewScreen(film);
                    else
                        player.displayClientMessage(Component.translatable("item.camera.no_exposures"), true);
                }
            }
        }
        else {
//            if (player.getLevel().isClientSide) {
//            Minecraft.getInstance().gameRenderer.loadEffect(new ResourceLocation("exposure:shaders/post/orange_tint.json"));
                if (Viewfinder.isActive()) {
                    if (hasLoadedFilm(player.getItemInHand(hand)))
                        tryTakeShot(player, hand);
                    else {
                        player.displayClientMessage(Component.translatable("item.exposure.camera.no_film_loaded")
                                .withStyle(ChatFormatting.RED), true);
                    }
                }
                else
                    Viewfinder.setActive(true);
//            }
        }
    }

    public boolean hasLoadedFilm(ItemStack cameraStack) {
        return cameraStack.getTag() != null && cameraStack.getTag().contains("Film", Tag.TAG_COMPOUND);
    }

    public ItemStack getLoadedFilm(ItemStack cameraStack) {
        if (!hasLoadedFilm(cameraStack))
            return ItemStack.EMPTY;

        CompoundTag film = cameraStack.getOrCreateTag().getCompound("Film");
        return ItemStack.of(film);
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

    public void setFilm(ItemStack cameraStack, ItemStack filmStack) {
        cameraStack.getOrCreateTag().put("Film", filmStack.save(new CompoundTag()));
    }

    protected boolean tryTakeShot(Player player, InteractionHand hand) {
        Level level = player.level;

        ItemStack cameraStack = player.getItemInHand(hand);
        ItemStack film = getLoadedFilm(cameraStack);

        if (!(film.getItem() instanceof FilmItem filmItem))
            throw new IllegalStateException("Loaded film is not a film item: " + film);


        int slot = filmItem.getEmptyFrame(film);

//        setFilm(cameraStack, film);

        if (slot == -1) {
            player.displayClientMessage(Component.translatable("item.exposure.camera.no_empty_frames"), true);
            level.playSound(player, player, SoundEvents.UI_BUTTON_CLICK, SoundSource.PLAYERS, 1f,
                    level.getRandom().nextFloat() * 0.2f + 1.1f);
            return false;
        }

//        level.playSound(player, player, SoundEvents.UI_LOOM_SELECT_PATTERN, SoundSource.PLAYERS, 1f,
//                level.getRandom().nextFloat() * 0.2f + 1.1f);



        if (player.level.isClientSide) {
            String id = player.getName().getString() + "_" + level.getGameTime();

            Camera.capture(id);

//            setFilm(cameraStack, filmItem.setFrame(film, slot, new ExposureFrame(id)));
//            player.setItemInHand(hand, cameraStack);

//            ItemStack itemInHand = cameraStack;
//            itemInHand.getOrCreateTag().putString("lastShot", id);
            Packets.sendToServer(new ServerboundUpdateCameraPacket(id, hand, slot));
        }

//        boolean useFlash = true;
//
//        if (useFlash) {
//            BlockPos initialFlashPos = player.blockPosition().above();
//
////            if (level.getBlockState(initialFlashPos))
//
//
//        }

        return true;
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
