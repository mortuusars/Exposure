package io.github.mortuusars.exposure.camera;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.storage.ExposureImageConverter;
import io.github.mortuusars.exposure.storage.ExposureSavedData;
import io.github.mortuusars.exposure.storage.ExposureStorage;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Exposure.ID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class Camera {
    private static CaptureProperties captureProperties;

    private static boolean capturing;
    private static boolean processing;
    private static int captureDelay;
    private static boolean hideGuiBeforeCapture;
    private static CameraType cameraTypeBeforeCapture;

    public static boolean isCapturing() {
        return capturing;
    }

    public static boolean isProcessing() {
        return processing;
    }

    public static void capture(CaptureProperties properties) {
        captureProperties = properties;

        capturing = true;
        hideGuiBeforeCapture = Minecraft.getInstance().options.hideGui;
        cameraTypeBeforeCapture = Minecraft.getInstance().options.getCameraType();

        Minecraft.getInstance().options.hideGui = true;
        Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);

        captureDelay = 0;

        for (IExposureModifier modifier : captureProperties.modifiers) {
            captureDelay = Math.max(captureDelay, modifier.getCaptureDelay(captureProperties));
            modifier.setup(properties);
        }
    }

    public static float modifyBrightness(float originalBrightness) {
        return originalBrightness; // TODO: brighten darks a little to not leave black spots on longer exposures.
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (!event.phase.equals(TickEvent.Phase.END) || !capturing || processing)
            return;

        if (captureDelay > 0) {
            captureDelay--;

            //TODO: Flash light
            for (IExposureModifier modifier : captureProperties.modifiers) {
                modifier.onSetupDelayTick(captureProperties);
            }

            return;
        }

        NativeImage screenshot = Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget());
        capturing = false;

        Minecraft.getInstance().options.hideGui = hideGuiBeforeCapture;
        Minecraft.getInstance().options.setCameraType(cameraTypeBeforeCapture);

        processing = true;

        processAndSaveImageThreaded(screenshot, captureProperties);
    }

    private static void processAndSaveImageThreaded(NativeImage nativeImage, CaptureProperties properties) {
        new Thread(() -> processAndSaveImage(nativeImage, properties), "ProcessingAndSavingExposure").start();
    }

    private static void processAndSaveImage(NativeImage screenshotImage, CaptureProperties properties) {
        String id = properties.id;

        BufferedImage bufferedImage;
        bufferedImage = scaleCropAndProcess(screenshotImage, properties.size, properties.cropFactor, properties);

        for (IExposureModifier modifier : properties.modifiers) {
            bufferedImage = modifier.modifyImage(properties, bufferedImage);
        }

        bufferedImage = FloydDither.dither(bufferedImage);

        for (IExposureModifier modifier : properties.modifiers) {
            modifier.teardown(properties);
        }

        // Save the dithered image
        File outputFile = new File("exposures/" + getLevelName() + "/" + id + ".png");
        try {
            outputFile.mkdirs();
            ImageIO.write(bufferedImage, "png", outputFile);
        } catch (IOException e) {
            Exposure.LOGGER.error(e.toString());
        }


        byte[] bytes = ExposureImageConverter.convert(bufferedImage);

        ExposureSavedData exposureSavedData = new ExposureSavedData(bufferedImage.getWidth(), bufferedImage.getHeight(), bytes);

        ExposureStorage.storeClientsideAndSendToServer(id, exposureSavedData);

        processing = false;

        for (IExposureModifier modifier : properties.modifiers) {
            modifier.end(properties);
        }

//        Minecraft mc = Minecraft.getInstance();
//        mc.execute(() -> mc.gameRenderer.shutdownEffect());
//        Minecraft.getInstance().gameRenderer.shutdownEffect();


//        BlockPos pos = mc.player.blockPosition();
//
//        AABB inflate = new AABB(pos).inflate(10);
//
//        BlockPos.betweenClosedStream(inflate).forEach(p -> {
//            if (mc.level.getBlockState(p).is(Blocks.LIGHT)) {
//                BlockState state = mc.level.getFluidState(p).is(Fluids.WATER) ? Blocks.WATER.defaultBlockState()
//                        .getFluidState()
//                        .createLegacyBlock() : Blocks.AIR.defaultBlockState();
//                mc.level.setBlockAndUpdate(p, state);
//            }
//        });
//
//        mc.options.gamma().set(0.1d);
//        Minecraft.getInstance().level.setBlockAndUpdate(Minecraft.getInstance().player.blockPosition().above(), Blocks.AIR.defaultBlockState());

    }

    private static BufferedImage scaleCropAndProcess(NativeImage sourceImage, int size, float cropFactor, CaptureProperties properties) {
        int sWidth = sourceImage.getWidth();
        int sHeight = sourceImage.getHeight();

        int sourceSize = Math.min(sWidth, sHeight);
        float crop = sourceSize - (sourceSize / cropFactor);
        sourceSize -= crop;

        int sourceXStart = sWidth > sHeight ? (sWidth - sHeight) / 2 : 0;
        int sourceYStart = sHeight > sWidth ? (sHeight - sWidth) / 2 : 0;

        sourceXStart += crop / 2;
        sourceYStart += crop / 2;

        BufferedImage bufferedImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < size; x++) {
            float sourceX = sourceSize * (x / (float)size);
            int sx = Mth.clamp((int)sourceX + sourceXStart, sourceXStart, sourceXStart + sourceSize);

            for (int y = 0; y < size; y++) {
                float sourceY = sourceSize * (y / (float)size);
                int sy = Mth.clamp((int)sourceY + sourceYStart, sourceYStart, sourceYStart + sourceSize);

                int rgba = sourceImage.getPixelRGBA(sx, sy);
                int red = (rgba >> 16) & 0xFF;
                int green = (rgba >> 8) & 0xFF;
                int blue = rgba & 0xFF;

                Vec3i pixel = new Vec3i(red, green, blue);

                for (IExposureModifier modifier : properties.modifiers) {
                    pixel = modifier.modifyPixel(properties, pixel.getX(), pixel.getY(), pixel.getZ());
                }

                bufferedImage.setRGB(x, y, 0xFF << 24 | pixel.getZ() << 16 | pixel.getY() << 8 | pixel.getX());
            }
        }

        return bufferedImage;
    }

    private static String getLevelName() {
        try{
            if (Minecraft.getInstance().getCurrentServer() == null){
                String gameDirectory = Minecraft.getInstance().gameDirectory.getAbsolutePath();
                Path savesDir = Path.of(gameDirectory, "/saves");

                File[] dirs = savesDir.toFile().listFiles((dir, name) -> new File(dir, name).isDirectory());

                if (dirs == null || dirs.length == 0)
                    return "";

                File lastModified = dirs[0];

                for (File dir : dirs) {
                    if (dir.lastModified() > lastModified.lastModified())
                        lastModified = dir;
                }

                return lastModified.getName();
            }
            else {
                return Minecraft.getInstance().getCurrentServer().name;
            }
        }
        catch (Exception e){
            Exposure.LOGGER.error("Failed to get level name: " + e);
            return "";
        }
    }
}
