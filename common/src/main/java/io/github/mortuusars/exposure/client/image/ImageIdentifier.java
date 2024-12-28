package io.github.mortuusars.exposure.client.image;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public record ImageIdentifier(String id, List<String> variantParts) {
    public static final ImageIdentifier EMPTY = new ImageIdentifier("empty", Collections.emptyList());

    public ImageIdentifier {
        Preconditions.checkArgument(!StringUtil.isNullOrEmpty(id), "Identifier should have an id that's not null or empty.");
    }

    public ResourceLocation toResourceLocation() {
        String path = variantParts.isEmpty()
                ? "image/" + id
                : String.join("/", "image", id, getVariantString());
        return Exposure.resource(Util.sanitizeName(path, ResourceLocation::validPathChar));
    }

    private String getVariantString() {
        return String.join("_", variantParts);
    }

    public boolean matches(String id) {
        return this.id.equals(id);
    }

    public boolean matches(ExposureIdentifier exposureIdentifier) {
        if (exposureIdentifier.isId()) {
            return id.equals(exposureIdentifier.id());
        }
        if (exposureIdentifier.isTexture()) {
            return id.equals(of(exposureIdentifier).id());
        }
        return false;
    }

    public ImageIdentifier appendVariants(String... variants) {
        return variants.length > 0
                ? new ImageIdentifier(id, Stream.concat(variantParts().stream(), Arrays.stream(variants)).toList())
                : this;
    }

    public static ImageIdentifier of(ExposureIdentifier exposureIdentifier, String... variantParts) {
        return new ImageIdentifier(exposureIdentifier.map(
                Function.identity(),
                texture -> String.join("/", "texture", texture.getNamespace(), texture.getPath())),
                List.of(variantParts));
    }
}
