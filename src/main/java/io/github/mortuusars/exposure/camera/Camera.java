package io.github.mortuusars.exposure.camera;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.storage.saver.ExposureFileSaver;
import io.github.mortuusars.exposure.storage.saver.ExposureStorageSaver;
import io.github.mortuusars.exposure.util.ColorUtils;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;
import java.awt.image.BufferedImage;

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
        try {
            BufferedImage bufferedImage = scaleCropAndProcess(screenshotImage, properties);

            for (IExposureModifier modifier : properties.modifiers) {
                bufferedImage = modifier.modifyImage(properties, bufferedImage);
            }

            byte[] materialColorPixels = FloydDither.dither(bufferedImage);

            for (IExposureModifier modifier : properties.modifiers) {
                modifier.teardown(properties);
            }

            saveExposure(properties, materialColorPixels);
        }
        catch (Exception e) {
            Exposure.LOGGER.error(e.toString());
        }
        finally {
            processing = false;

            for (IExposureModifier modifier : properties.modifiers) {
                modifier.end(properties);
            }
        }
    }

    private static BufferedImage scaleCropAndProcess(NativeImage sourceImage, CaptureProperties properties) {
        int sWidth = sourceImage.getWidth();
        int sHeight = sourceImage.getHeight();

        int sourceSize = Math.min(sWidth, sHeight);
        float crop = sourceSize - (sourceSize / captureProperties.cropFactor);
        sourceSize -= crop;

        int sourceXStart = sWidth > sHeight ? (sWidth - sHeight) / 2 : 0;
        int sourceYStart = sHeight > sWidth ? (sHeight - sWidth) / 2 : 0;

        sourceXStart += crop / 2;
        sourceYStart += crop / 2;

        int outputSize = properties.size;

        BufferedImage bufferedImage = new BufferedImage(outputSize, outputSize, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < outputSize; x++) {
            float sourceX = sourceSize * (x / (float)outputSize);
            int sx = Mth.clamp((int)sourceX + sourceXStart, sourceXStart, sourceXStart + sourceSize);

            for (int y = 0; y < outputSize; y++) {
                float sourceY = sourceSize * (y / (float)outputSize);
                int sy = Mth.clamp((int)sourceY + sourceYStart, sourceYStart, sourceYStart + sourceSize);

                int rgba = ColorUtils.BGRtoRGB(sourceImage.getPixelRGBA(sx, sy)); // Mojang decided to return BGR in getPixelRGBA method.
                Color pixel = new Color(rgba, false);

                for (IExposureModifier modifier : properties.modifiers) {
                    pixel = modifier.modifyPixel(properties, pixel.getRed(), pixel.getGreen(), pixel.getBlue());
                }

                bufferedImage.setRGB(x, y, 0xFF << 24 | pixel.getRed() << 16 | pixel.getGreen() << 8 | pixel.getBlue());
            }
        }

        return bufferedImage;
    }

    private static void saveExposure(CaptureProperties properties, byte[] materialColorPixels) {
        new ExposureFileSaver().save(properties.id, materialColorPixels, properties.size, properties.size);
        new ExposureStorageSaver().save(properties.id, materialColorPixels, properties.size, properties.size);
    }
}
