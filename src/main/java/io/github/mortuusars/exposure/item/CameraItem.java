package io.github.mortuusars.exposure.item;

import io.github.mortuusars.exposure.camera.ImageCapture;
import io.github.mortuusars.exposure.camera.PhotoScreen;
import io.github.mortuusars.exposure.event.ForgeEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

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
    public InteractionResult useOn(UseOnContext context) {
        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        useCamera(player, usedHand);
        return super.use(level, player, usedHand);
    }

    protected void useCamera(Player player, InteractionHand usedHand) {
        if (player.isSecondaryUseActive()) {
            if (player.getLevel().isClientSide) {
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

        boolean useFlash = true;

        if (useFlash) {
            BlockPos initialFlashPos = player.blockPosition().above();

//            if (level.getBlockState(initialFlashPos))


        }

        if (level.isClientSide) {
//            Minecraft.getInstance().level.setBlockAndUpdate(player.blockPosition().above(), Blocks.LIGHT.defaultBlockState());
//            Minecraft.getInstance().level.getLightEngine().onBlockEmissionIncrease(player.blockPosition().above(), 15);
//            Minecraft.getInstance().level.getLightEngine().runUpdates(Integer.MAX_VALUE, true, true);
//            Minecraft.getInstance().level.setLightReady(player.blockPosition().getX(), player.blockPosition().getZ());
//            Minecraft.getInstance().level.pollLightUpdates();
//            Minecraft.getInstance().gameRenderer.loadEffect(new ResourceLocation("exposure:shaders/post/orange_tint.json"));
            ImageCapture.capture("photo_0");
        }
    }
}
