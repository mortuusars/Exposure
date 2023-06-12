package io.github.mortuusars.exposure.item;

import io.github.mortuusars.exposure.camera.Camera;
import io.github.mortuusars.exposure.camera.PhotoScreen;
import io.github.mortuusars.exposure.camera.viewfinder.Viewfinder;
import net.minecraft.client.Minecraft;
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
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        useCamera(player, usedHand);
        return super.use(level, player, usedHand);
    }

    protected void useCamera(Player player, InteractionHand usedHand) {
        if (player.isSecondaryUseActive()) {
            if (player.getLevel().isClientSide) {
                if (Viewfinder.isActive())
                    Viewfinder.setActive(false);
                else
                    Minecraft.getInstance().setScreen(new PhotoScreen("photo_0", 4));
            }
        }
        else {
            takeShot(player, usedHand);
        }
    }

    protected void takeShot(Player player, InteractionHand usedHand) {
        Level level = player.level;

        level.playSound(player, player, SoundEvents.UI_LOOM_SELECT_PATTERN, SoundSource.PLAYERS, 1f,
                level.getRandom().nextFloat() * 0.2f + 0.9f);

//        boolean useFlash = true;
//
//        if (useFlash) {
//            BlockPos initialFlashPos = player.blockPosition().above();
//
////            if (level.getBlockState(initialFlashPos))
//
//
//        }

        if (level.isClientSide) {
//            Minecraft.getInstance().gameRenderer.loadEffect(new ResourceLocation("exposure:shaders/post/orange_tint.json"));
            if (Viewfinder.isActive())
                Camera.capture();
            else
                Viewfinder.setActive(true);
        }
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.SPYGLASS;
    }
}
