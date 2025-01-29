package io.github.mortuusars.exposure;

import io.github.mortuusars.exposure.world.block.FlashBlock;
import io.github.mortuusars.exposure.world.camera.component.FocalRange;
import io.github.mortuusars.exposure.util.color.Color;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Using ForgeConfigApiPort on fabric allows using forge config in both environments and without extra dependencies on forge.
 */
public class Config {
    public static class Server {
        public static final ModConfigSpec SPEC;

        // Camera
        public static final ModConfigSpec.ConfigValue<String> CAMERA_DEFAULT_FOCAL_RANGE;
        public static final ModConfigSpec.BooleanValue CAMERA_VIEWFINDER_ATTACK;
        public static final ModConfigSpec.BooleanValue CAMERA_GUI_RIGHT_CLICK_OPEN_ATTACHMENTS;
        public static final ModConfigSpec.BooleanValue CAMERA_GUI_RIGHT_CLICK_HOTSWAP;

        // Capture
        public static final ModConfigSpec.IntValue DEFAULT_FRAME_SIZE;
        public static final ModConfigSpec.BooleanValue CAN_PROJECT;
        public static final ModConfigSpec.IntValue PROJECT_TIMEOUT_TICKS;

        // Lightroom
        public static final ModConfigSpec.IntValue LIGHTROOM_BW_PRINT_TIME;
        public static final ModConfigSpec.IntValue LIGHTROOM_COLOR_PRINT_TIME;
        public static final ModConfigSpec.IntValue LIGHTROOM_CHROMATIC_PRINT_TIME;
        public static final ModConfigSpec.IntValue LIGHTROOM_BW_EXPERIENCE;
        public static final ModConfigSpec.IntValue LIGHTROOM_COLOR_EXPERIENCE;
        public static final ModConfigSpec.IntValue LIGHTROOM_CHROMATIC_EXPERIENCE;

        // Photographs
        public static final ModConfigSpec.IntValue STACKED_PHOTOGRAPHS_MAX_SIZE;

        // Misc
        public static final ModConfigSpec.BooleanValue FILM_ROLL_EASY_RENAMING;
        public static final ModConfigSpec.BooleanValue INTERPLANAR_PROJECTOR_LARGER_RENAMING_LIMIT;


        static {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

            {
                builder.push("camera");
                CAMERA_DEFAULT_FOCAL_RANGE = builder
                        .comment("Default focal range of the camera (without a lens attached).",
                                "Allowed range: " + FocalRange.ALLOWED_MIN + "-" + FocalRange.ALLOWED_MAX,
                                "Default: 18-55")
                        .define("default_focal_range", "18-55");
                CAMERA_VIEWFINDER_ATTACK = builder
                        .comment("Can attack while looking through Viewfinder.",
                                "Default: true")
                        .define("viewfinder_attacking", true);
                CAMERA_GUI_RIGHT_CLICK_OPEN_ATTACHMENTS = builder
                        .comment("Right-clicking a Camera in GUI will open Camera Attachments screen. Only in player inventory.",
                                "Default: true")
                        .define("right_click_attachments_screen", true);
                CAMERA_GUI_RIGHT_CLICK_HOTSWAP = builder
                        .comment("Right-clicking Camera in GUI with attachment item will insert/swap it.",
                                "Default: true")
                        .define("right_click_hotswap", true);
                builder.pop();
            }

            {
                builder.push("capture");
                DEFAULT_FRAME_SIZE = builder
                        .comment("Default size of an exposure image. High values take more disk space and can cause lag. Default: 320")
                        .defineInRange("default_frame_size", 320, 1, 2048);
                CAN_PROJECT = builder
                        .comment("Interplanar Projector can load images from URL or file on client's PC. Default: true")
                        .define("projecting_enabled", true);
                PROJECT_TIMEOUT_TICKS = builder
                        .comment("Time limit in ticks for projecting.",
                                "This is affecting gameplay slightly - Interplanar Projector will be consumed if loading times out.",
                                "Default: 100 (5 seconds)")
                        .defineInRange("projecting_timeout_ticks", 100, 1, 200);
                builder.pop();
            }

            {
                builder.push("lightroom");
                LIGHTROOM_BW_PRINT_TIME = builder
                        .comment("Time in ticks to print black and white photograph. Default: 80")
                        .defineInRange("print_time_black_and_white", 80, 1, Integer.MAX_VALUE);
                LIGHTROOM_COLOR_PRINT_TIME = builder
                        .comment("Time in ticks to print color photograph. Default: 200")
                        .defineInRange("print_time_color", 200, 1, Integer.MAX_VALUE);
                LIGHTROOM_CHROMATIC_PRINT_TIME = builder
                        .comment("Time in ticks to print one channel of a chromatic photograph. Default: 120")
                        .defineInRange("print_time_chromatic", 120, 1, Integer.MAX_VALUE);
                LIGHTROOM_BW_EXPERIENCE = builder
                        .comment("Amount of experience awarded per printed black and white Photograph. Set to 0 to disable. Default: 2")
                        .defineInRange("experience_black_and_white", 2, 0, 99);
                LIGHTROOM_COLOR_EXPERIENCE = builder
                        .comment("Amount of experience awarded per printed color Photograph. Set to 0 to disable. Default: 4")
                        .defineInRange("experience_color", 4, 0, 99);
                LIGHTROOM_CHROMATIC_EXPERIENCE = builder
                        .comment("Amount of experience awarded per printed chromatic Photograph (when all three channels have been printed). Set to 0 to disable. Default: 5")
                        .defineInRange("experience_chromatic", 5, 0, 99);
                builder.pop();
            }

            {
                builder.push("photographs");
                STACKED_PHOTOGRAPHS_MAX_SIZE = builder
                        .comment("How many photographs can be stacked in Stacked Photographs item. Default: 16.",
                                "Larger numbers may cause errors. Use at your own risk. 32 should be fine though.")
                        .defineInRange("stacked_photographs_size", 16, 2, 64);
                builder.pop();
            }

            builder.push("misc");
            {
                FILM_ROLL_EASY_RENAMING = builder
                        .comment("Film rolls can be renamed by using the item. No experience cost. Default: true")
                        .define("film_roll_easy_renaming", true);
                INTERPLANAR_PROJECTOR_LARGER_RENAMING_LIMIT = builder
                        .comment("Increases item name length limit for Interplanar Projector to 150 characters. Vanilla limit: 50.",
                                "Default: true")
                        .define("increase_interplanar_projector_name_limit", true);
            }
            builder.pop();

            SPEC = builder.build();
        }
    }

