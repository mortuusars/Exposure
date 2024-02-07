package io.github.mortuusars.exposure.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public class LevelUtil {
    public static int getLightLevelAt(Level level, BlockPos pos) {
        level.updateSkyBrightness(); // This updates 'getSkyDarken' on the client. Otherwise, it always returns 0.
        int skyBrightness = level.getBrightness(LightLayer.SKY, pos);
        int blockBrightness = level.getBrightness(LightLayer.BLOCK, pos);
        return skyBrightness < 15 ?
                Math.max(blockBrightness, (int) (skyBrightness * ((15 - level.getSkyDarken()) / 15f))) :
                Math.max(blockBrightness, 15 - level.getSkyDarken());
    }
}
