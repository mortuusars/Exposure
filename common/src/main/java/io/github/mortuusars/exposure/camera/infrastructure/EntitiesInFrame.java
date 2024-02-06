package io.github.mortuusars.exposure.camera.infrastructure;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.util.Fov;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class EntitiesInFrame {
    public static List<Entity> get(Player player, double fov, int limit, boolean inSelfieMode) {
        double currentFov = fov / Exposure.CROP_FACTOR;
        double currentFocalLength = Fov.fovToFocalLength(currentFov);
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        List<Entity> entities = player.level().getEntities(player, new AABB(player.blockPosition()).inflate(128),
                entity -> entity instanceof LivingEntity);

        entities.sort((entity, entity2) -> {
            float dist1 = player.distanceTo(entity);
            float dist2 = player.distanceTo(entity2);
            if (dist1 == dist2) return 0;
            return dist1 > dist2 ? 1 : -1;
        });

        List<Entity> entitiesInFrame = new ArrayList<>();

        for (Entity entity : entities) {
            if (entitiesInFrame.size() >= limit)
                break;

            if (!isInFOV(currentFov, entity))
                continue; // Not in frame

            if (getPerceivedDistance(cameraPos, entity) > currentFocalLength)
                continue; // Too far to be in frame

            if (!player.hasLineOfSight(entity))
                continue; // Not visible

            entitiesInFrame.add(entity);
        }

        if (inSelfieMode)
            entitiesInFrame.add(0, player);

        return entitiesInFrame;
    }

    /**
     * Gets the distance in blocks to the target entity. Perceived == adjusted relative to the size of entity's bounding box.
     */
    public static double getPerceivedDistance(Vec3 cameraPos, Entity entity) {
        double distanceInBlocks = Math.sqrt(entity.distanceToSqr(cameraPos));

        AABB boundingBox = entity.getBoundingBoxForCulling();
        double size = boundingBox.getSize();
        if (Double.isNaN(size) || size == 0.0)
            size = 0.1;

        double sizeModifier = (size - 1.0) * 0.6 + 1.0;
        return (distanceInBlocks / sizeModifier) / Exposure.CROP_FACTOR;
    }

    public static boolean isInFOV(double fov, Entity target) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        Vec3 cameraLookAngle = new Vec3(camera.getLookVector());
        Vec3 targetEyePos = target.position().add(0, target.getEyeHeight(), 0);

        // Valid angles form a circle instead of square.
        // Due to this, entities in the corners of a frame are not considered "in frame".
        // I'm too dumb at maths to fix this.
        
        double relativeAngle = getRelativeAngle(cameraPos, cameraLookAngle, targetEyePos);
        return relativeAngle <= fov / 2f;
    }

    /**
     * L    T (Target)
     * | D /
     * |--/
     * | /
     * |/
     * C (Camera), L (Camera look angle)
     */
    public static double getRelativeAngle(Vec3 cameraPos, Vec3 cameraLookAngle, Vec3 targetEyePos) {
        Vec3 originToTargetAngle = targetEyePos.subtract(cameraPos).normalize();
        return Math.toDegrees(Math.acos(cameraLookAngle.dot(originToTargetAngle)));
    }
}
