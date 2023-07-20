package io.github.mortuusars.exposure.camera;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExposureFrame {
    public String id;
    public Vec3 shotPosition;
    public List<EntityInfo> entitiesInFrame;

    public static final ExposureFrame EMPTY = new ExposureFrame("", Vec3.ZERO, Collections.emptyList());

    public ExposureFrame(String id, Vec3 shotPosition, List<EntityInfo> entitiesInFrame) {
        this.id = id;
        this.shotPosition = shotPosition;
        this.entitiesInFrame = entitiesInFrame;
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putString("Id", id);

        if (!shotPosition.equals(Vec3.ZERO)) {
            ListTag pos = new ListTag();
            pos.add(DoubleTag.valueOf(shotPosition.x));
            pos.add(DoubleTag.valueOf(shotPosition.y));
            pos.add(DoubleTag.valueOf(shotPosition.z));
            tag.put("Pos", pos);
        }

        if (entitiesInFrame.size() > 0) {
            ListTag entities = new ListTag();
            for (EntityInfo entityInfo : entitiesInFrame) {
                entities.add(entityInfo.save(new CompoundTag()));
            }
            tag.put("Entities", entities);
        }

        return tag;
    }

    public static ExposureFrame load(CompoundTag tag) {
        String id = tag.getString("Id");
        if (id.length() == 0) {
            Exposure.LOGGER.error("Cannot load exposure frame: id is not valid. Tag: " + tag);
            return EMPTY;
        }

        ListTag posTag = tag.getList("Pos", Tag.TAG_DOUBLE);
        Vec3 pos = posTag.size() == 3 ? new Vec3(posTag.getDouble(0), posTag.getDouble(1), posTag.getDouble(2))
                : Vec3.ZERO;

        ListTag entities = tag.getList("Entities", Tag.TAG_COMPOUND);
        List<EntityInfo> entitiesInFrame = new ArrayList<>();
        for (Tag entityInfoTag : entities) {
            entitiesInFrame.add(EntityInfo.fromTag(((CompoundTag) entityInfoTag)));
        }

        return new ExposureFrame(id, pos, entitiesInFrame);
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
