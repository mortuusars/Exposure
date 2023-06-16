package io.github.mortuusars.exposure.camera;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.storage.ExposureImageConverter;
import io.github.mortuusars.exposure.storage.ExposureSavedData;
import io.github.mortuusars.exposure.storage.ExposureStorage;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Exposure.ID, value = Dist.CLIENT)
public class Camera {
    private static boolean capturing;
    private static boolean processing;
    private static int captureDelay;
    private static String exposureId;
    private static boolean hideGuiBeforeCapture;
    private static CameraType cameraTypeBeforeCapture;

    public static boolean isProcessing() {
        return processing;
    }

    public static void capture(String id) {
        exposureId = id;
        capturing = true;
        captureDelay = 0;
        hideGuiBeforeCapture = Minecraft.getInstance().options.hideGui;
        cameraTypeBeforeCapture = Minecraft.getInstance().options.getCameraType();

        Minecraft.getInstance().options.hideGui = true;
        Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (!event.phase.equals(TickEvent.Phase.END) || !capturing || processing)
            return;

        if (captureDelay > 0) {
            captureDelay--;

            //TODO: Flash light

            return;
        }

        NativeImage screenshot = Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget());
        capturing = false;

        Minecraft.getInstance().options.hideGui = hideGuiBeforeCapture;
        Minecraft.getInstance().options.setCameraType(cameraTypeBeforeCapture);

        processing = true;

        processAndSaveImageThreaded(screenshot, exposureId);
    }

    private static void processAndSaveImageThreaded(NativeImage nativeImage, String id) {
        new Thread(() -> processAndSaveImage(nativeImage, id), "ProcessingAndSavingImage").start();
    }

    private static void processAndSaveImage(NativeImage screenshotImage, String id) {
//        BufferedImage bufferedImage = new BufferedImage(screenshotImage.getWidth(), screenshotImage.getHeight(), BufferedImage.TYPE_INT_RGB);
//
//        float exposure = 1f;
//
//        for (int x = 0; x < screenshotImage.getWidth(); x++) {
//            for (int y = 0; y < screenshotImage.getHeight(); y++) {
//                int rgba = screenshotImage.getPixelRGBA(x, y);
//                int alpha = (rgba >> 24) & 0xFF;
//                int blue = (rgba >> 16) & 0xFF;
//                int green = (rgba >> 8) & 0xFF;
//                int red = rgba & 0xFF;
//
//                red = Mth.clamp(((int) (red * exposure)), 0, 255);
//                green = Mth.clamp(((int) (green * exposure)), 0, 255);
//                blue = Mth.clamp(((int) (blue * exposure)), 0, 255);
//
//                // Apply the luma conversion formula
////                int luma = (int) (0.299 * red + 0.587 * green + 0.114 * blue);
////                int average = Math.min(255, (int)(((red + green + blue) / 3) * 0.5));
//
//                bufferedImage.setRGB(x, y, alpha << 24 | red << 16 | green << 8 | blue);
////                bufferedImage.setRGB(x, y, alpha << 24 | average << 16 | average << 8 | average);
////                bufferedImage.setRGB(x, y, alpha << 24 | luma << 16 | luma << 8 | luma);
//            }
//        }


        int sWidth = screenshotImage.getWidth();
        int sHeight = screenshotImage.getHeight();

        int screenshotMinSize = Math.min(screenshotImage.getWidth(), screenshotImage.getHeight());
        int screenshotXStart = sWidth > sHeight ? (sWidth - sHeight) / 2 : 0;
        int screenshotYStart = sHeight > sWidth ? (sHeight - sWidth) / 2 : 0;

        int size = 384;

        BufferedImage bufferedImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < bufferedImage.getWidth(); x++) {
            float screenshotX = screenshotMinSize * (x / (float)size);
            int nx = Mth.clamp((int)screenshotX + screenshotXStart, screenshotXStart, screenshotXStart + screenshotMinSize);

            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                float screenshotY = screenshotMinSize * (y / (float)size);
                int ny = Mth.clamp((int)screenshotY + screenshotYStart, screenshotYStart, screenshotYStart + screenshotMinSize);

                int rgba = screenshotImage.getPixelRGBA(nx, ny);
                int alpha = (rgba >> 24) & 0xFF;
                int red = (rgba >> 16) & 0xFF;
                int green = (rgba >> 8) & 0xFF;
                int blue = rgba & 0xFF;

//                red = Mth.clamp(((int) (red * exposure)), 0, 255);
//                green = Mth.clamp(((int) (green * exposure)), 0, 255);
//                blue = Mth.clamp(((int) (blue * exposure)), 0, 255);

                bufferedImage.setRGB(x, y, alpha << 24 | blue << 16 | green << 8 | red);
            }
        }

        //TODO: Blur
//        bufferedImage = Blur.applyGaussianBlur(bufferedImage, 32);
//        bufferedImage = bufferedImage.getSubimage(32, 32, 256 - 32, 256 - 32);


        bufferedImage = FloydDither.dither(bufferedImage);


        // Save the dithered image
        File outputFile = new File("exposures/" + getLevelName() + "/" + id + ".png"); //TODO: world subfolder
        try {
            outputFile.mkdirs();
            ImageIO.write(bufferedImage, "png", outputFile);
        } catch (IOException e) {
            Exposure.LOGGER.error(e.toString());
        }


        byte[] bytes = ExposureImageConverter.convert(bufferedImage);

        ExposureSavedData exposureSavedData = new ExposureSavedData(bufferedImage.getWidth(), bufferedImage.getHeight(), bytes);

        ExposureStorage.storeClientsideAndSendToServer(id, exposureSavedData);

//        ExposureStorage.CLIENT.put(id, exposureSavedData);

//        new ExposureSender().sendToServer(id, exposureSavedData);



//        Packets.sendToServer(new ServerboundSaveExposurePacket(id, exposureSavedData));
//        ExposureStorage.save(id, exposureSavedData);


//        new MapPhotography().saveImage(bufferedImage, id);

//        String[][] parts = new MapDataPhotoStore().saveImage(bufferedImage, "photo_0");

        processing = false;

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
