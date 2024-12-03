package io.github.mortuusars.exposure;

import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.advancement.trigger.FrameExposedTrigger;
import io.github.mortuusars.exposure.block.FlashBlock;
import io.github.mortuusars.exposure.block.LightroomBlock;
import io.github.mortuusars.exposure.block.entity.LightroomBlockEntity;
import io.github.mortuusars.exposure.core.camera.CompositionGuide;
import io.github.mortuusars.exposure.core.camera.FlashMode;
import io.github.mortuusars.exposure.command.argument.ExposureLookArgument;
import io.github.mortuusars.exposure.command.argument.ExposureSizeArgument;
import io.github.mortuusars.exposure.command.argument.ShaderLocationArgument;
import io.github.mortuusars.exposure.command.argument.TextureLocationArgument;
import io.github.mortuusars.exposure.core.ExposureType;
import io.github.mortuusars.exposure.core.InterplanarProjectorMode;
import io.github.mortuusars.exposure.core.camera.ShutterSpeed;
import io.github.mortuusars.exposure.entity.PhotographFrameEntity;
import io.github.mortuusars.exposure.item.*;
import io.github.mortuusars.exposure.item.component.ExposureFrame;
import io.github.mortuusars.exposure.item.component.StoredItemStack;
import io.github.mortuusars.exposure.item.component.album.AlbumContent;
import io.github.mortuusars.exposure.item.component.album.SignedAlbumContent;
import io.github.mortuusars.exposure.item.component.camera.ShutterState;
import io.github.mortuusars.exposure.menu.*;
import io.github.mortuusars.exposure.recipe.FilmDevelopingRecipe;
import io.github.mortuusars.exposure.recipe.PhotographAgingRecipe;
import io.github.mortuusars.exposure.recipe.PhotographCopyingRecipe;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatFormatter;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootTable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;


public class Exposure {
    public static final String ID = "exposure";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final float CROP_FACTOR = 1.142857f;
    public static final int MAX_ENTITIES_IN_FRAME = 12;

    public static void init() {
        Blocks.init();
        BlockEntityTypes.init();
        EntityTypes.init();
        Items.init();
        DataComponents.init();
        CriteriaTriggers.init();
        MenuTypes.init();
        RecipeSerializers.init();
        SoundEvents.init();
        ArgumentTypes.init();

//        Camera.registerCameraGetter(Exposure.resource("camera_in_hand"), player -> CameraInHand.ofPlayer(player, CameraItem.class));
    }

    public static void initServer(MinecraftServer server) {
        ExposureServer.init(server);
    }

