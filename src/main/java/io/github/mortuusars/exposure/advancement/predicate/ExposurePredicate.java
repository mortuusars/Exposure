package io.github.mortuusars.exposure.advancement.predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import io.github.mortuusars.exposure.advancement.BooleanPredicate;
import io.github.mortuusars.exposure.camera.infrastructure.FrameData;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class ExposurePredicate {
    public static final ExposurePredicate ANY = new ExposurePredicate(BooleanPredicate.ANY,
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            NbtPredicate.ANY,
            MinMaxBounds.Ints.ANY,
            MinMaxBounds.Ints.ANY,
            EntityInFramePredicate.ANY);

    private final BooleanPredicate owner;
    private final MinMaxBounds.Doubles shutterSpeedMS;
    private final MinMaxBounds.Doubles focalLength;
    private final NbtPredicate nbt;
    private final MinMaxBounds.Ints lightLevel;
    private final MinMaxBounds.Ints entitiesInFrameCount;
    private final EntityInFramePredicate entityInFrame;

    public ExposurePredicate(BooleanPredicate ownerPredicate,
                             MinMaxBounds.Doubles shutterSpeedMS,
                             MinMaxBounds.Doubles focalLength,
                             NbtPredicate nbtPredicate,
                             MinMaxBounds.Ints lightLevel,
                             MinMaxBounds.Ints entitiesInFrameCount,
                             EntityInFramePredicate entityInFramePredicate) {
        this.owner = ownerPredicate;
        this.shutterSpeedMS = shutterSpeedMS;
        this.focalLength = focalLength;
        this.nbt = nbtPredicate;
        this.lightLevel = lightLevel;
        this.entitiesInFrameCount = entitiesInFrameCount;
        this.entityInFrame = entityInFramePredicate;
    }

    public boolean matches(ServerPlayer player, CompoundTag tag) {
        if (!ownerMatches(player, tag))
            return false;

        if (!shutterSpeedMS.matches(tag.getFloat(FrameData.SHUTTER_SPEED_MS)))
            return false;

        if (!focalLength.matches(tag.getFloat(FrameData.FOCAL_LENGTH)))
            return false;

        if (!nbt.matches(tag))
            return false;

        if (!lightLevel.matches(tag.getInt(FrameData.LIGHT_LEVEL)))
            return false;

        if (!entitiesMatch(player, tag))
            return false;

        return true;
    }

    private boolean ownerMatches(ServerPlayer player, CompoundTag tag) {
        if (owner.equals(BooleanPredicate.ANY))
            return true;

        if (!tag.contains("PhotographerId", Tag.TAG_INT_ARRAY))
            return false;

        UUID photographerId = tag.getUUID("PhotographerId");
        UUID playerId = player.getUUID();

        return owner.matches(photographerId.equals(playerId));
    }

    private boolean entitiesMatch(ServerPlayer player, CompoundTag tag) {
        if (tag.contains(FrameData.ENTITIES_IN_FRAME, Tag.TAG_LIST)) {
            ListTag entities = tag.getList(FrameData.ENTITIES_IN_FRAME, Tag.TAG_COMPOUND);

            if (!entitiesInFrameCount.matches(entities.size()))
                return false;

            for (int i = 0; i < entities.size(); i++) {
                if (entityInFrame.matches(player, entities.getCompound(i)))
                    return true;
            }
        }
        else {
            return entityInFrame.equals(EntityInFramePredicate.ANY) && entitiesInFrameCount.matches(0);
        }

        return false;
    }

    public JsonElement serializeToJson() {
        if (this == ANY)
            return JsonNull.INSTANCE;

        JsonObject json = new JsonObject();

        if (!owner.equals(BooleanPredicate.ANY))
            json.add("owner", owner.serializeToJson());

        if (!shutterSpeedMS.isAny())
            json.add("shutter_speed_ms", shutterSpeedMS.serializeToJson());

        if (!focalLength.isAny())
            json.add("focal_length", focalLength.serializeToJson());

        if (!nbt.equals(NbtPredicate.ANY))
            json.add("nbt", nbt.serializeToJson());

        if (!lightLevel.isAny())
            json.add("light_level", lightLevel.serializeToJson());

        if (!entitiesInFrameCount.isAny())
            json.add("entities_count", entitiesInFrameCount.serializeToJson());

        if (!entityInFrame.equals(EntityInFramePredicate.ANY))
            json.add("entity_in_frame", entityInFrame.serializeToJson());

        return json;
    }

    public static ExposurePredicate fromJson(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull())
            return ANY;

        JsonObject jsonobject = GsonHelper.convertToJsonObject(json, "exposure");

        return new ExposurePredicate(
                BooleanPredicate.fromJson(jsonobject.get("owner")),
                MinMaxBounds.Doubles.fromJson(jsonobject.get("shutter_speed_ms")),
                MinMaxBounds.Doubles.fromJson(jsonobject.get("focal_length")),
                NbtPredicate.fromJson(jsonobject.get("nbt")),
                MinMaxBounds.Ints.fromJson(jsonobject.get("light_level")),
                MinMaxBounds.Ints.fromJson(jsonobject.get("entities_count")),
                EntityInFramePredicate.fromJson(jsonobject.get("entity_in_frame")));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExposurePredicate that = (ExposurePredicate) o;
        return Objects.equals(owner, that.owner) && Objects.equals(shutterSpeedMS, that.shutterSpeedMS) && Objects.equals(focalLength, that.focalLength) && Objects.equals(nbt, that.nbt) && Objects.equals(entitiesInFrameCount, that.entitiesInFrameCount) && Objects.equals(entityInFrame, that.entityInFrame);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, shutterSpeedMS, focalLength, nbt, entitiesInFrameCount, entityInFrame);
    }
}
