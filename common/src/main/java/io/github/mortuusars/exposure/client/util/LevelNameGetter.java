package io.github.mortuusars.exposure.client.util;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.nio.file.Path;

public class LevelNameGetter {
    public static String getWorldName() {
        try {
            if (Minecraft.getInstance().isSingleplayer()) {
                if (Minecraft.getInstance().getSingleplayerServer() != null)
                    return Minecraft.getInstance().getSingleplayerServer().getWorldData().getLevelName()
                            .replace('.', '_'); // Folder name has underscores instead of dots.
                else {
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
            }
            else if (Minecraft.getInstance().getCurrentServer() != null) {
                return Minecraft.getInstance().getCurrentServer().name;
            }
            else {
                return "Unknown";
            }
        } catch (Exception e) {
            Exposure.LOGGER.error("Failed to get level name: ", e);
            return "Unknown";
        }
    }
}