    public static class Common {
        public static final ModConfigSpec SPEC;
        public static final ModConfigSpec.BooleanValue GENERATE_LOOT;

        static {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

            builder.push("misc");
            {
                GENERATE_LOOT = builder
                        .comment("Generate photographs and film rolls in loot chests. Default: true")
                        .define("loot_chests", true);
            }
            builder.pop();

            SPEC = builder.build();
        }
    }

    public static class Client {
        public static final ModConfigSpec SPEC;

        // UI
        public static final ModConfigSpec.BooleanValue RECIPE_TOOLTIPS_WITHOUT_JEI;
        public static final ModConfigSpec.BooleanValue CAMERA_SHOW_TOOLTIP_DETAILS;
        public static final ModConfigSpec.BooleanValue CAMERA_SHOW_FILM_FRAMES_IN_TOOLTIP;
        public static final ModConfigSpec.BooleanValue CAMERA_SHOW_FILM_BAR_ON_ITEM;
        public static final ModConfigSpec.BooleanValue PHOTOGRAPH_SHOW_PHOTOGRAPHER_IN_TOOLTIP;
        public static final ModConfigSpec.BooleanValue PHOTOGRAPH_IN_HAND_HIDE_CROSSHAIR;
        public static final ModConfigSpec.BooleanValue SIGNED_ALBUM_GLINT;
        public static final ModConfigSpec.BooleanValue ALBUM_SHOW_PHOTOS_COUNT;
        public static final ModConfigSpec.BooleanValue DIFFERENT_DEVELOPING_POTION_COLORS;

        // VIEWFINDER
        public static final ModConfigSpec.BooleanValue VIEWFINDER_MIDDLE_CLICK_CONTROLS;
        public static final ModConfigSpec.BooleanValue WAIST_LEVEL_VIEWFINDER;
        public static final ModConfigSpec.DoubleValue VIEWFINDER_ZOOM_SENSITIVITY_INFLUENCE;
        public static final ModConfigSpec.ConfigValue<String> VIEWFINDER_BACKGROUND_COLOR;
        public static final ModConfigSpec.ConfigValue<String> VIEWFINDER_FONT_MAIN_COLOR;
        public static final ModConfigSpec.ConfigValue<String> VIEWFINDER_FONT_SECONDARY_COLOR;

        // CAPTURE
        public static final ModConfigSpec.BooleanValue KEEP_POST_EFFECT;
        public static final ModConfigSpec.IntValue FLASH_CAPTURE_DELAY_TICKS;
        public static final ModConfigSpec.BooleanValue FORCE_DIRECT_CAPTURE;
        public static final ModConfigSpec.IntValue DIRECT_CAPTURE_DELAY_FRAMES;

