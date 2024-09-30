package io.github.mortuusars.exposure.core;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.Optional;
import java.util.function.Function;

public final class CameraAccessor {
    public static final Codec<CameraAccessor> CODEC = ResourceLocation.CODEC.xmap(CameraAccessors::byId, CameraAccessors::idOf);

    public static final StreamCodec<ByteBuf, CameraAccessor> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, CameraAccessors::idOf,
            CameraAccessors::byId
    );

    private final Function<Entity, Optional<NewCamera>> accessFunction;

    public CameraAccessor(Function<Entity, Optional<NewCamera>> accessFunction) {
        this.accessFunction = accessFunction;
    }

    public Optional<NewCamera> getCamera(Entity entity) {
        return accessFunction.apply(entity);
    }
}