    /**
     * Creates resource location in the mod namespace with the given filePath.
     */
    public static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ID, path);
    }

    public static class Blocks {
        public static final Supplier<LightroomBlock> LIGHTROOM = Register.block("lightroom",
                () -> new LightroomBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_BROWN)
                        .strength(2.5f)
                        .sound(SoundType.WOOD)
                        .lightLevel(state -> 15)));

        public static final Supplier<FlashBlock> FLASH = Register.block("flash",
                () -> new FlashBlock(BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.AIR)
                        .strength(-1.0F, 3600000.8F)
                        .noLootTable()
                        .mapColor(MapColor.NONE)
                        .noOcclusion()
                        .noCollission()
                        .lightLevel(state -> 15)));

        static void init() {
        }
    }

    public static class BlockEntityTypes {
        public static final Supplier<BlockEntityType<LightroomBlockEntity>> LIGHTROOM =
                Register.blockEntityType("lightroom", () -> Register.newBlockEntityType(LightroomBlockEntity::new, Blocks.LIGHTROOM.get()));

        static void init() {
        }
    }

    public static class Items {
        public static final Supplier<CameraItem> CAMERA = Register.item("camera",
                () -> new CameraItem(new Item.Properties()
                        .stacksTo(1)));

        public static final Supplier<FilmRollItem> BLACK_AND_WHITE_FILM = Register.item("black_and_white_film",
                () -> new FilmRollItem(ExposureType.BLACK_AND_WHITE, Mth.color(0.8F, 0.8F, 0.9F),
                        new Item.Properties()
                                .stacksTo(16)));

        public static final Supplier<FilmRollItem> COLOR_FILM = Register.item("color_film",
                () -> new FilmRollItem(ExposureType.COLOR, Mth.color(0.4F, 0.4F, 1.0F), new Item.Properties()
                        .stacksTo(16)));

        public static final Supplier<DevelopedFilmItem> DEVELOPED_BLACK_AND_WHITE_FILM = Register.item("developed_black_and_white_film",
                () -> new DevelopedFilmItem(ExposureType.BLACK_AND_WHITE, new Item.Properties()
                        .stacksTo(1)));

        public static final Supplier<DevelopedFilmItem> DEVELOPED_COLOR_FILM = Register.item("developed_color_film",
                () -> new DevelopedFilmItem(ExposureType.COLOR, new Item.Properties()
                        .stacksTo(1)));

        public static final Supplier<PhotographItem> PHOTOGRAPH = Register.item("photograph",
                () -> new PhotographItem(new Item.Properties()
                        .stacksTo(1)));

        public static final Supplier<ChromaticSheetItem> CHROMATIC_SHEET = Register.item("chromatic_sheet",
                () -> new ChromaticSheetItem(new Item.Properties()
                        .stacksTo(1)));

        public static final Supplier<PhotographItem> AGED_PHOTOGRAPH = Register.item("aged_photograph",
                () -> new AgedPhotographItem(new Item.Properties()
                        .stacksTo(1)));

        public static final Supplier<InterplanarProjectorItem> INTERPLANAR_PROJECTOR = Register.item("interplanar_projector",
                () -> new InterplanarProjectorItem(new Item.Properties()));

        public static final Supplier<StackedPhotographsItem> STACKED_PHOTOGRAPHS = Register.item("stacked_photographs",
                () -> new StackedPhotographsItem(new Item.Properties()
                        .stacksTo(1)));

        public static final Supplier<AlbumItem> ALBUM = Register.item("album",
                () -> new AlbumItem(new Item.Properties()
                        .stacksTo(1)));
        public static final Supplier<SignedAlbumItem> SIGNED_ALBUM = Register.item("signed_album",
                () -> new SignedAlbumItem(new Item.Properties()
                        .stacksTo(1)));

        public static final Supplier<PhotographFrameItem> PHOTOGRAPH_FRAME = Register.item("photograph_frame",
                () -> new PhotographFrameItem(new Item.Properties()));

        public static final Supplier<BlockItem> LIGHTROOM = Register.item("lightroom",
                () -> new BlockItem(Blocks.LIGHTROOM.get(), new Item.Properties()));

        static void init() {
        }
    }

    public static class DataComponents {
        // Camera State
        public static final DataComponentType<Boolean> CAMERA_ACTIVE = Register.dataComponentType("camera_viewfinder_active",
                arg -> arg.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

        public static final DataComponentType<Boolean> SELFIE_MODE = Register.dataComponentType("camera_selfie_mode",
                arg -> arg.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

        public static final DataComponentType<ShutterState> SHUTTER_STATE = Register.dataComponentType("camera_shutter_state",
                arg -> arg.persistent(ShutterState.CODEC).networkSynchronized(ShutterState.STREAM_CODEC));

        // Camera Settings
        public static final DataComponentType<ShutterSpeed> SHUTTER_SPEED = Register.dataComponentType("camera_shutter_speed",
                arg -> arg.persistent(ShutterSpeed.CODEC).networkSynchronized(ShutterSpeed.STREAM_CODEC));

        public static final DataComponentType<CompositionGuide> COMPOSITION_GUIDE = Register.dataComponentType("camera_composition_guide",
                arg -> arg.persistent(CompositionGuide.CODEC).networkSynchronized(CompositionGuide.STREAM_CODEC));

        public static final DataComponentType<Double> ZOOM = Register.dataComponentType("camera_zoom",
                arg -> arg.persistent(Codec.DOUBLE).networkSynchronized(ByteBufCodecs.DOUBLE));

        public static final DataComponentType<FlashMode> FLASH_MODE = Register.dataComponentType("camera_flash_mode",
                arg -> arg.persistent(FlashMode.CODEC).networkSynchronized(FlashMode.STREAM_CODEC));

        // Camera Attachments
        public static final DataComponentType<StoredItemStack> FILM = Register.dataComponentType("camera_film_attachment",
                arg -> arg.persistent(StoredItemStack.CODEC).networkSynchronized(StoredItemStack.STREAM_CODEC));

        public static final DataComponentType<StoredItemStack> FLASH = Register.dataComponentType("camera_flash_attachment",
                arg -> arg.persistent(StoredItemStack.CODEC).networkSynchronized(StoredItemStack.STREAM_CODEC));

        public static final DataComponentType<StoredItemStack> LENS = Register.dataComponentType("camera_lens_attachment",
                arg -> arg.persistent(StoredItemStack.CODEC).networkSynchronized(StoredItemStack.STREAM_CODEC));

        public static final DataComponentType<StoredItemStack> FILTER = Register.dataComponentType("camera_filter_attachment",
                arg -> arg.persistent(StoredItemStack.CODEC).networkSynchronized(StoredItemStack.STREAM_CODEC));

        // Film
        public static final DataComponentType<Integer> FILM_FRAME_COUNT = Register.dataComponentType("film_frame_count",
                arg -> arg.persistent(ExtraCodecs.intRange(1, 512)).networkSynchronized(ByteBufCodecs.VAR_INT));

        public static final DataComponentType<Integer> FILM_FRAME_SIZE = Register.dataComponentType("film_frame_size",
                arg -> arg.persistent(ExtraCodecs.intRange(1, 2048)).networkSynchronized(ByteBufCodecs.VAR_INT));

        public static final DataComponentType<List<ExposureFrame>> FILM_FRAMES =
                Register.dataComponentType("film_frames",
                        arg -> arg.persistent(ExposureFrame.CODEC.listOf())
                                .networkSynchronized(ExposureFrame.STREAM_CODEC.apply(ByteBufCodecs.list())));

        public static final DataComponentType<InterplanarProjectorMode> INTERPLANAR_PROJECTOR_MODE =
                Register.dataComponentType("interplanar_projector_mode",
                        arg -> arg.persistent(InterplanarProjectorMode.CODEC)
                                .networkSynchronized(InterplanarProjectorMode.STREAM_CODEC));

        public static final DataComponentType<List<ExposureFrame>> CHROMATIC_SHEET_LAYERS =
                Register.dataComponentType("chromatic_sheet_layers",
                        arg -> arg.persistent(ExposureFrame.CODEC.listOf(0, 3))
                                .networkSynchronized(ExposureFrame.STREAM_CODEC.apply(ByteBufCodecs.list())));

        // Photograph
        public static final DataComponentType<ExposureFrame> PHOTOGRAPH_FRAME = Register.dataComponentType("photograph_frame",
                arg -> arg.persistent(ExposureFrame.CODEC).networkSynchronized(ExposureFrame.STREAM_CODEC));

        public static final DataComponentType<ExposureType> PHOTOGRAPH_TYPE = Register.dataComponentType("photograph_type",
                arg -> arg.persistent(ExposureType.CODEC).networkSynchronized(ExposureType.STREAM_CODEC));

        public static final DataComponentType<Integer> PHOTOGRAPH_GENERATION = Register.dataComponentType("photograph_generation",
                arg -> arg.persistent(ExtraCodecs.intRange(0, 3)).networkSynchronized(ByteBufCodecs.VAR_INT));

        public static final DataComponentType<List<ItemAndStack<PhotographItem>>> STACKED_PHOTOGRAPHS =
                Register.dataComponentType("stacked_photographs",
                        arg -> arg.persistent(StackedPhotographsItem.PHOTOGRAPH_ITEM_AND_STACK_CODEC.listOf(0, 64))
                                .networkSynchronized(StackedPhotographsItem.PHOTOGRAPH_ITEM_AND_STACK_STREAM_CODEC.apply(ByteBufCodecs.list())));

        // Album
        public static final DataComponentType<AlbumContent> ALBUM_CONTENT = Register.dataComponentType("album_content",
                arg -> arg.persistent(AlbumContent.CODEC).networkSynchronized(AlbumContent.STREAM_CODEC));

        public static final DataComponentType<SignedAlbumContent> SIGNED_ALBUM_CONTENT = Register.dataComponentType("signed_album_content",
                arg -> arg.persistent(SignedAlbumContent.CODEC).networkSynchronized(SignedAlbumContent.STREAM_CODEC));

        static void init() {
        }
    }

    public static class EntityTypes {
        public static final Supplier<EntityType<PhotographFrameEntity>> PHOTOGRAPH_FRAME = Register.entityType("photograph_frame",
                PhotographFrameEntity::new, MobCategory.MISC, 0.5F, 0.5F,
                128, false, Integer.MAX_VALUE);

        static void init() {
        }
    }

    public static class MenuTypes {
        public static final Supplier<MenuType<CameraAttachmentsMenu>> CAMERA = Register.menuType("camera", CameraAttachmentsMenu::fromBuffer);
        public static final Supplier<MenuType<AlbumMenu>> ALBUM = Register.menuType("album", AlbumMenu::fromBuffer);
        public static final Supplier<MenuType<LecternAlbumMenu>> LECTERN_ALBUM = Register.menuType("lectern_album", LecternAlbumMenu::fromBuffer);
        public static final Supplier<MenuType<LightroomMenu>> LIGHTROOM = Register.menuType("lightroom", LightroomMenu::fromBuffer);
        public static final Supplier<MenuType<ItemRenameMenu>> ITEM_RENAME = Register.menuType("item_rename", ItemRenameMenu::fromBuffer);

        static void init() {
        }
    }

    public static class RecipeSerializers {
        public static final Supplier<RecipeSerializer<?>> FILM_DEVELOPING = Register.recipeSerializer("film_developing",
                FilmDevelopingRecipe.Serializer::new);
        public static final Supplier<RecipeSerializer<?>> PHOTOGRAPH_CLONING = Register.recipeSerializer("photograph_copying",
                PhotographCopyingRecipe.Serializer::new);
        public static final Supplier<RecipeSerializer<?>> PHOTOGRAPH_AGING = Register.recipeSerializer("photograph_aging",
                PhotographAgingRecipe.Serializer::new);

        static void init() {
        }
    }

    public static class SoundEvents {
        public static final Supplier<SoundEvent> VIEWFINDER_OPEN = register("item", "camera.viewfinder_open");
        public static final Supplier<SoundEvent> VIEWFINDER_CLOSE = register("item", "camera.viewfinder_close");
        public static final Supplier<SoundEvent> SHUTTER_OPEN = register("item", "camera.shutter_open");
        public static final Supplier<SoundEvent> SHUTTER_CLOSE = register("item", "camera.shutter_close");
        public static final Supplier<SoundEvent> SHUTTER_TICKING = register("item", "camera.shutter_ticking");
        public static final Supplier<SoundEvent> FILM_ADVANCING = register("item", "camera.film_advance");
        public static final Supplier<SoundEvent> FILM_ADVANCE_LAST = register("item", "camera.film_advance_last");
        public static final Supplier<SoundEvent> FILM_REMOVED = register("item", "camera.film_removed");
        public static final Supplier<SoundEvent> CAMERA_GENERIC_CLICK = register("item", "camera.generic_click");
        public static final Supplier<SoundEvent> CAMERA_BUTTON_CLICK = register("item", "camera.button_click");
        public static final Supplier<SoundEvent> CAMERA_RELEASE_BUTTON_CLICK = register("item", "camera.release_button_click");
        public static final Supplier<SoundEvent> CAMERA_DIAL_CLICK = register("item", "camera.dial_click");
        public static final Supplier<SoundEvent> CAMERA_LENS_RING_CLICK = register("item", "camera.lens_ring_click");
        public static final Supplier<SoundEvent> LENS_INSERT = register("item", "camera.lens_insert");
        public static final Supplier<SoundEvent> LENS_REMOVE = register("item", "camera.lens_remove");
        public static final Supplier<SoundEvent> FILTER_INSERT = register("item", "camera.filter_insert");
        public static final Supplier<SoundEvent> FILTER_REMOVE = register("item", "camera.filter_remove");
        public static final Supplier<SoundEvent> FLASH = register("item", "camera.flash");
        public static final Supplier<SoundEvent> INTERPLANAR_PROJECT = register("item", "camera.interplanar_projector.project");

        public static final Supplier<SoundEvent> PHOTOGRAPH_PLACE = register("item", "photograph.place");
        public static final Supplier<SoundEvent> PHOTOGRAPH_BREAK = register("item", "photograph.break");
        public static final Supplier<SoundEvent> PHOTOGRAPH_RUSTLE = register("item", "photograph.rustle");

        public static final Supplier<SoundEvent> PHOTOGRAPH_FRAME_PLACE = register("item", "photograph_frame.place");
        public static final Supplier<SoundEvent> PHOTOGRAPH_FRAME_BREAK = register("item", "photograph_frame.break");
        public static final Supplier<SoundEvent> PHOTOGRAPH_FRAME_ADD_ITEM = register("item", "photograph_frame.add_item");
        public static final Supplier<SoundEvent> PHOTOGRAPH_FRAME_REMOVE_ITEM = register("item", "photograph_frame.remove_item");
        public static final Supplier<SoundEvent> PHOTOGRAPH_FRAME_ROTATE_ITEM = register("item", "photograph_frame.rotate_item");

        public static final Supplier<SoundEvent> LIGHTROOM_PRINT = register("block", "lightroom.print");

        public static final Supplier<SoundEvent> WRITE = register("misc", "write");

        private static Supplier<SoundEvent> register(String category, String key) {
            Preconditions.checkState(category != null && !category.isEmpty(), "'category' should not be empty.");
            Preconditions.checkState(key != null && !key.isEmpty(), "'key' should not be empty.");
            String path = category + "." + key;
            return Register.soundEvent(path, () -> SoundEvent.createVariableRangeEvent(Exposure.resource(path)));
        }

        static void init() {
        }
    }

    public static class Stats {
        public static final Map<ResourceLocation, StatFormatter> STATS = new HashMap<>();

        public static final ResourceLocation INTERACT_WITH_LIGHTROOM =
                register(Exposure.resource("interact_with_lightroom"), StatFormatter.DEFAULT);
        public static final ResourceLocation FILM_FRAMES_EXPOSED =
                register(Exposure.resource("film_frames_exposed"), StatFormatter.DEFAULT);
        public static final ResourceLocation FLASHES_TRIGGERED =
                register(Exposure.resource("flashes_triggered"), StatFormatter.DEFAULT);

        @SuppressWarnings("SameParameterValue")
        private static ResourceLocation register(ResourceLocation location, StatFormatter formatter) {
            STATS.put(location, formatter);
            return location;
        }

        public static void register() {
            STATS.forEach((location, formatter) -> {
                Registry.register(BuiltInRegistries.CUSTOM_STAT, location, location);
                net.minecraft.stats.Stats.CUSTOM.get(location, formatter);
            });
        }
    }

    public static class CriteriaTriggers {
        public static Supplier<FrameExposedTrigger> FILM_FRAME_EXPOSED = Register.criterionTrigger("frame_exposed", FrameExposedTrigger::new);
        public static Supplier<PlayerTrigger> PHOTOGRAPH_ENDERMAN_EYES = Register.criterionTrigger("photograph_enderman_eyes", PlayerTrigger::new);

        public static void init() {
        }
    }

    public static class LootTables {
        public static final ResourceKey<LootTable> SIMPLE_DUNGEON_INJECT =
                ResourceKey.create(Registries.LOOT_TABLE, Exposure.resource("chests/simple_dungeon"));
        public static final ResourceKey<LootTable> ABANDONED_MINESHAFT_INJECT =
                ResourceKey.create(Registries.LOOT_TABLE, Exposure.resource("chests/abandoned_mineshaft"));
        public static final ResourceKey<LootTable> STRONGHOLD_CROSSING_INJECT =
                ResourceKey.create(Registries.LOOT_TABLE, Exposure.resource("chests/stronghold"));
        public static final ResourceKey<LootTable> VILLAGE_PLAINS_HOUSE_INJECT =
                ResourceKey.create(Registries.LOOT_TABLE, Exposure.resource("chests/village_plains_house"));
        public static final ResourceKey<LootTable> SHIPWRECK_MAP_INJECT =
                ResourceKey.create(Registries.LOOT_TABLE, Exposure.resource("chests/shipwreck_map"));
    }

    public static class Tags {
        public static class Items {
            public static final TagKey<Item> FILM_ROLLS = TagKey.create(Registries.ITEM, Exposure.resource("film_rolls"));
            public static final TagKey<Item> DEVELOPED_FILM_ROLLS = TagKey.create(Registries.ITEM, Exposure.resource("developed_film_rolls"));
            public static final TagKey<Item> CYAN_PRINTING_DYES = TagKey.create(Registries.ITEM, Exposure.resource("cyan_printing_dyes"));
            public static final TagKey<Item> MAGENTA_PRINTING_DYES = TagKey.create(Registries.ITEM, Exposure.resource("magenta_printing_dyes"));
            public static final TagKey<Item> YELLOW_PRINTING_DYES = TagKey.create(Registries.ITEM, Exposure.resource("yellow_printing_dyes"));
            public static final TagKey<Item> BLACK_PRINTING_DYES = TagKey.create(Registries.ITEM, Exposure.resource("black_printing_dyes"));
            public static final TagKey<Item> PHOTO_PAPERS = TagKey.create(Registries.ITEM, Exposure.resource("photo_papers"));
            public static final TagKey<Item> PHOTO_AGERS = TagKey.create(Registries.ITEM, Exposure.resource("photo_agers"));
            public static final TagKey<Item> FLASHES = TagKey.create(Registries.ITEM, Exposure.resource("flashes"));
            public static final TagKey<Item> LENSES = TagKey.create(Registries.ITEM, Exposure.resource("lenses"));
            public static final TagKey<Item> FILTERS = TagKey.create(Registries.ITEM, Exposure.resource("filters"));

            public static final TagKey<Item> RED_FILTERS = TagKey.create(Registries.ITEM, Exposure.resource("red_filters"));
            public static final TagKey<Item> GREEN_FILTERS = TagKey.create(Registries.ITEM, Exposure.resource("green_filters"));
            public static final TagKey<Item> BLUE_FILTERS = TagKey.create(Registries.ITEM, Exposure.resource("blue_filters"));
        }

        public static class Blocks {
            public static final TagKey<Block> CHROMATIC_REFRACTORS = TagKey.create(Registries.BLOCK, Exposure.resource("chromatic_refractors"));
        }
    }

    public static class ArgumentTypes {
        public static final Supplier<ArgumentTypeInfo<ExposureSizeArgument, SingletonArgumentInfo<ExposureSizeArgument>.Template>> EXPOSURE_SIZE =
                Register.commandArgumentType("exposure_size", ExposureSizeArgument.class, SingletonArgumentInfo.contextFree(ExposureSizeArgument::new));
        public static final Supplier<ArgumentTypeInfo<ExposureLookArgument, SingletonArgumentInfo<ExposureLookArgument>.Template>> EXPOSURE_LOOK =
                Register.commandArgumentType("exposure_look", ExposureLookArgument.class, SingletonArgumentInfo.contextFree(ExposureLookArgument::new));
        public static final Supplier<ArgumentTypeInfo<ShaderLocationArgument, SingletonArgumentInfo<ShaderLocationArgument>.Template>> SHADER_LOCATION =
                Register.commandArgumentType("shader_location", ShaderLocationArgument.class, SingletonArgumentInfo.contextFree(ShaderLocationArgument::new));
        public static final Supplier<ArgumentTypeInfo<TextureLocationArgument, SingletonArgumentInfo<TextureLocationArgument>.Template>> TEXTURE_LOCATION =
                Register.commandArgumentType("texture_location", TextureLocationArgument.class, SingletonArgumentInfo.contextFree(TextureLocationArgument::new));

        public static void init() {
        }
    }
}
