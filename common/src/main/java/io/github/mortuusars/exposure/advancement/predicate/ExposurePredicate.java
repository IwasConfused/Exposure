package io.github.mortuusars.exposure.advancement.predicate;

import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.NbtPredicate;

import java.util.Optional;

public record ExposurePredicate(Optional<MinMaxBounds.Ints> dayTime,
                                Optional<MinMaxBounds.Ints> lightLevel,
                                Optional<NbtPredicate> nbt,
                                Optional<MinMaxBounds.Ints> entitiesInFrameCount,
                                Optional<EntityPredicate> entityInFrame) {

//    public boolean matches(ServerPlayer player, CompoundTag frameTag, List<Entity> entitiesInFrameIds) {
//        return dayTime.isEmpty() || dayTime.get().matches()
//        return this.dayTime.matches(frameTag.contains(FrameData.DAYTIME, Tag.TAG_INT) ? frameTag.getInt(FrameData.DAYTIME) : -1)
//                && this.lightLevelBeforeShot.matches(frameTag.contains(FrameData.LIGHT_LEVEL, Tag.TAG_INT) ? frameTag.getInt(FrameData.LIGHT_LEVEL) : -1)
//                && this.entitiesInFrameCount.matches(entitiesInFrameIds.size())
//                && this.nbt.matches(frameTag)
//                && entitiesMatch(player, entitiesInFrameIds);
//    }
//
//    protected boolean entitiesMatch(ServerPlayer player, List<Entity> entitiesInFrameIds) {
//        // Handles the case where the list is empty
//        if (entityInFrame.equals(EntityPredicate.ANY)) {
//            return true;
//        }
//
//        for (Entity entity : entitiesInFrameIds) {
//            if (entityInFrame.matches(player, entity))
//                return true;
//        }
//
//        return false;
//    }
}
