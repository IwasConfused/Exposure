package io.github.mortuusars.exposure.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.component.CustomData;

public record EntityInFrame(ResourceLocation id, String name, BlockPos pos, CustomData customData) {
    public static Codec<EntityInFrame> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(EntityInFrame::id),
                    Codec.STRING.optionalFieldOf("name", "").forGetter(EntityInFrame::name),
                    BlockPos.CODEC.fieldOf("pos").forGetter(EntityInFrame::pos),
                    CustomData.CODEC.optionalFieldOf("custom_data", CustomData.EMPTY).forGetter(EntityInFrame::customData))
            .apply(instance, EntityInFrame::new));

    @SuppressWarnings("deprecation")
    public static StreamCodec<FriendlyByteBuf, EntityInFrame> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, EntityInFrame::id,
            ByteBufCodecs.STRING_UTF8, EntityInFrame::name,
            BlockPos.STREAM_CODEC, EntityInFrame::pos,
            CustomData.STREAM_CODEC, EntityInFrame::customData,
            EntityInFrame::new
    );

    public static EntityInFrame of(Entity entity) {
        return of(entity, CustomData.EMPTY);
    }

    public static EntityInFrame of(Entity entity, CustomData customData) {
        String name = entity.getCustomName() != null ? entity.getCustomName().getString() : "";
        return new EntityInFrame(EntityType.getKey(entity.getType()), name, entity.blockPosition(), customData);
    }
}
