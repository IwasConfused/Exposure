package io.github.mortuusars.exposure.core.camera.component;

import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.Exposure;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record CompositionGuide(String name) {
    public static final Codec<CompositionGuide> CODEC = Codec.STRING.xmap(CompositionGuides::byNameOrNone, CompositionGuide::name);

    public static final StreamCodec<ByteBuf, CompositionGuide> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, CompositionGuide::name,
            CompositionGuides::byNameOrNone
    );

    public MutableComponent translate() {
        return Component.translatable("gui." + Exposure.ID + ".composition_guide." + name);
    }

    public ResourceLocation overlayTextureLocation() {
        return Exposure.resource("textures/gui/viewfinder/composition_guide/" + name + ".png");
    }

    public ResourceLocation buttonSpriteLocation() {
        return Exposure.resource("camera_controls/composition_guide/" + name);
    }
}
