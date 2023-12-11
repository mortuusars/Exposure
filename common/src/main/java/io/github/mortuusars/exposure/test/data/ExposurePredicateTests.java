package io.github.mortuusars.exposure.test.data;

import com.google.gson.JsonObject;
import io.github.mortuusars.exposure.advancement.predicate.ExposurePredicate;
import io.github.mortuusars.exposure.camera.infrastructure.FrameData;
import io.github.mortuusars.exposure.test.framework.ITestClass;
import io.github.mortuusars.exposure.test.framework.Test;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

import java.util.List;

public class ExposurePredicateTests implements ITestClass {
    @Override
    public List<Test> collect() {
        return List.of(
                new Test("ExposurePredicate_DeserializesProperly", this::deserializesProperly)
        );
    }

    private void deserializesProperly(ServerPlayer serverPlayer) {
        String json =
            """
            {
              "owner": true,
              "shutter_speed_ms": {
                "min": 50
              },
              "entities_count": {
                "max": 5
              }
            }
            """;

        JsonObject jsonObj = GsonHelper.parse(json).getAsJsonObject();

        ExposurePredicate exposurePredicate = ExposurePredicate.fromJson(jsonObj);

        CompoundTag frame = new CompoundTag();
        frame.putUUID(FrameData.PHOTOGRAPHER_ID, serverPlayer.getUUID());
        frame.putFloat(FrameData.SHUTTER_SPEED_MS, 100);
        ListTag entities = new ListTag();
        entities.add(new CompoundTag());
        entities.add(new CompoundTag());
        frame.put(FrameData.ENTITIES_IN_FRAME, entities);

        assertThat(exposurePredicate.matches(serverPlayer, frame), "Deserialized predicate does not match frame: " + frame);
    }
}
