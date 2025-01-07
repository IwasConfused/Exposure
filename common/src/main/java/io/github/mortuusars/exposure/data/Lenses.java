package io.github.mortuusars.exposure.data;

import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.camera.component.FocalRange;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Optional;

public class Lenses extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();

    private Map<Ingredient, FocalRange> lenses = ImmutableMap.of();

    public Lenses() {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(), "exposure/lens");
    }

    public Optional<FocalRange> getFocalRange(ItemStack stack) {
        if (!stack.is(Exposure.Tags.Items.LENSES)) {
            return Optional.empty();
        }

        for (var lens : lenses.entrySet()) {
            if (lens.getKey().test(stack)) {
                return Optional.of(lens.getValue());
            }
        }

        return Optional.empty();
    }

    public FocalRange getFocalRangeOrDefault(ItemStack stack) {
        return getFocalRange(stack).orElse(FocalRange.getDefault());
    }

    public Map<Ingredient, FocalRange> get() {
        return ImmutableMap.copyOf(lenses);
    }

    public void set(Map<Ingredient, FocalRange> lenses) {
        this.lenses = lenses;
    }

    // --

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> content, ResourceManager resourceManager, ProfilerFiller profiler) {
        ImmutableMap.Builder<Ingredient, FocalRange> builder = ImmutableMap.builder();

        for (var entry : content.entrySet()) {
            ResourceLocation location = entry.getKey();

            try {
                JsonObject jsonObject = entry.getValue().getAsJsonObject();
                JsonElement item = jsonObject.get("item");

                Ingredient ingredient = Ingredient.CODEC.parse(JsonOps.INSTANCE, item).getOrThrow();
                if (ingredient.isEmpty())
                    throw new IllegalArgumentException("'item' cannot be empty.");

                JsonElement value = jsonObject.get("focal_range");
                FocalRange focalRange = FocalRange.fromJson(value);

                builder.put(ingredient, focalRange);
            }
            catch (Exception e) {
                LOGGER.error("Error loading lens '{}'", location, e);
            }
        }

        set(builder.build());

        if (lenses.isEmpty())
            LOGGER.info("No lenses have been loaded.");
        else {
            LOGGER.info("Loaded {} lenses.", this.lenses.size());
        }
    }
}