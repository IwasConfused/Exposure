package io.github.mortuusars.exposure.core;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class ExposureIdentifier {
    public static final ExposureIdentifier EMPTY = new ExposureIdentifier("");

    public static final Codec<ExposureIdentifier> TEXTURE_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(ResourceLocation.CODEC.fieldOf("texture").forGetter(ExposureIdentifier::getTextureLocation))
                    .apply(instance, ExposureIdentifier::new));

    public static final Codec<ExposureIdentifier> CODEC = Codec.withAlternative(
            Codec.STRING.xmap(ExposureIdentifier::new, ExposureIdentifier::getId), TEXTURE_CODEC);

    public static final StreamCodec<FriendlyByteBuf, ExposureIdentifier> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ExposureIdentifier decode(FriendlyByteBuf buffer) {
            boolean isTexture = buffer.readBoolean();
            return isTexture
                    ? new ExposureIdentifier(buffer.readResourceLocation())
                    : new ExposureIdentifier(buffer.readUtf());
        }

        @Override
        public void encode(FriendlyByteBuf buffer, ExposureIdentifier instance) {
            boolean isTexture = instance.isTexture();
            buffer.writeBoolean(isTexture);
            if (isTexture) {
                buffer.writeResourceLocation(instance.getTextureLocation());
            } else {
                buffer.writeUtf(instance.getId());
            }
        }
    };

    @Nullable
    private final String id;
    @Nullable
    private final ResourceLocation textureLocation;

    public ExposureIdentifier(@NotNull String id) {
        this.id = id;
        this.textureLocation = null;
    }

    public ExposureIdentifier(@NotNull ResourceLocation textureLocation) {
        this.id = null;
        this.textureLocation = textureLocation;
    }

    public boolean isEmpty() {
        return this == EMPTY || (id != null && id.isEmpty());
    }

    public Either<String, ResourceLocation> get() {
        return id != null ? Either.left(id) : Either.right(textureLocation);
    }

    public @Nullable String getId() {
        return id;
    }

    public Optional<String> getIdOpt() {
        return Optional.ofNullable(id);
    }

    public @Nullable ResourceLocation getTextureLocation() {
        return textureLocation;
    }

    public Optional<ResourceLocation> getTextureOpt() {
        return Optional.ofNullable(textureLocation);
    }

    public boolean isId() {
        return getId() != null;
    }

    public boolean isTexture() {
        return !isId();
    }

    public <T> T map(final Function<String, T> ifId, final Function<ResourceLocation, T> ifTexture) {
        return isId() ? ifId.apply(getId()) : ifTexture.apply(getTextureLocation());
    }

    public ExposureIdentifier ifId(Consumer<String> idConsumer) {
        if (isId()) {
            idConsumer.accept(getId());
        }
        return this;
    }

    public <T> Optional<T> mapId(Function<String, T> mappingFunc) {
        return isId() ? Optional.ofNullable(mappingFunc.apply(id)) : Optional.empty();
    }

    public ExposureIdentifier ifTexture(Consumer<ResourceLocation> textureConsumer) {
        if (isTexture()) {
            textureConsumer.accept(getTextureLocation());
        }
        return this;
    }

    @Override
    public String toString() {
        return map(Function.identity(), ResourceLocation::toString);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExposureIdentifier that = (ExposureIdentifier) o;
        return Objects.equals(id, that.id) && Objects.equals(textureLocation, that.textureLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, textureLocation);
    }
}
