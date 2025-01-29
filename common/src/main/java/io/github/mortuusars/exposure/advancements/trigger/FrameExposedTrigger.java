package io.github.mortuusars.exposure.advancements.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.advancements.predicate.CameraPredicate;
import io.github.mortuusars.exposure.advancements.predicate.FramePredicate;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class FrameExposedTrigger extends SimpleCriterionTrigger<FrameExposedTrigger.TriggerInstance> {
    @Override
    public @NotNull Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player,
                        CameraHolder cameraHolder,
                        ItemStack cameraStack,
                        Frame frame,
                        List<BlockPos> locationsInFrame,
                        List<LivingEntity> entitiesInFrame) {
        this.trigger(player, triggerInstance ->
                triggerInstance.matches(player, cameraHolder, cameraStack, frame, locationsInFrame, entitiesInFrame));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player,
                                  Optional<CameraPredicate> camera,
                                  Optional<FramePredicate> frame,
                                  Optional<LocationPredicate> locationInFrame,
                                  Optional<EntityPredicate> entityInFrame) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                        CameraPredicate.CODEC.optionalFieldOf("camera").forGetter(TriggerInstance::camera),
                        FramePredicate.CODEC.optionalFieldOf("frame").forGetter(TriggerInstance::frame),
                        LocationPredicate.CODEC.optionalFieldOf("location_in_frame").forGetter(TriggerInstance::locationInFrame),
                        EntityPredicate.CODEC.optionalFieldOf("entity_in_frame").forGetter(TriggerInstance::entityInFrame))
                .apply(instance, TriggerInstance::new));

        public boolean matches(ServerPlayer player,
                               CameraHolder cameraHolder,
                               ItemStack cameraStack,
                               Frame frame,
                               List<BlockPos> locationsInFrame,
                               List<LivingEntity> entitiesInFrame) {
            Entity holder = cameraHolder.asEntity();

            return (camera.isEmpty() || camera.get().matches(player.serverLevel(), cameraStack, holder.position()))
                    && (this.frame.isEmpty() || this.frame.get().matches(frame))
                    && (locationInFrame.isEmpty() || locationsInFrame.stream().anyMatch(pos ->
                        locationInFrame.get().matches(player.serverLevel(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)))
                    && (entityInFrame.isEmpty() || entitiesInFrame.stream().anyMatch(entity ->
                        entityInFrame.get().matches(player.serverLevel(), holder.position(), entity)));
        }
    }
}