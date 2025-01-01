package io.github.mortuusars.exposure.fabric;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeModConfigEvents;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.event_hub.CommonEvents;
import io.github.mortuusars.exposure.event_hub.ServerEvents;
import io.github.mortuusars.exposure.fabric.resources.FabricLensesDataLoader;
import io.github.mortuusars.exposure.integration.ModCompatibilityClient;
import io.github.mortuusars.exposure.network.fabric.FabricC2SPackets;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableSource;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.neoforged.fml.config.ModConfig;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class ExposureFabric implements ModInitializer {
    // Server field to access when no other objects are available to get it from.
    public static @Nullable MinecraftServer server = null;

    @Override
    public void onInitialize() {
        Exposure.init();

        NeoForgeModConfigEvents.reloading(Exposure.ID).register(config -> {
            if (config.getType() == ModConfig.Type.COMMON && FabricLoader.getInstance().isModLoaded("create")) {
//                CreateFilmDeveloping.clearCachedData();
            }

            if (config.getType() == ModConfig.Type.CLIENT) {
                ModCompatibilityClient.handle();
            }
        });

        NeoForgeConfigRegistry.INSTANCE.register(Exposure.ID, ModConfig.Type.SERVER, Config.Server.SPEC);
        NeoForgeConfigRegistry.INSTANCE.register(Exposure.ID, ModConfig.Type.COMMON, Config.Common.SPEC);
        NeoForgeConfigRegistry.INSTANCE.register(Exposure.ID, ModConfig.Type.CLIENT, Config.Client.SPEC);

        CommandRegistrationCallback.EVENT.register(CommonEvents::registerCommands);

        addToCreativeTabs();

        Exposure.Stats.register();

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new FabricLensesDataLoader());

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ServerEvents.serverStarted(server);
            ExposureFabric.server = server;
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            ServerEvents.serverStopped(server);
            ExposureFabric.server = null;
        });

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> ServerEvents.syncDatapack(Stream.of(player)));

        LootTableEvents.MODIFY.register(ExposureFabric::modifyLoot);

        FabricC2SPackets.register();
    }

    private static void addToCreativeTabs() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(content -> {
            content.accept(Exposure.Items.CAMERA.get());
            content.accept(Exposure.Items.BLACK_AND_WHITE_FILM.get());
            content.accept(Exposure.Items.COLOR_FILM.get());
            content.accept(Exposure.Items.DEVELOPED_BLACK_AND_WHITE_FILM.get());
            content.accept(Exposure.Items.DEVELOPED_COLOR_FILM.get());
            content.accept(Exposure.Items.PHOTOGRAPH.get());
            content.accept(Exposure.Items.AGED_PHOTOGRAPH.get());
            content.accept(Exposure.Items.INTERPLANAR_PROJECTOR.get());
            content.accept(Exposure.Items.STACKED_PHOTOGRAPHS.get());
            content.accept(Exposure.Items.PHOTOGRAPH_FRAME.get());
            content.accept(Exposure.Items.ALBUM.get());
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(content -> {
            content.accept(Exposure.Items.LIGHTROOM.get());
        });
    }

    private static void modifyLoot(ResourceKey<LootTable> tableKey, LootTable.Builder builder,
                                   LootTableSource source, HolderLookup.Provider provider) {
        if (!Config.Common.LOOT_ADDITION.get() || !source.isBuiltin())
            return;

        if (BuiltInLootTables.SIMPLE_DUNGEON.equals(tableKey)) {
            builder.pool(LootPool.lootPool()
                    .add(NestedLootTable.lootTableReference(Exposure.LootTables.SIMPLE_DUNGEON_INJECT))
                    .build());
        }
        if (BuiltInLootTables.ABANDONED_MINESHAFT.equals(tableKey)) {
            builder.pool(LootPool.lootPool()
                    .add(NestedLootTable.lootTableReference(Exposure.LootTables.ABANDONED_MINESHAFT_INJECT))
                    .build());
        }
        if (BuiltInLootTables.STRONGHOLD_CROSSING.equals(tableKey)) {
            builder.pool(LootPool.lootPool()
                    .add(NestedLootTable.lootTableReference(Exposure.LootTables.STRONGHOLD_CROSSING_INJECT))
                    .build());
        }
        if (BuiltInLootTables.VILLAGE_PLAINS_HOUSE.equals(tableKey)) {
            builder.pool(LootPool.lootPool()
                    .add(NestedLootTable.lootTableReference(Exposure.LootTables.VILLAGE_PLAINS_HOUSE_INJECT))
                    .build());
        }
        if (BuiltInLootTables.SHIPWRECK_MAP.equals(tableKey)) {
            builder.pool(LootPool.lootPool()
                    .add(NestedLootTable.lootTableReference(Exposure.LootTables.SHIPWRECK_MAP_INJECT))
                    .build());
        }
    }
}
