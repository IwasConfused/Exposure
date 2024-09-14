package io.github.mortuusars.exposure.neoforge.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.neoforge.ExposureNeoForge;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.AddTableLootModifier;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import org.jetbrains.annotations.NotNull;

/**
 * Basically a copy of AddTableLootModifier just to allow disabling it through config.
 */
public class PreventableAddTableLootModifier extends AddTableLootModifier {
    public static final MapCodec<PreventableAddTableLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    IGlobalLootModifier.LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(glm -> glm.conditions),
                    ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("table").forGetter(PreventableAddTableLootModifier::table))
            .apply(instance, PreventableAddTableLootModifier::new));

    public PreventableAddTableLootModifier(LootItemCondition[] conditions, ResourceKey<LootTable> table) {
        super(conditions, table);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(@NotNull ObjectArrayList<ItemStack> generatedLoot, @NotNull LootContext context) {
        if (!Config.Common.LOOT_ADDITION.get()) {
            return generatedLoot;
        }

        return super.doApply(generatedLoot, context);
    }

    @Override
    public @NotNull MapCodec<? extends IGlobalLootModifier> codec() {
        return ExposureNeoForge.LootModifiers.ADD_TABLE.get();
    }
}
