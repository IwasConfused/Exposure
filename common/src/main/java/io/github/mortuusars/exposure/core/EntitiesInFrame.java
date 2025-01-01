package io.github.mortuusars.exposure.core;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.util.Fov;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class EntitiesInFrame {
    public static List<LivingEntity> get(Entity photographer, double fov, int limit, boolean inSelfieMode) {
        double currentFov = fov * Exposure.CROP_FACTOR;
        double currentFocalLength = Fov.fovToFocalLength(currentFov);

        Vec3 cameraPos = photographer.position().add(0, photographer.getEyeHeight(), 0);
        Vec3 cameraLookAngle = Vec3.directionFromRotation(photographer.getXRot(), photographer.getYRot());

        List<Entity> entities = photographer.level().getEntities(photographer, new AABB(photographer.blockPosition()).inflate(128),
                entity -> entity instanceof LivingEntity);

        //TODO: Take entity size into account
        entities.sort((entity, entity2) -> {
            float dist1 = photographer.distanceTo(entity);
            float dist2 = photographer.distanceTo(entity2);
            if (dist1 == dist2) return 0;
            return dist1 > dist2 ? 1 : -1;
        });

        List<LivingEntity> entitiesInFrame = new ArrayList<>();

        for (Entity entity : entities) {
            if (entitiesInFrame.size() >= limit)
                break;

            if (!(entity instanceof LivingEntity))
                continue;

            if (!isInFOV(cameraPos, cameraLookAngle, currentFov, entity))
                continue; // Not in frame

            if (getWeightedDistance(cameraPos, entity) > currentFocalLength)
                continue; // Too far to be in frame

            if (!hasLineOfSight(photographer, entity))
                continue; // Not visible

            entitiesInFrame.add(((LivingEntity) entity));
        }

        //TODO: Move camera pos properly to capture what's behind a player
        if (inSelfieMode)
            entitiesInFrame.addFirst(((LivingEntity) photographer));

        return entitiesInFrame;
    }

    /**
     * Copy of LivingEntity#hasLineOfSight just to accept Entity instead of LivingEntity. Entity does not have this method.
     */
    public static boolean hasLineOfSight(Entity photographer, Entity entity) {
        if (entity.level() != photographer.level()) {
            return false;
        } else {
            Vec3 vec3 = new Vec3(photographer.getX(), photographer.getEyeY(), photographer.getZ());
            Vec3 vec32 = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
            if (vec32.distanceTo(vec3) > 128.0) {
                return false;
            } else {
                return photographer.level().clip(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE, photographer)).getType() == HitResult.Type.MISS;
            }
        }
    }

    /**
     * Gets the distance in blocks to the target entity. Weighted == adjusted relative to the size of entity's bounding box.
     */
    public static double getWeightedDistance(Vec3 cameraPos, Entity entity) {
        double distanceInBlocks = Math.sqrt(entity.distanceToSqr(cameraPos));

        AABB boundingBox = entity.getBoundingBoxForCulling();
        double size = boundingBox.getSize();
        if (Double.isNaN(size) || size == 0.0)
            size = 0.1;

        double sizeModifier = (size - 1.0) * 0.6 + 1.0;
        return (distanceInBlocks / sizeModifier) * Exposure.CROP_FACTOR;
    }

    public static boolean isInFOV(Vec3 cameraPos, Vec3 cameraLookAngle, double fov, Entity target) {
        Vec3 targetEyePos = target.position().add(0, target.getEyeHeight(), 0);

        // Valid angles form a circle instead of square.
        // Due to this, entities in the corners of a frame are not considered "in frame".
        // I'm too dumb at math to fix this.
        
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
