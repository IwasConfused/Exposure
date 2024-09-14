package io.github.mortuusars.exposure.neoforge.api.event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

import java.util.List;

/**
 * Can be used to add additional data to the frame or modify existing data. This data can be used in advancements or quests afterward.
 * Fired only on the server side.
 */
public class ModifyFrameDataEvent extends Event {
    public final ServerPlayer player;
    public final ItemStack cameraStack;
    public final CompoundTag frame;
    public final List<Entity> entitiesInFrame;

    public ModifyFrameDataEvent(ServerPlayer player, ItemStack cameraStack, CompoundTag frame, List<Entity> entitiesInFrame) {
        this.player = player;
        this.cameraStack = cameraStack;
        this.frame = frame;
        this.entitiesInFrame = entitiesInFrame;
    }
}
