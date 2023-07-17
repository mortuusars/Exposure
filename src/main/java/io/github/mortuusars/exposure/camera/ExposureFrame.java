package io.github.mortuusars.exposure.camera;

import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ExposureFrame {
    public String id;
    public Vec3 shotPosition;
    public List<EntityInfo> entitiesInFrame;

    public ExposureFrame(String id, Vec3 shotPosition, List<EntityInfo> entitiesInFrame) {
        this.id = id;
        this.shotPosition = shotPosition;
        this.entitiesInFrame = entitiesInFrame;
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putString("Id", id);
        ListTag pos = new ListTag();
        pos.add(DoubleTag.valueOf(shotPosition.x));
        pos.add(DoubleTag.valueOf(shotPosition.y));
        pos.add(DoubleTag.valueOf(shotPosition.z));
        tag.put("Pos", pos);

        ListTag entities = new ListTag();
        for (EntityInfo entityInfo : entitiesInFrame) {
            entities.add(entityInfo.save(new CompoundTag()));
        }
        tag.put("Entities", entities);

        return tag;
    }

    public static ExposureFrame load(CompoundTag tag) {
        String id = tag.getString("Id");

        ListTag posTag = tag.getList("Pos", Tag.TAG_DOUBLE);
        Vec3 pos = new Vec3(posTag.getDouble(0), posTag.getDouble(1), posTag.getDouble(2));

        ListTag entities = tag.getList("Entities", Tag.TAG_COMPOUND);
        List<EntityInfo> entitiesInFrame = new ArrayList<>();
        for (Tag entityInfoTag : entities) {
            entitiesInFrame.add(EntityInfo.fromTag(((CompoundTag) entityInfoTag)));
        }

        return new ExposureFrame(id, pos, entitiesInFrame);
    }

    public static class EntityInfo {
        public ResourceLocation typeId;
        public CompoundTag tag;

        public EntityInfo(ResourceLocation typeId, CompoundTag tag) {
            this.typeId = typeId;
            this.tag = tag;
        }

        public CompoundTag save(CompoundTag tag) {
            tag.putString("Id", typeId.toString());
            tag.put("Tag", this.tag);
            return tag;
        }

        public static EntityInfo fromTag(CompoundTag tag) {
            return new EntityInfo(new ResourceLocation(tag.getString("Id")), tag.getCompound("Tag"));
        }
    }
}
