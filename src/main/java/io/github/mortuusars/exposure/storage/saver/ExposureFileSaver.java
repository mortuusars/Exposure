package io.github.mortuusars.exposure.storage.saver;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.config.Config;
import io.github.mortuusars.exposure.util.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@OnlyIn(Dist.CLIENT)
public class ExposureFileSaver implements IExposureSaver {

    private final String folder;
    private final boolean levelNameSubfolder;

    public ExposureFileSaver(String folder, boolean levelNameSubfolder) {
        this.folder = folder;
        this.levelNameSubfolder = levelNameSubfolder;
    }

    public static ExposureFileSaver withDefaultFolders() {
        return new ExposureFileSaver(Config.Client.EXPOSURE_SAVE_PATH.get(), Config.Client.EXPOSURE_SAVE_LEVEL_SUBFOLDER.get());
    }

    public void save(String id, byte[] materialColorPixels, int width, int height) {
        BufferedImage img = convertToBufferedImage(materialColorPixels, width, height);

        File outputFile = new File(folder + "/" + (levelNameSubfolder ? getLevelName() + "/" : "") + id + ".png");
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
