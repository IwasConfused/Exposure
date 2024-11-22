package io.github.mortuusars.exposure.neoforge;

import com.google.common.base.Preconditions;
import com.mojang.serialization.MapCodec;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.integration.ModCompatibilityClient;
import io.github.mortuusars.exposure.neoforge.loot.PreventableAddTableLootModifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

@Mod(Exposure.ID)
public class ExposureNeoForge {
    public ExposureNeoForge(ModContainer container) {
        Exposure.init();

        Exposure.Stats.STATS.forEach((location, formatter) -> {
            RegisterImpl.CUSTOM_STATS.register(location.getPath(), () -> location);
        });

        container.registerConfig(ModConfig.Type.SERVER, Config.Server.SPEC);
        container.registerConfig(ModConfig.Type.COMMON, Config.Common.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, Config.Client.SPEC);

        @Nullable IEventBus modEventBus = container.getEventBus();
        Preconditions.checkNotNull(modEventBus);

        modEventBus.addListener(this::onConfigReloading);

        RegisterImpl.BLOCKS.register(modEventBus);
        RegisterImpl.BLOCK_ENTITY_TYPES.register(modEventBus);
        RegisterImpl.ENTITY_TYPES.register(modEventBus);
        RegisterImpl.ITEMS.register(modEventBus);
        RegisterImpl.MENU_TYPES.register(modEventBus);
        RegisterImpl.RECIPE_TYPES.register(modEventBus);
        RegisterImpl.RECIPE_SERIALIZERS.register(modEventBus);
        RegisterImpl.CRITERION_TRIGGERS.register(modEventBus);
        RegisterImpl.SOUND_EVENTS.register(modEventBus);
        RegisterImpl.COMMAND_ARGUMENT_TYPES.register(modEventBus);
        RegisterImpl.WORLD_GEN_FEATURES.register(modEventBus);
        RegisterImpl.DATA_COMPONENT_TYPES.register(modEventBus);
        RegisterImpl.PARTICLE_TYPES.register(modEventBus);
        RegisterImpl.CUSTOM_STATS.register(modEventBus);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ExposureNeoForgeClient.init(container);
        }
    }

    public static class LootModifiers {
        private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
                DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Exposure.ID);

        public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<PreventableAddTableLootModifier>> ADD_TABLE =
                LOOT_MODIFIERS.register("add_table", () -> PreventableAddTableLootModifier.CODEC);
    }

    private void onConfigReloading(ModConfigEvent.Reloading event) {
//        if (event.getConfig().getType() == ModConfig.Type.COMMON && ModList.get().isLoaded("create")) {
//            CreateFilmDeveloping.clearCachedData();
//        }

        if (event.getConfig().getType() == ModConfig.Type.CLIENT) {
            ModCompatibilityClient.handle();
        }
    }
}
