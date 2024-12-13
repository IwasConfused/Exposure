package io.github.mortuusars.exposure.integration.kubejs.event;

import dev.latvian.mods.kubejs.player.KubePlayerEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Can be used to add additional encodedValue to the frame or modify existing encodedValue. This encodedValue can be used in advancements or quests afterward.
 * Fired only on the server side.
 */
public class ModifyFrameDataEventJS implements KubePlayerEvent {
    private final Player player;
    private final ItemStack cameraStack;
    private final CompoundTag frame;
    private final List<Entity> entitiesInFrame;

    public ModifyFrameDataEventJS(Player player, ItemStack cameraStack, CompoundTag frame, List<Entity> entitiesInFrame) {
        this.player = player;
        this.cameraStack = cameraStack;
        this.frame = frame;
        this.entitiesInFrame = entitiesInFrame;
    }

    @Override
    public Player getEntity() {
        return player;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    public ItemStack getCameraStack() {
        return cameraStack;
    }

    public CompoundTag getFrame() {
        return frame;
    }

    public List<Entity> getEntitiesInFrame() {
        return entitiesInFrame;
    }
}
