package io.github.mortuusars.exposure.client.export;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.PalettedImage;
import io.github.mortuusars.exposure.client.util.LevelNameGetter;
import io.github.mortuusars.exposure.util.color.Color;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Function;

public class ImageExporter {
    protected final Image image;
    protected final String fileName;
    protected String folder = "exposures";
    @Nullable
    protected String worldName = null;
    protected long creationUnixTimestamp = 0;
    protected Function<Image, Image> imageModifier = Function.identity();
    protected Consumer<File> onExport = f -> {};

    public ImageExporter(Image image, String fileName) {
        this.image = image;
        this.fileName = fileName;
    }

    public ImageExporter(ExposureData exposure, String fileName) {
        this(PalettedImage.fromExposure(exposure), fileName);
    }

    public Function<Image, Image> getModifier() { return imageModifier; }
    public String getFileName() { return fileName; }
    public String getFolder() { return folder; }
    public @Nullable String getWorldSubfolder() { return worldName; }
    public long getCreationUnixTimestamp() { return creationUnixTimestamp; }

    public ImageExporter modify(Function<Image, Image> imageModifier) {
        this.imageModifier = imageModifier;
        return this;
    }

    public ImageExporter withFolder(String folder) {
        this.folder = folder;
        return this;
    }

    public ImageExporter toExposuresFolder() {
        this.folder = "exposures";
        return this;
    }

    public ImageExporter organizeByWorld(@Nullable String worldName) {
        this.worldName = worldName;
        return this;
    }

    public ImageExporter organizeByWorld(boolean organize) {
        this.worldName = organize ? LevelNameGetter.getWorldName() : null;
        return this;
    }

    public ImageExporter setCreationDate(long unixTimestamp) {
        this.creationUnixTimestamp = unixTimestamp;
        return this;
    }

    public void export() {
        save(imageModifier.apply(image));
    }

    protected boolean save(Image image) {
        try (NativeImage nativeImage = convertToNativeImage(image)) {
            String filepath = getFolder() + "/" + (getWorldSubfolder() != null ? getWorldSubfolder() + "/" : "") + getFileName() + ".png";
            File outputFile = new File(filepath);
            boolean ignored = outputFile.getParentFile().mkdirs();

            nativeImage.writeToFile(outputFile);

            if (creationUnixTimestamp > 0) {
                trySetFileCreationDate(outputFile.getAbsolutePath(), creationUnixTimestamp);
            }

            onExport.accept(outputFile);

            Exposure.LOGGER.info("Exposure saved: {}", outputFile);
            return true;
        }
        catch (Exception e) {
            Exposure.LOGGER.error("Failed to save exposure to file: {}", e.toString());
            return false;
        }
    }

    protected NativeImage convertToNativeImage(Image image) {
        NativeImage nativeImage = new NativeImage(image.width(), image.height(), false);

        for (int x = 0; x < nativeImage.getWidth(); x++) {
            for (int y = 0; y < nativeImage.getHeight(); y++) {
                nativeImage.setPixel(x, y, image.getPixelARGB(x, y));
            }
        }

        return nativeImage;
    }

    protected void trySetFileCreationDate(String filePath, long creationTimeUnixSeconds) {
        try {
            Date creationDate = Date.from(Instant.ofEpochSecond(creationTimeUnixSeconds));

            BasicFileAttributeView attributes = Files.getFileAttributeView(Paths.get(filePath), BasicFileAttributeView.class);
            FileTime creationTime = FileTime.fromMillis(creationDate.getTime());
            FileTime modifyTime = FileTime.fromMillis(System.currentTimeMillis());
            attributes.setTimes(modifyTime, modifyTime, creationTime);
        }
        catch (Exception ignored) { }
    }

    public ImageExporter onExport(Consumer<File> onExport) {
        this.onExport = onExport;
        return this;
    }
}