        // RENDER
        public static final ModConfigSpec.BooleanValue PHOTOGRAPH_RENDERS_IN_ITEM_FRAME;
        public static final ModConfigSpec.BooleanValue HIDE_PROJECTED_PHOTOGRAPHS_MADE_BY_OTHERS;
        public static final ModConfigSpec.BooleanValue HIDE_ALL_PHOTOGRAPHS_MADE_BY_OTHERS;
        public static final ModConfigSpec.IntValue PHOTOGRAPH_FRAME_CULLING_DISTANCE;

        // INTEGRATION
        public static final ModConfigSpec.BooleanValue SHOW_JEI_INFORMATION;
        public static final ModConfigSpec.BooleanValue REAL_CAMERA_DISABLE_IN_VIEWFINDER;

        // IMAGE SAVING
        public static final ModConfigSpec.BooleanValue EXPORT_PHOTOGRAPH_WHEN_VIEWED;
        public static final ModConfigSpec.BooleanValue EXPORT_ORGANIZE_BY_WORLD;
        public static final ModConfigSpec.IntValue EXPORT_SIZE_MULTIPLIER;

        static {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

            {
                builder.push("ui");

                RECIPE_TOOLTIPS_WITHOUT_JEI = builder
                        .comment("Tooltips for Developing Film Rolls and Copying Photographs will be shown on Film Rolls and Photographs respectively, describing the crafting recipe. ",
                                "Only when JEI is not installed. (Only JEI shows these recipes, not REI or EMI)")
                        .define("recipe_tooltips_without_jei", true);

                CAMERA_SHOW_TOOLTIP_DETAILS = builder
                        .comment("Details about Camera configuring will be shown in Camera item tooltip.")
                        .define("camera_details_tooltip", true);

                CAMERA_SHOW_FILM_FRAMES_IN_TOOLTIP = builder
                        .comment("Film Roll Frames will be shown in the camera tooltip.",
                                "Default: true")
                        .define("camera_film_frames_tooltip", true);

                CAMERA_SHOW_FILM_BAR_ON_ITEM = builder
                        .comment("Film Roll fullness bar will be shown on the Camera item.",
                                "Default: false")
                        .define("camera_shows_film_bar", false);

                PHOTOGRAPH_SHOW_PHOTOGRAPHER_IN_TOOLTIP = builder
                        .comment("Photographer name will be shown in Photograph's tooltip.")
                        .define("photograph_photographer_name_tooltip", false);

                PHOTOGRAPH_IN_HAND_HIDE_CROSSHAIR = builder
                        .comment("Crosshair will not get in the way when holding a photograph.")
                        .define("photograph_in_hand_hide_crosshair", true);

                ALBUM_SHOW_PHOTOS_COUNT = builder
                        .comment("Album will show how many photographs it contains in a tooltip.")
                        .define("album_show_photos_count", true);

                SIGNED_ALBUM_GLINT = builder
                        .comment("Signed Album item will have an enchantment glint.")
                        .define("signed_album_glint", true);

                DIFFERENT_DEVELOPING_POTION_COLORS = builder
                        .comment("Mundane, Awkward and Thick potions will have their color changed slightly, so it's easier to tell them apart. Default: true")
                        .define("different_developing_potions_colors", true);

                builder.pop();
            }

            {
                builder.push("viewfinder");
                VIEWFINDER_MIDDLE_CLICK_CONTROLS = builder
                        .comment("Clicking middle mouse button will open Viewfinder Controls. This is independent of Open Camera Controls keybind.",
                                "Allows opening camera controls without dismounting from a vehicle - and keeping controls on sneak or other button as well.",
                                "Default: true")
                        .define("middle_click_controls", true);
                WAIST_LEVEL_VIEWFINDER = builder
                        .comment("Shifts view down to match waist-level camera position. Default: false.")
                        .define("waist_level_viewfinder", false);

                VIEWFINDER_ZOOM_SENSITIVITY_INFLUENCE = builder
                        .comment("How much zooming influences mouse sensitivity.",
                                "0 - no change to sensitivity. 1 - full effect.",
                                "Default: 0.75")
                        .defineInRange("zoom_sensitivity_influence", 0.75, 0.0, 1.0);
                VIEWFINDER_BACKGROUND_COLOR = builder
                        .comment("Color in hex format. AARRGGBB.").define("background_color", "FA1F1D1B");
                VIEWFINDER_FONT_MAIN_COLOR = builder
                        .comment("Color in hex format. AARRGGBB.").define("font_main_color", "FF2B2622");
                VIEWFINDER_FONT_SECONDARY_COLOR = builder
                        .comment("Color in hex format. AARRGGBB.").define("font_secondary_color", "FF7A736C");
                builder.pop();
            }

            {
                builder.push("capture");
                KEEP_POST_EFFECT = builder
                        .comment("Keep Post Effect (vanilla shader) when capturing an image.",
                                "It is sometimes used by mods to change how player sees the world. (Cold Sweat's overheating blur, Supplementaries mob heads, for example).",
                                "In vanilla, it's only used when spectating a creeper/enderman/etc.",
                                "Default: false")
                        .define("keep_post_effect", false);
                FLASH_CAPTURE_DELAY_TICKS = builder
                        .comment("Delay in ticks before capturing an image when shooting with flash." +
                                "\nIf you experience flash synchronization issues (Flash having no effect on the image) - try increasing the value.")
                        .defineInRange("flash_capture_delay_ticks", 4, 1, FlashBlock.LIFETIME_TICKS);
                FORCE_DIRECT_CAPTURE = builder
                        .comment("Force legacy (pre 1.21) capturing method for taking images. Enable if you experiencing issues with resulting images.",
                                "If Iris or Oculus is installed legacy method will be used regardless of this setting.",
                                "Default: false")
                        .define("force_direct_capture", false);
                DIRECT_CAPTURE_DELAY_FRAMES = builder
                        .comment("Delay in frames before capturing an image if 'direct_capture' method is in use (or if Oculus or Iris is installed).",
                                "Set to higher value when leftovers of GUI elements (such as nameplates) are visible on the images",
                                "(some shaders have temporal effects that take several frames to disappear fully)")
                        .defineInRange("direct_capture_delay_frames", 0, 0, 100);
                builder.pop();
            }

            {
                builder.push("render");
                PHOTOGRAPH_RENDERS_IN_ITEM_FRAME = builder
                        .comment("Photographs in Item Frame will be rendered as image instead of an item icon. Default: false")
                        .define("photograph_renders_in_item_frame", false);
                HIDE_PROJECTED_PHOTOGRAPHS_MADE_BY_OTHERS = builder
                        .comment("Projected photographs (using Interplanar Projector) made by other players will be censored (pixelated).")
                        .define("censor_projected_photographs_made_by_others", false);
                HIDE_ALL_PHOTOGRAPHS_MADE_BY_OTHERS = builder
                        .comment("All photographs made by other players will will be censored (pixelated).")
                        .define("censor_all_photographs_made_by_others", false);
                PHOTOGRAPH_FRAME_CULLING_DISTANCE = builder
                        .comment("Distance from the player beyond which Photograph Frame would not be rendered. Default: 64",
                                "Note: this number may not relate to distance in blocks exactly. It's influenced by render distance and entity distance settings.")
                        .defineInRange("photograph_frame_culling_distance", 64, 8, 128);
                builder.pop();
            }

            {
                builder.push("integration");
                SHOW_JEI_INFORMATION = builder
                        .comment("Useful information about some items will be shown in JEI description category. Default: true")
                        .define("jei_information", true);
                REAL_CAMERA_DISABLE_IN_VIEWFINDER = builder
                        .comment("[Real Camera] Disable player model rendering when looking through viewfinder. Default: true")
                        .define("real_camera_disable_in_viewfinder", true);
                builder.pop();
            }

            {
                builder.push("export");
                EXPORT_PHOTOGRAPH_WHEN_VIEWED = builder
                        .comment("When the Photograph you took is viewed in UI (by using a Photograph), image will be exported to '<instance>/exposures' folder as a png.")
                        .define("export_viewed_photographs", true);
                EXPORT_ORGANIZE_BY_WORLD = builder
                        .comment("When exporting, exposures will be organized to subfolders with world name.")
                        .define("export_organize_by_world", true);
                EXPORT_SIZE_MULTIPLIER = builder
                        .comment("Exported exposures will be scaled by this multiplier.",
                                "Given the default exposure size of 320 pixels - this will produce:",
                                "320/640/960/1280/etc image. Be careful with larger frame sizes.",
                                "Default: 2")
                        .defineInRange("export_size_multiplier", 2, 1, 10);

                builder.pop();
            }

            SPEC = builder.build();
        }

        public static int getBackgroundColor() {
            return Color.fromHex(VIEWFINDER_BACKGROUND_COLOR.get()).getARGB();
        }

        public static int getMainFontColor() {
            return Color.fromHex(VIEWFINDER_FONT_MAIN_COLOR.get()).getARGB();
        }

        public static int getSecondaryFontColor() {
            return Color.fromHex(VIEWFINDER_FONT_SECONDARY_COLOR.get()).getARGB();
        }
    }
}