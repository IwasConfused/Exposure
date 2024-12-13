package io.github.mortuusars.exposure.data.lenses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.camera.component.FocalRange;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LensesDataLoader extends SimpleJsonResourceReloadListener {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final String DIRECTORY = "lens";
    
    public LensesDataLoader() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> content, ResourceManager resourceManager, ProfilerFiller profiler) {
        ConcurrentMap<Ingredient, FocalRange> lenses = new ConcurrentHashMap<>();

        Exposure.LOGGER.info("Loading exposure lenses:");

        for (var entry : content.entrySet()) {
            // Lenses should be in encodedValue/exposure/lens folder.
            // Excluding other namespaces because it potentially can cause conflicts,
            // if some other mod adds their own type of 'lens'.
            if (!entry.getKey().getNamespace().equals(Exposure.ID))
                continue;

            try {
                JsonObject jsonObject = entry.getValue().getAsJsonObject();
                JsonElement item = jsonObject.get("item");

                Ingredient ingredient = Ingredient.CODEC.parse(JsonOps.INSTANCE, item).getOrThrow();
                if (ingredient.isEmpty())
                    throw new IllegalArgumentException("'item' cannot be empty.");

                JsonElement value = jsonObject.get("focal_range");
                FocalRange focalRange = FocalRange.fromJson(value);

                lenses.put(ingredient, focalRange);

                Exposure.LOGGER.info("Lens [" + entry.getKey() + ", " + focalRange + "] added.");
            }
            catch (Exception e) {
                Exposure.LOGGER.error(e.toString());
            }
        }

        if (lenses.isEmpty())
            Exposure.LOGGER.info("No lenses have been loaded.");

        Lenses.reload(lenses);
    }
}