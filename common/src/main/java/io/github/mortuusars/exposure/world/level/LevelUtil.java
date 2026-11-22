package io.github.mortuusars.exposure.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.List;
import java.util.Objects;

public class LevelUtil {
    public static int getLightLevelAt(Level level, BlockPos pos) {
        level.updateSkyBrightness(); // This updates 'getSkyDarken' on the client. It'll return 0 if we don't update it.
        int skyBrightness = level.getBrightness(LightLayer.SKY, pos);
        int blockBrightness = level.getBrightness(LightLayer.BLOCK, pos);
        return skyBrightness < 15 ?
                Math.max(blockBrightness, (int) (skyBrightness * ((15 - level.getSkyDarken()) / 15f))) :
                Math.max(blockBrightness, 15 - level.getSkyDarken());
    }

    public static List<Identifier> getStructuresAt(ServerLevel level, BlockPos pos) {
        Registry<Structure> registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);

        return level.structureManager().getAllStructuresAt(pos).keySet().stream()
                .filter(structure -> {
                    StructureStart structureStart = level.structureManager().getStructureAt(pos, structure);
                    return structureStart.isValid();
                })
                .map(registry::getKey)
                .filter(Objects::nonNull)
                .toList();
    }
}
