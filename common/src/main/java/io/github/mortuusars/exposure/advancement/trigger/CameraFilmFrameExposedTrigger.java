package io.github.mortuusars.exposure.advancement.trigger;

import com.google.gson.JsonObject;
import io.github.mortuusars.exposure.advancement.predicate.ExposurePredicate;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.advancements.critereon.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class CameraFilmFrameExposedTrigger extends SimpleCriterionTrigger<CameraFilmFrameExposedTrigger.TriggerInstance> {
    public static final ResourceLocation ID = new ResourceLocation("frame_exposed");

    public @NotNull ResourceLocation getId() {
        return ID;
    }

    @Override
    protected @NotNull TriggerInstance createInstance(JsonObject json, @NotNull EntityPredicate.Composite predicate,
                                                      @NotNull DeserializationContext deserializationContext) {
        LocationPredicate location = LocationPredicate.fromJson(json.get("location"));
        ExposurePredicate exposure = ExposurePredicate.fromJson(json.get("exposure"));
        return new TriggerInstance(predicate, location, exposure);
    }

    public void trigger(ServerPlayer player, ItemAndStack<CameraItem> camera, CompoundTag frame) {
        this.trigger(player, triggerInstance -> triggerInstance.matches(player, camera, frame));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final LocationPredicate locationPredicate;
        private final ExposurePredicate exposurePredicate;

        public TriggerInstance(EntityPredicate.Composite predicate, LocationPredicate locationPredicate, ExposurePredicate exposurePredicate) {
            super(ID, predicate);
            this.locationPredicate = locationPredicate;
            this.exposurePredicate = exposurePredicate;
        }

        public boolean matches(ServerPlayer player, ItemAndStack<CameraItem> camera, CompoundTag frame) {
            if (!locationPredicate.matches(player.getLevel(), player.getX(), player.getY(), player.getZ()))
                return false;

            return exposurePredicate.matches(player, frame);
        }

        public @NotNull JsonObject serializeToJson(@NotNull SerializationContext conditions) {
            JsonObject jsonobject = super.serializeToJson(conditions);
            if (this.exposurePredicate != ExposurePredicate.ANY)
                jsonobject.add("exposure", this.exposurePredicate.serializeToJson());

            if (this.locationPredicate != LocationPredicate.ANY)
                jsonobject.add("location", this.locationPredicate.serializeToJson());

            return jsonobject;
        }
    }
}
