package io.github.mortuusars.exposure.camera.film;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FrameData {
    public String id;
    public FilmType filmType;
    public String photographer;
    public String timestamp;
    public long dayTimeTicks;
    public BlockPos shotPosition;
    @Nullable
    public ResourceLocation dimension;
    @Nullable
    public ResourceLocation biome;
    public boolean flash;
    public List<EntityInfo> entitiesInFrame;

    public static final FrameData EMPTY = new FrameData("", FilmType.COLOR, "", "", -1, BlockPos.ZERO,
            null, null, false, Collections.emptyList());

    public FrameData(String id, FilmType filmType, String photographer, String timestamp, long dayTimeTicks, BlockPos shotPosition, @Nullable ResourceLocation dimension,
                     @Nullable ResourceLocation biome, boolean flash, List<EntityInfo> entitiesInFrame) {
        this.id = id;
        this.filmType = filmType;
        this.photographer = photographer;
        this.timestamp = timestamp;
        this.dayTimeTicks = dayTimeTicks;
        this.shotPosition = shotPosition;
        this.dimension = dimension;
        this.biome = biome;
        this.flash = flash;
        this.entitiesInFrame = entitiesInFrame;
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putString("Id", id);

        tag.putString("FilmType", filmType.getSerializedName());

        if (photographer.length() > 0)
            tag.putString("Photographer", photographer);

        if (timestamp.length() > 0)
            tag.putString("Timestamp", timestamp);

        if (dayTimeTicks >= 0)
            tag.putLong("DayTime", dayTimeTicks);

        if (!shotPosition.equals(BlockPos.ZERO)) {
            ListTag pos = new ListTag();
            pos.add(IntTag.valueOf(shotPosition.getX()));
            pos.add(IntTag.valueOf(shotPosition.getY()));
            pos.add(IntTag.valueOf(shotPosition.getZ()));
            tag.put("Pos", pos);
        }

        if (dimension != null)
            tag.putString("Dimension", dimension.toString());

        if (biome != null)
            tag.putString("Biome", biome.toString());

        if (flash)
            tag.putBoolean("Flash", true);

        if (entitiesInFrame.size() > 0) {
            ListTag entities = new ListTag();
            for (EntityInfo entityInfo : entitiesInFrame) {
                entities.add(entityInfo.save(new CompoundTag()));
                // Duplicate entity id as a separate field in the tag.
                // Can then be used by FTBQuests nbt matching (it's hard to match from a list), for example.
                tag.putBoolean(entityInfo.typeId.toString(), true);
            }
            tag.put("Entities", entities);
        }

        return tag;
    }

    public static FrameData load(CompoundTag tag) {
        String id = tag.getString("Id");
        if (id.length() == 0) {
            Exposure.LOGGER.error("Cannot load exposure frame: id is not valid. Tag: " + tag);
            return EMPTY;
        }

        @Nullable FilmType type = FilmType.byName(tag.getString("FilmType"));
        FilmType filmType = type != null ? type : FilmType.COLOR;

        String shooterName = tag.getString("Photographer");
        String timestamp = tag.getString("Timestamp");
        long dayTime = tag.getLong("DayTime");

        ListTag posTag = tag.getList("Pos", Tag.TAG_INT);
        BlockPos pos = posTag.size() == 3 ?
                new BlockPos(posTag.getInt(0), posTag.getInt(1), posTag.getInt(2))
                : BlockPos.ZERO;

        ResourceLocation dimension = null;
        if (tag.contains("Dimension", Tag.TAG_STRING)) {
            String dimensionStr = tag.getString("Dimension");
            if (dimensionStr.length() > 0) {
                dimension = new ResourceLocation(dimensionStr);
            }
        }

        ResourceLocation biome = null;
        if (tag.contains("Biome", Tag.TAG_STRING)) {
            String biomeStr = tag.getString("Biome");
            if (biomeStr.length() > 0) {
                biome = new ResourceLocation(biomeStr);
            }
        }

        boolean flash = tag.getBoolean("Flash");

        ListTag entities = tag.getList("Entities", Tag.TAG_COMPOUND);
        List<EntityInfo> entitiesInFrame = new ArrayList<>();
        for (Tag entityInfoTag : entities) {
            entitiesInFrame.add(EntityInfo.fromTag(((CompoundTag) entityInfoTag)));
        }

        return new FrameData(id, filmType, shooterName, timestamp, dayTime, pos, dimension, biome, flash, entitiesInFrame);
    }

    public static class EntityInfo {
        public ResourceLocation typeId;
        public CompoundTag data;

        public EntityInfo(ResourceLocation typeId, CompoundTag data) {
            this.typeId = typeId;
            this.data = data;
        }

        public CompoundTag save(CompoundTag tag) {
            tag.putString("Id", typeId.toString());
            tag.put("Data", this.data);
            return tag;
        }

        public static EntityInfo fromTag(CompoundTag tag) {
            return new EntityInfo(new ResourceLocation(tag.getString("Id")), tag.getCompound("Data"));
        }
    }
}
