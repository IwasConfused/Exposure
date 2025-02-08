package io.github.mortuusars.exposure.neoforge.api.event;

import io.github.mortuusars.exposure.util.ExtraData;
import io.github.mortuusars.exposure.world.camera.capture.CaptureProperties;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

import java.util.List;

/**
 * Can be used to add additional data to the frame or modify existing data. This data can be used in advancements or quests afterward.
 * Fired server side on NeoForge.EVENT_BUS.
 */
public class ModifyFrameExtraDataEvent extends Event {
    private final CameraHolder cameraHolder;
    private final ItemStack stack;
    private final CaptureProperties captureProperties;
    private final List<BlockPos> positionsInFrame;
    private final List<LivingEntity> entitiesInFrame;
    private final ExtraData data;

    public ModifyFrameExtraDataEvent(CameraHolder cameraHolder, ItemStack stack, CaptureProperties captureProperties,
                                     List<BlockPos> positionsInFrame, List<LivingEntity> entitiesInFrame, ExtraData data) {
        this.cameraHolder = cameraHolder;
        this.stack = stack;
        this.captureProperties = captureProperties;
        this.positionsInFrame = positionsInFrame;
        this.entitiesInFrame = entitiesInFrame;
        this.data = data;
    }

    public CameraHolder getCameraHolder() {
        return cameraHolder;
    }

    public Entity getCameraHolderEntity() {
        return cameraHolder.asEntity();
    }

    public ItemStack getCamera() {
        return stack;
    }

    public CaptureProperties getCaptureProperties() {
        return captureProperties;
    }

    public List<BlockPos> getPositionsInFrame() {
        return positionsInFrame;
    }

    public List<LivingEntity> getEntitiesInFrame() {
        return entitiesInFrame;
    }

    public ExtraData getData() {
        return data;
    }
}
