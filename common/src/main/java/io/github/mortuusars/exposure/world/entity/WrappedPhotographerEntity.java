package io.github.mortuusars.exposure.world.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class WrappedPhotographerEntity implements PhotographerEntity {
    private final Entity entity;
    private final Player executingPlayer;

    public WrappedPhotographerEntity(Entity entity, Player executingPlayer) {
        this.entity = entity;
        this.executingPlayer = executingPlayer;
    }

    @Override
    public @NotNull Player getExecutingPlayer() {
        return executingPlayer;
    }

    @Override
    public Entity getOwnerEntity() {
        return entity;
    }

    @Override
    public Entity asEntity() {
        return entity;
    }
}
