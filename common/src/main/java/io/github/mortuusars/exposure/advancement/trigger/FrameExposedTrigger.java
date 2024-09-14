package io.github.mortuusars.exposure.advancement.trigger;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.advancement.predicate.CameraPredicate;
import io.github.mortuusars.exposure.advancement.predicate.ExposurePredicate;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.advancements.critereon.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class FrameExposedTrigger extends SimpleCriterionTrigger<FrameExposedTrigger.TriggerInstance> {
    @Override
    public @NotNull Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ItemAndStack<CameraItem> camera, CompoundTag frame, List<Entity> entitiesInFrame) {
        this.trigger(player, triggerInstance -> triggerInstance.matches(player, camera, frame, entitiesInFrame));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player,
                                  Optional<CameraPredicate> camera,
//                                  Optional<ExposurePredicate> exposure,
                                  Optional<LocationPredicate> location) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                        CameraPredicate.CODEC.optionalFieldOf("camera").forGetter(TriggerInstance::camera),
//                        ExposurePredicate.CODEC.optionalFieldOf("exposure").forGetter(TriggerInstance::exposure),
                        LocationPredicate.CODEC.optionalFieldOf("location").forGetter(TriggerInstance::location))
                .apply(instance, TriggerInstance::new));

        public boolean matches(ServerPlayer player, ItemAndStack<CameraItem> camera, CompoundTag frame, List<Entity> entitiesInFrame) {
            //TODO: finish
            return false; /*camera.matches(camera)
                    && exposure.matches(player, frame, entitiesInFrameIds)
                    && location.matches(player.serverLevel(), player.getX(), player.getY(), player.getZ());*/
        }
    }
}