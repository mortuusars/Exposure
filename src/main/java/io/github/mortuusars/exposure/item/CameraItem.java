package io.github.mortuusars.exposure.item;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.Camera;
import io.github.mortuusars.exposure.camera.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.client.GUI;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.ServerboundUpdateCameraPacket;
import io.github.mortuusars.exposure.storage.IExposureStorage;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

import java.util.Objects;

public class CameraItem extends Item {
    public CameraItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        useCamera(Objects.requireNonNull(context.getPlayer()), context.getHand());
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        useCamera(player, usedHand);
        return super.use(level, player, usedHand);
    }

    protected void useCamera(Player player, InteractionHand usedHand) {



        if (player.isSecondaryUseActive()) {
            if (player.getLevel().isClientSide) {
                if (Viewfinder.isActive())
                    Viewfinder.setActive(false);
                else {
                    ItemStack itemInHand = player.getItemInHand(usedHand);
                    String lastShot = itemInHand.getOrCreateTag().getString("lastShot");

                    GUI.showExposureViewScreen(lastShot);
                }
            }
        }
        else {
//            if (player.getLevel().isClientSide) {
//            Minecraft.getInstance().gameRenderer.loadEffect(new ResourceLocation("exposure:shaders/post/orange_tint.json"));
                if (Viewfinder.isActive())
                    takeShot(player, usedHand);
                else
                    Viewfinder.setActive(true);
//            }
        }
    }

    protected void takeShot(Player player, InteractionHand usedHand) {
        Level level = player.level;

        level.playSound(player, player, SoundEvents.UI_LOOM_SELECT_PATTERN, SoundSource.PLAYERS, 1f,
                level.getRandom().nextFloat() * 0.2f + 1.1f);



        if (player.level.isClientSide) {
            String id = player.getName().getString() + "_" + level.getGameTime();

            Camera.capture(id);

            ItemStack itemInHand = player.getItemInHand(usedHand);
            itemInHand.getOrCreateTag().putString("lastShot", id);
            Packets.sendToServer(new ServerboundUpdateCameraPacket(id, usedHand));
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
