package io.github.mortuusars.exposure.advancement.predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import io.github.mortuusars.exposure.camera.infrastructure.FrameData;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class EntityInFramePredicate {
    public static final EntityInFramePredicate ANY = new EntityInFramePredicate(null, LocationPredicate.ANY, MinMaxBounds.Doubles.ANY);

    @Nullable
    private final String id;
    private final LocationPredicate location;
    private final MinMaxBounds.Doubles distance;

    public EntityInFramePredicate(@Nullable ResourceLocation id, LocationPredicate location, MinMaxBounds.Doubles distance) {
        this.id = id != null ? id.toString() : null;
        this.location = location;
        this.distance = distance;
    }

    public boolean matches(ServerPlayer player, CompoundTag entityInfoTag) {
        if (this.equals(ANY))
            return true;

        if (id != null && !id.equals(entityInfoTag.getString(FrameData.ENTITY_ID)))
            return false;

        if (!locationMatches(player, entityInfoTag))
            return false;

        if (!distance.matches(entityInfoTag.getFloat(FrameData.ENTITY_DISTANCE)))
            return false;

        return true;
    }

    private boolean locationMatches(ServerPlayer player, CompoundTag entityInfoTag) {
        ListTag posList = entityInfoTag.getList(FrameData.ENTITY_POSITION, Tag.TAG_INT);
        if (posList.size() < 3)
            return false;

        int x = posList.getInt(0);
        int y = posList.getInt(1);
        int z = posList.getInt(2);

        return location.matches(player.serverLevel(), x, y, z);
    }

    public JsonElement serializeToJson() {
        if (this == ANY)
            return JsonNull.INSTANCE;

        JsonObject json = new JsonObject();
        if (id != null)
            json.addProperty("id", id);

        if (!location.equals(LocationPredicate.ANY))
            json.add("location", location.serializeToJson());

        if (!distance.isAny())
            json.add("distance", distance.serializeToJson());

        return json;
    }

    public static EntityInFramePredicate fromJson(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull())
            return ANY;

        JsonObject jsonobject = GsonHelper.convertToJsonObject(json, "entity");

        String id = null;
        if (jsonobject.has("id"))
            id = jsonobject.get("id").getAsString();

        LocationPredicate location = LocationPredicate.fromJson(jsonobject.getAsJsonObject("location"));
        MinMaxBounds.Doubles distance = MinMaxBounds.Doubles.fromJson(jsonobject.getAsJsonObject("distance"));

        return new EntityInFramePredicate(id != null ? new ResourceLocation(id) : null, location, distance);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityInFramePredicate that = (EntityInFramePredicate) o;
        return Objects.equals(id, that.id) && Objects.equals(location, that.location) && Objects.equals(distance, that.distance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, location, distance);
    }
}
