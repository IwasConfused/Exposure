package io.github.mortuusars.exposure.core.frame;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

public record Photographer(String name, UUID uuid) {
    public static final Photographer EMPTY = new Photographer("", Util.NIL_UUID);

    public static final Codec<Photographer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.optionalFieldOf("name", "").forGetter(Photographer::name),
                    UUIDUtil.CODEC.optionalFieldOf("uuid", Util.NIL_UUID).forGetter(Photographer::uuid))
            .apply(instance, Photographer::new));

    public static final StreamCodec<ByteBuf, Photographer> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, Photographer::name,
            UUIDUtil.STREAM_CODEC, Photographer::uuid,
            Photographer::new
    );

    public Photographer(LivingEntity entity) {
        this(entity.getScoreboardName(), entity.getUUID());
    }

    public boolean matches(LivingEntity entity) {
        return uuid.equals(entity.getUUID());
    }

    public boolean isEmpty() {
        return this.equals(EMPTY);
    }
}
