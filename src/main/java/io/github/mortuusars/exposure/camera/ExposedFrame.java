package io.github.mortuusars.exposure.camera;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExposedFrame {
    public String id;
    public String shooterName;
    public String timestamp;
    public Vec3 shotPosition;
    @Nullable
    public ResourceLocation dimension;
    @Nullable
    public ResourceLocation biome;
    public List<EntityInfo> entitiesInFrame;

    public static final ExposedFrame EMPTY = new ExposedFrame("", "", "", Vec3.ZERO, null, null, Collections.emptyList());

    public ExposedFrame(String id, String shooterName, String timestamp, Vec3 shotPosition, @Nullable ResourceLocation dimension,
                        @Nullable ResourceLocation biome, List<EntityInfo> entitiesInFrame) {
        this.id = id;
        this.shooterName = shooterName;
        this.timestamp = timestamp;
        this.shotPosition = shotPosition;
        this.dimension = dimension;
        this.biome = biome;
        this.entitiesInFrame = entitiesInFrame;
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putString("Id", id);

        if (shooterName.length() > 0)
            tag.putString("ShooterName", shooterName);

        if (timestamp.length() > 0)
            tag.putString("Timestamp", timestamp);

        if (!shotPosition.equals(Vec3.ZERO)) {
            ListTag pos = new ListTag();
            pos.add(DoubleTag.valueOf(shotPosition.x));
            pos.add(DoubleTag.valueOf(shotPosition.y));
            pos.add(DoubleTag.valueOf(shotPosition.z));
            tag.put("Pos", pos);
        }

        if (dimension != null)
            tag.putString("Dimension", dimension.toString());

        if (biome != null)
            tag.putString("Biome", biome.toString());

        if (entitiesInFrame.size() > 0) {
            ListTag entities = new ListTag();
            for (EntityInfo entityInfo : entitiesInFrame) {
                entities.add(entityInfo.save(new CompoundTag()));
            }
            tag.put("Entities", entities);
        }

        return tag;
    }

    public static ExposedFrame load(CompoundTag tag) {
        String id = tag.getString("Id");
        if (id.length() == 0) {
            Exposure.LOGGER.error("Cannot load exposure frame: id is not valid. Tag: " + tag);
            return EMPTY;
        }

        String shooterName = tag.getString("ShooterName");
        String timestamp = tag.getString("Timestamp");

        ListTag posTag = tag.getList("Pos", Tag.TAG_DOUBLE);
        Vec3 pos = posTag.size() == 3 ? new Vec3(posTag.getDouble(0), posTag.getDouble(1), posTag.getDouble(2))
                : Vec3.ZERO;

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

        ListTag entities = tag.getList("Entities", Tag.TAG_COMPOUND);
        List<EntityInfo> entitiesInFrame = new ArrayList<>();
        for (Tag entityInfoTag : entities) {
            entitiesInFrame.add(EntityInfo.fromTag(((CompoundTag) entityInfoTag)));
        }

        return new ExposedFrame(id, shooterName, timestamp, pos, dimension, biome, entitiesInFrame);
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
