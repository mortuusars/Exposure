package io.github.mortuusars.exposure.camera;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.modifier.IExposureModifier;
import io.github.mortuusars.exposure.camera.processing.FloydDither;
import io.github.mortuusars.exposure.storage.saver.IExposureSaver;
import io.github.mortuusars.exposure.util.ColorUtils;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Queue;

@OnlyIn(Dist.CLIENT)
public class CameraCapture {
    public static float additionalBrightness = 0f;

    private static final Queue<Capture> captureQueue = new LinkedList<>();
    @Nullable
    private static Capture currentCapture;
    private static boolean capturing;
    private static boolean processing;
    private static int captureDelay;
    private static boolean hideGuiBeforeCapture;
    private static CameraType cameraTypeBeforeCapture;

    public static float getModifiedBrightness(float originalBrightness) {
        return originalBrightness + additionalBrightness;
    }

    public static boolean isCapturing() {
        return capturing;
    }

    public static boolean isProcessing() {
        return processing;
    }

    public static void enqueueCapture(Capture capture) {
        captureQueue.add(capture);
    }

    private static void capture(Capture capture) {
        capturing = true;
        currentCapture = capture;
        hideGuiBeforeCapture = Minecraft.getInstance().options.hideGui;
        cameraTypeBeforeCapture = Minecraft.getInstance().options.getCameraType();

        Minecraft.getInstance().options.hideGui = true;
        Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);

        captureDelay = 0;

        for (IExposureModifier modifier : capture.modifiers) {
            captureDelay = Math.max(captureDelay, modifier.getCaptureDelay(capture));
            modifier.setup(capture);
        }
    }

    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (!isCapturing() && !captureQueue.isEmpty()) {
            capture(captureQueue.poll());
            return;
        }

        if (!event.phase.equals(TickEvent.Phase.END) || !capturing || currentCapture == null)
            return;

        if (captureDelay > 0) {
            captureDelay--;

            for (IExposureModifier modifier : currentCapture.modifiers) {
                modifier.onSetupDelayTick(currentCapture, captureDelay);
            }

            return;
        }

        NativeImage screenshot = Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget());

//        try {
//            BufferedImage read = ImageIO.read(new File("E:/img.jpg"));
//
//            screenshot = new NativeImage(read.getWidth(), read.getHeight(), false);
//
//            for (int x = 0; x < read.getWidth(); x++) {
//                for (int y = 0; y < read.getHeight(); y++) {
//                    screenshot.setPixelRGBA(x, y, ColorUtils.BGRtoRGB(read.getRGB(x, y)));
//                }
//            }
//
//        } catch (IOException e) {
//            Exposure.LOGGER.error(e.toString());
//        }

        Minecraft.getInstance().options.hideGui = hideGuiBeforeCapture;
        Minecraft.getInstance().options.setCameraType(cameraTypeBeforeCapture);

//        processing = true;

//        for (int size : new int[] { 192, 256, 320, 384, 448}) {
//            processAndSaveImageThreaded(screenshot, new Capture(currentCapture.camera, currentCapture.id + "_" + size,
//                    size, currentCapture.cropFactor, currentCapture.shutterSpeed, currentCapture.modifiers));
//        }

        processAndSaveImageThreaded(screenshot, currentCapture);

        capturing = false;
        currentCapture = null;
    }

    private static void processAndSaveImageThreaded(NativeImage nativeImage, Capture properties) {
        new Thread(() -> processAndSaveImage(nativeImage, properties), "ProcessingAndSavingExposure").start();
    }

    private static void processAndSaveImage(NativeImage screenshotImage, Capture properties) {
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

    private static BufferedImage scaleCropAndProcess(NativeImage sourceImage, Capture properties) {
        //TODO: Aspect ratios
        int sWidth = sourceImage.getWidth();
        int sHeight = sourceImage.getHeight();

        int sourceSize = Math.min(sWidth, sHeight);
        float crop = sourceSize - (sourceSize / properties.cropFactor);
        sourceSize -= crop;

        int sourceXStart = sWidth > sHeight ? (sWidth - sHeight) / 2 : 0;
        int sourceYStart = sHeight > sWidth ? (sHeight - sWidth) / 2 : 0;

        sourceXStart += crop / 2;
        sourceYStart += crop / 2;

        int width = properties.width;
        int height = properties.height;

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            float sourceX = sourceSize * (x / (float)width);
            int sx = Mth.clamp((int)sourceX + sourceXStart, sourceXStart, sourceXStart + sourceSize);

            for (int y = 0; y < height; y++) {
                float sourceY = sourceSize * (y / (float)height);
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

    private static void saveExposure(Capture properties, byte[] materialColorPixels) {
        for (IExposureSaver saver : properties.savers) {
            saver.save(properties.id, materialColorPixels, properties.width, properties.height);
        }
    }
}

