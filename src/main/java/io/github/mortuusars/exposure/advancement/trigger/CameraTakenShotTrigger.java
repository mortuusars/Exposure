package io.github.mortuusars.exposure.advancement.trigger;

import com.google.gson.JsonObject;
import io.github.mortuusars.exposure.advancement.predicate.CameraPredicate;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class CameraTakenShotTrigger extends SimpleCriterionTrigger<CameraTakenShotTrigger.TriggerInstance> {
    public static final ResourceLocation ID = new ResourceLocation("frame_exposed");

    public @NotNull ResourceLocation getId() {
        return ID;
    }

    @Override
    protected @NotNull TriggerInstance createInstance(@NotNull JsonObject json, EntityPredicate.@NotNull Composite player, @NotNull DeserializationContext context) {
        LocationPredicate location = LocationPredicate.fromJson(json.get("location"));
        CameraPredicate camera = CameraPredicate.fromJson(json.get("camera"));
        return new TriggerInstance(player, location, camera);
    }

    public void trigger(ServerPlayer player, ItemAndStack<CameraItem> camera, boolean flashHasFired, boolean frameExposed) {
        this.trigger(player, triggerInstance -> triggerInstance.matches(player, camera, flashHasFired, frameExposed));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final LocationPredicate locationPredicate;
        private final CameraPredicate cameraPredicate;
        public TriggerInstance(EntityPredicate.Composite playerPredicate, LocationPredicate locationPredicate, CameraPredicate cameraPredicate) {
            super(ID, playerPredicate);
            this.locationPredicate = locationPredicate;
            this.cameraPredicate = cameraPredicate;
        }

        public boolean matches(ServerPlayer player, ItemAndStack<CameraItem> camera, boolean flashHasFired, boolean frameExposed) {
            if (!locationPredicate.matches(player.getLevel(), player.getX(), player.getY(), player.getZ()))
                return false;

            return cameraPredicate.matches(camera, flashHasFired, frameExposed);
        }

        public @NotNull JsonObject serializeToJson(@NotNull SerializationContext conditions) {
            JsonObject jsonobject = super.serializeToJson(conditions);
            if (this.cameraPredicate != CameraPredicate.ANY)
                jsonobject.add("camera", this.cameraPredicate.serializeToJson());

            if (this.locationPredicate != LocationPredicate.ANY)
                jsonobject.add("location", this.locationPredicate.serializeToJson());

            return jsonobject;
        }
    }
}
