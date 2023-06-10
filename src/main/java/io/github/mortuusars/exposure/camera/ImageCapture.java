package io.github.mortuusars.exposure.camera;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.awt.image.BufferedImage;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Exposure.ID, value = Dist.CLIENT)
public class ImageCapture {
    public static boolean shouldCapture;
    public static boolean capturing;

    private static String id;

    private static int captureDelay;

    public boolean canCapture() {
        return !capturing;
    }

    public static void capture(String id) {
        ImageCapture.id = id;
        shouldCapture = true;

        Minecraft.getInstance().options.hideGui = true;
//        Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
        Minecraft.getInstance().options.gamma().set(1d);
        captureDelay = 20;

//        Minecraft.getInstance().level.setBlockAndUpdate(Minecraft.getInstance().player.blockPosition().above(), Blocks.LIGHT.defaultBlockState());
    }

    @SubscribeEvent
    public static void renderT(TickEvent.RenderTickEvent event) {
        if (!event.phase.equals(TickEvent.Phase.END))
            return;

        if (!ImageCapture.shouldCapture || ImageCapture.capturing)
            return;

        Minecraft mc = Minecraft.getInstance();

        if (captureDelay > 0) {
            captureDelay--;
            BlockPos pos = mc.player.blockPosition().above();
            mc.level.setBlock(pos, Blocks.LIGHT.defaultBlockState()
                    .setValue(LightBlock.WATERLOGGED, mc.level.getFluidState(pos).is(Fluids.WATER)), Block.UPDATE_NONE);
            return;
        }

        ImageCapture.capturing = true;
        ImageCapture.shouldCapture = false;

//        Screenshot.grab(mc.gameDirectory, mc.getMainRenderTarget(), (p_90917_) -> {
//        });

        NativeImage nativeImage = Screenshot.takeScreenshot(mc.getMainRenderTarget());

//        Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK);

        mc.options.hideGui = false;
        ImageCapture.saveImageThreaded(nativeImage);
    }

    public static void saveImageThreaded(NativeImage image) {
        new Thread(() -> {
            saveImage(image, id);
        }, "ImageSaveThread").start();
    }

    private static void saveImage(NativeImage nativeImage, String id) {
        BufferedImage bufferedImage = new BufferedImage(nativeImage.getWidth(), nativeImage.getHeight(), BufferedImage.TYPE_INT_RGB);

        float exposure = 1f;

        for (int x = 0; x < nativeImage.getWidth(); x++) {
            for (int y = 0; y < nativeImage.getHeight(); y++) {
                int rgba = nativeImage.getPixelRGBA(x, y);
                int alpha = (rgba >> 24) & 0xFF;
                int blue = (rgba >> 16) & 0xFF;
                int green = (rgba >> 8) & 0xFF;
                int red = rgba & 0xFF;



                red = Mth.clamp(((int) (red * exposure)), 0, 255);
                green = Mth.clamp(((int) (green * exposure)), 0, 255);
                blue = Mth.clamp(((int) (blue * exposure)), 0, 255);

//                int average = Math.min(255, (int)(((red + green + blue) / 3) * 0.5));

                bufferedImage.setRGB(x, y, alpha << 24 | red << 16 | green << 8 | blue);
//                bufferedImage.setRGB(x, y, alpha << 24 | average << 16 | average << 8 | average);
            }
        }
        new MapPhotography().saveImage(bufferedImage, id);

        capturing = false;

        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> mc.gameRenderer.shutdownEffect());
//        Minecraft.getInstance().gameRenderer.shutdownEffect();


        BlockPos pos = mc.player.blockPosition();

        AABB inflate = new AABB(pos).inflate(10);

        BlockPos.betweenClosedStream(inflate).forEach(p -> {
            if (mc.level.getBlockState(p).is(Blocks.LIGHT)) {
                BlockState state = mc.level.getFluidState(p).is(Fluids.WATER) ? Blocks.WATER.defaultBlockState()
                        .getFluidState()
                        .createLegacyBlock() : Blocks.AIR.defaultBlockState();
                mc.level.setBlockAndUpdate(p, state);
            }
        });

        mc.options.gamma().set(0.1d);
//        Minecraft.getInstance().level.setBlockAndUpdate(Minecraft.getInstance().player.blockPosition().above(), Blocks.AIR.defaultBlockState());

    }
}
