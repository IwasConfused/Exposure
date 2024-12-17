package io.github.mortuusars.exposure.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.CustomData;

import java.util.function.Consumer;

public record EntityInFrame(ResourceLocation id, String name, BlockPos pos, int distance, CustomData customData) {
    public static Codec<EntityInFrame> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(EntityInFrame::id),
                    Codec.STRING.optionalFieldOf("name", "").forGetter(EntityInFrame::name),
                    BlockPos.CODEC.fieldOf("pos").forGetter(EntityInFrame::pos),
                    Codec.INT.optionalFieldOf("distance", Integer.MAX_VALUE).forGetter(EntityInFrame::distance),
                    CustomData.CODEC.optionalFieldOf("custom_data", CustomData.EMPTY).forGetter(EntityInFrame::customData))
            .apply(instance, EntityInFrame::new));

    @SuppressWarnings("deprecation")
    public static StreamCodec<FriendlyByteBuf, EntityInFrame> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, EntityInFrame::id,
            ByteBufCodecs.STRING_UTF8, EntityInFrame::name,
            BlockPos.STREAM_CODEC, EntityInFrame::pos,
            ByteBufCodecs.VAR_INT, EntityInFrame::distance,
            CustomData.STREAM_CODEC, EntityInFrame::customData,
            EntityInFrame::new
    );

    public static EntityInFrame of(Entity cameraHolder, Entity entity) {
        return of(cameraHolder, entity, CustomData.EMPTY);
    }

    public static EntityInFrame of(Entity cameraHolder, Entity entity, Consumer<CompoundTag> customDataInput) {
        CustomData customData = CustomData.EMPTY.update(customDataInput);
        return of(cameraHolder, entity, customData);
    }

    public static EntityInFrame of(Entity cameraHolder, Entity entity, CustomData customData) {
        return new EntityInFrame(EntityType.getKey(entity.getType()), entity.getScoreboardName(), entity.blockPosition(),
                ((int) cameraHolder.distanceTo(entity)), customData);
    }
}
