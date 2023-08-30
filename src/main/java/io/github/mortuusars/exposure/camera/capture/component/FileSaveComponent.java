package io.github.mortuusars.exposure.camera.capture.component;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.capture.Capture;
import io.github.mortuusars.exposure.config.Config;
import io.github.mortuusars.exposure.util.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.material.MaterialColor;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class FileSaveComponent implements ICaptureComponent {
    private final String exposureId;
    private final String folder;
    private final boolean levelNameSubfolder;

    public FileSaveComponent(String exposureId, String folder, boolean levelNameSubfolder) {
        this.exposureId = exposureId;
        this.folder = folder;
        this.levelNameSubfolder = levelNameSubfolder;
    }

    public static FileSaveComponent withDefaultFolders(String exposureId) {
        return new FileSaveComponent(exposureId, Config.Client.EXPOSURE_SAVE_PATH.get(), Config.Client.EXPOSURE_SAVE_LEVEL_SUBFOLDER.get());
    }

    @Override
    public void save(Capture capture, byte[] materialColorPixels, int width, int height) {
        BufferedImage img = convertToBufferedImage(materialColorPixels, width, height);

        File outputFile = new File(folder + "/" + (levelNameSubfolder ? getLevelName() + "/" : "") + exposureId + ".png");
        try {
            //noinspection ResultOfMethodCallIgnored
            outputFile.mkdirs();
            ImageIO.write(img, "png", outputFile);
        } catch (IOException e) {
            Exposure.LOGGER.error("Exposure file was not saved: " + e);
        }
    }

    @NotNull
    private BufferedImage convertToBufferedImage(byte[] materialColorPixels, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int bgr = MaterialColor.getColorFromPackedId(materialColorPixels[x + y * width]); // Mojang returns BGR color
                int rgb = ColorUtils.BGRtoRGB(bgr);
                img.setRGB(x, y, rgb);
            }
        }

        return img;
    }

    private String getLevelName() {
        try {
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
