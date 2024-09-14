package io.github.mortuusars.exposure.core;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.function.Function;

public final class CameraAccessor {
    public static final Codec<CameraAccessor> CODEC = ResourceLocation.CODEC.xmap(CameraAccessors::byId, CameraAccessors::idOf);

    public static final StreamCodec<ByteBuf, CameraAccessor> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, CameraAccessors::idOf,
            CameraAccessors::byId
    );

    private final Function<Player, Optional<NewCamera>> accessFunction;

    public CameraAccessor(Function<Player, Optional<NewCamera>> accessFunction) {
        this.accessFunction = accessFunction;
    }

    public Optional<NewCamera> getCamera(Player player) {
        return accessFunction.apply(player);
    }
}
