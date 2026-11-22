package io.github.mortuusars.exposure.world.camera.frame;

import io.github.mortuusars.exposure.util.Fov;
import io.github.mortuusars.exposure.util.PointOfView;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class EntitiesInFrame {
    public static List<LivingEntity> get(CameraHolder cameraHolder, PointOfView pov, double fov) {
        return get(cameraHolder.asHolderEntity(), pov, fov);
    }

    public static List<LivingEntity> get(Entity cameraHolder, PointOfView pov, double fov) {
        fov *= 0.95; // 5% margin from edge
        double focalLength = Fov.fovToFocalLength(fov);

        AABB area = new AABB(cameraHolder.blockPosition()).inflate(128);
        List<Entity> entities = cameraHolder.level().getEntities(null, area);

        FrustumCheck frustum = FrustumCheck.createFromCamera(pov.pos(), pov.dir(), ((float) Math.toRadians(fov)));

        entities.sort((entity, entity2) -> {
            double dist1 = pov.pos().distanceTo(entity.position());
            double dist2 = pov.pos().distanceTo(entity2.position());
            if (dist1 == dist2) return 0;
            return dist1 > dist2 ? 1 : -1;
        });

        List<LivingEntity> entitiesInFrame = new ArrayList<>();

        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity livingEntity)) continue;
            if (!livingEntity.isAlive()) continue;
            if (!frustum.contains(entity.getEyePosition())) continue; // Not in frame
            if (calculateVisibleDistance(pov.pos(), entity) > focalLength) continue; // Too far to be in frame
            if (!hasLineOfSight(pov.pos(), entity)) continue; // Not visible

            entitiesInFrame.add(livingEntity);
        }

        return entitiesInFrame;
    }

    /**
     * Gets the distance in blocks to the target entity.
     */
    public static double calculateVisibleDistance(Vec3 cameraPos, Entity entity) {
        double distanceInBlocks = Math.sqrt(entity.distanceToSqr(cameraPos));

        AABB boundingBox = entity.getBoundingBox();
        double size = boundingBox.getSize();
        if (Double.isNaN(size) || size == 0.0)
            size = 0.1;

        double sizeInfluence = (size - 1.0) * 0.6 + 1.0;
        // Makes distance longer, so entity would need to be closer to be "in frame". Very sophisticated math right here.
        double feelsRightInfluence = 1.15;
        return (distanceInBlocks / sizeInfluence) * feelsRightInfluence;
    }

    /**
     * Adaptation of {@link LivingEntity#hasLineOfSight(Entity)} but using custom camera position instead of an entity.
     */
    public static boolean hasLineOfSight(Vec3 cameraPos, Entity entity) {
        return entity.level().clip(new ClipContext(cameraPos, entity.getEyePosition(),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getType() == HitResult.Type.MISS;
    }

    /**
     * ChatGPT did a good job with this one.
     */
    public static class FrustumCheck {
        private final Vec3 cameraPos;
        private final Vec3 forward;
        private final Vec3 right;
        private final Vec3 up;
        private final float fovRadians;

        public FrustumCheck(Vec3 cameraPos, Vec3 forward, Vec3 right, Vec3 up, float fovRadians) {
            this.cameraPos = cameraPos;
            this.forward = forward.normalize();
            this.right = right.normalize();
            this.up = up.normalize();
            this.fovRadians = fovRadians;
        }

        public boolean contains(Vec3 pos) {
            // Calculate relative position
            Vec3 toEntity = pos.subtract(cameraPos);

            // Dot product with forward vector for depth
            double depth = toEntity.dot(forward);
            if (depth <= 0) {
                // Entity is behind the camera
                return false;
            }

            // Dot product with right and up vectors
            double horizontalOffset = toEntity.dot(right);
            double verticalOffset = toEntity.dot(up);

            double halfSize = depth * Math.tan(fovRadians / 2);

            // Check if the entity is within the frustum bounds
            return Math.abs(horizontalOffset) <= halfSize && Math.abs(verticalOffset) <= halfSize;
        }

        public static FrustumCheck createFromCamera(Vec3 cameraPos, Vec3 lookVec, float fov) {
            // Create forward vector (normalized)
            Vec3 forward = lookVec.normalize();

            // Create up vector (assume Y-up world). You may replace this with actual camera up vector if available.
            Vec3 worldUp = new Vec3(0, 1, 0);

            // Calculate right and adjusted up vectors
            Vec3 right = forward.cross(worldUp).normalize();
            Vec3 up = right.cross(forward).normalize();

            return new FrustumCheck(cameraPos, forward, right, up, fov);
        }
    }
}