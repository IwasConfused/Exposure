package io.github.mortuusars.exposure.core;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class ExposureIdentifier {
    public static final ExposureIdentifier EMPTY = new ExposureIdentifier("", null);

    public static final Codec<ExposureIdentifier> CODEC = Codec.withAlternative(
            Codec.STRING.xmap(ExposureIdentifier::id, ExposureIdentifier::id),
            ResourceLocation.CODEC.xmap(ExposureIdentifier::texture, ExposureIdentifier::texture));

    public static final StreamCodec<FriendlyByteBuf, ExposureIdentifier> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ExposureIdentifier decode(FriendlyByteBuf buffer) {
            boolean isTexture = buffer.readBoolean();
            return isTexture
                    ? ExposureIdentifier.texture(buffer.readResourceLocation())
                    : ExposureIdentifier.id(buffer.readUtf());
        }

        @Override
        public void encode(FriendlyByteBuf buffer, ExposureIdentifier instance) {
            buffer.writeBoolean(instance.isTexture());
            instance.ifId(buffer::writeUtf)
                    .ifTexture(buffer::writeResourceLocation);
        }
    };

    @Nullable
    private final String id;
    @Nullable
    private final ResourceLocation texture;

    private ExposureIdentifier(@Nullable String id, @Nullable ResourceLocation textureLocation) {
        this.id = id;
        this.texture = textureLocation;
    }

    public static ExposureIdentifier id(@NotNull String id) {
        return new ExposureIdentifier(id, null);
    }

    public static ExposureIdentifier texture(@NotNull ResourceLocation texture) {
        return new ExposureIdentifier(null, texture);
    }

    public boolean isEmpty() {
        return this == EMPTY || (id != null && id.isEmpty());
    }

    public @Nullable String id() {
        return id;
    }

    public Optional<String> getId() {
        return Optional.ofNullable(id);
    }

    public @Nullable ResourceLocation texture() {
        return texture;
    }

    public Optional<ResourceLocation> getTexture() {
        return Optional.ofNullable(texture);
    }

    public boolean isId() {
        return id != null;
    }

    public boolean isTexture() {
        return texture != null;
    }

    public <T> T map(final Function<String, T> ifId, final Function<ResourceLocation, T> ifTexture) {
        return isId() ? ifId.apply(id) : ifTexture.apply(texture());
    }

    public ExposureIdentifier ifId(Consumer<String> idConsumer) {
        if (isId()) {
            idConsumer.accept(id);
        }
        return this;
    }

    public <T> Optional<T> mapId(Function<String, T> mappingFunc) {
        return isId() ? Optional.of(mappingFunc.apply(id)) : Optional.empty();
    }

    public ExposureIdentifier ifTexture(Consumer<ResourceLocation> textureConsumer) {
        if (isTexture()) {
            textureConsumer.accept(texture());
        }
        return this;
    }

    public <T> Optional<T> mapTexture(Function<ResourceLocation, T> mappingFunc) {
        return isTexture() ? Optional.of(mappingFunc.apply(texture)) : Optional.empty();
    }

    public String toValueString() {
        return map(Function.identity(), ResourceLocation::toString);
    }

    @Override
    public String toString() {
        return map(id -> "Id: " + id, texture -> "Texture: " + texture);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExposureIdentifier that = (ExposureIdentifier) o;
        return Objects.equals(id, that.id) && Objects.equals(texture, that.texture);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, texture);
    }

    // --

    public static ExposureIdentifier createId(Entity entity, String... middleParts) {
        List<String> parts = new ArrayList<>();
        parts.add(entity.getScoreboardName());
        parts.addAll(Arrays.asList(middleParts));
        parts.add(Long.toString(entity.level().getGameTime()));
        return createId(parts.toArray(String[]::new));
    }

    public static ExposureIdentifier createId(String... parts) {
        Preconditions.checkArgument(parts.length > 0, "Cannot compose ID with 0 parts.");
        List<String> sanitizedParts = Arrays.stream(parts)
                .filter(s -> !StringUtil.isNullOrEmpty(s))
                .map(s -> s.replace('_', '-'))
                .toList();
        String id = String.join("_", sanitizedParts);
        return ExposureIdentifier.id(id);
    }
}
