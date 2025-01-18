package io.github.mortuusars.exposure.world.camera.frame;

import io.github.mortuusars.exposure.util.Fov;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class EntitiesInFrame {
    public static List<LivingEntity> get(Entity photographer, double fov, boolean inSelfieMode) {
        Vec3 cameraPos = photographer.position().add(0, photographer.getEyeHeight(), 0);
        Vec3 cameraDirection = Vec3.directionFromRotation(photographer.getXRot(), photographer.getYRot());

        if (inSelfieMode) {
            cameraDirection = cameraDirection.reverse();
            float maxDistance = 1.75F;  // This is a default value (used in CameraMixin) for client camera when in selfie mode.
            float maxCameraDistance = getMaxCameraDistance(photographer, cameraPos, cameraDirection, maxDistance);
            cameraPos = cameraPos.add(cameraDirection.normalize().scale(-maxCameraDistance));
        }

        return get(photographer, cameraPos, cameraDirection, fov);
    }

    public static List<LivingEntity> get(Entity photographer, Vec3 cameraPos, Vec3 cameraDirection, double fov) {
        double focalLength = Fov.fovToFocalLength(fov);

        AABB area = new AABB(photographer.blockPosition()).inflate(128);
        List<Entity> entities = photographer.level().getEntities(null, area);

        FrustumCheck frustum = FrustumCheck.createFromCamera(cameraPos, cameraDirection, ((float) Math.toRadians(fov)));

        entities.sort((entity, entity2) -> {
            float dist1 = photographer.distanceTo(entity);
            float dist2 = photographer.distanceTo(entity2);
            if (dist1 == dist2) return 0;
            return dist1 > dist2 ? 1 : -1;
        });

        List<LivingEntity> entitiesInFrame = new ArrayList<>();

        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity livingEntity)) continue;
            if (!livingEntity.isAlive()) continue;
            if (!frustum.contains(entity.getEyePosition())) continue; // Not in frame
            if (calculateVisibleDistance(cameraPos, entity) > focalLength) continue; // Too far to be in frame
            if (!hasLineOfSight(cameraPos, entity)) continue; // Not visible

            entitiesInFrame.add(livingEntity);
        }

        return entitiesInFrame;
    }

    /**
     * This method is same as {@link net.minecraft.client.Camera}#getMaxZoom.
     * We need it to calculate proper camera position if camera is in third-person mode, so it does not clip into blocks.
     */
    public static float getMaxCameraDistance(Entity photographer, Vec3 position, Vec3 direction, float maxDistance) {
        for (int i = 0; i < 8; i++) {
            float xOff = (float) ((i & 1) * 2 - 1);
            float yOff = (float) ((i >> 1 & 1) * 2 - 1);
            float zOff = (float) ((i >> 2 & 1) * 2 - 1);
            Vec3 pos = position.add(xOff * 0.1F, yOff * 0.1F, zOff * 0.1F);
            Vec3 endPos = pos.add(direction.scale(-maxDistance));
            HitResult hitResult = photographer.level().clip(
                    new ClipContext(pos, endPos, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, photographer));
            if (hitResult.getType() != HitResult.Type.MISS) {
                float distanceSqr = (float) hitResult.getLocation().distanceToSqr(position);
                if (distanceSqr < Mth.square(maxDistance)) {
                    maxDistance = Mth.sqrt(distanceSqr);
                }
            }
        }

        return maxDistance;
    }

    /**
     * Gets the distance in blocks to the target entity.
     */
    public static double calculateVisibleDistance(Vec3 cameraPos, Entity entity) {
        double distanceInBlocks = Math.sqrt(entity.distanceToSqr(cameraPos));

        AABB boundingBox = entity.getBoundingBoxForCulling();
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
