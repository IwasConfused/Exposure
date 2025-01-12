package io.github.mortuusars.exposure;

import io.github.mortuusars.exposure.block.FlashBlock;
import io.github.mortuusars.exposure.core.camera.component.FocalRange;
import io.github.mortuusars.exposure.data.ExposureSize;
import io.github.mortuusars.exposure.core.color.Color;
import net.neoforged.neoforge.common.ModConfigSpec;

//TODO: Restructure

/**
 * Using ForgeConfigApiPort on fabric allows using forge config in both environments and without extra dependencies on forge.
 */
public class Config {
    public static class Server {
        public static final ModConfigSpec SPEC;

        public static final ModConfigSpec.IntValue EXPOSURE_RESOLUTION;
        public static final ModConfigSpec.BooleanValue CAN_PROJECT_FROM_FILE;
        public static final ModConfigSpec.IntValue PROJECT_FROM_FILE_TIMEOUT_TICKS;

        static {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

            builder.push("Capture");
            {
                EXPOSURE_RESOLUTION = builder
                        .comment("Default size of an exposure image. High values take more disk space and can cause lag. Default: 320")
                        .defineInRange("ExposureResolution", 320, 1, 2048);
                CAN_PROJECT_FROM_FILE = builder
                        .comment("Interplanar Projector can load images from a file on client's PC. Default: true")
                        .define("LoadingFromFileEnabled", true);
                PROJECT_FROM_FILE_TIMEOUT_TICKS = builder
                        .comment("Time limit in ticks for how long image can load.",
                                "This is affecting gameplay slightly - Interplanar Projector will be consumed if loading times out.",
                                "Default: 80 (4 seconds)")
                        .defineInRange("LoadingFromFileTimeoutTicks", 80, 1, 200);
            }
            builder.pop();

            SPEC = builder.build();
        }
    }

    public static class Common {
        public static final ModConfigSpec SPEC;

        // Camera
        public static final ModConfigSpec.ConfigValue<String> CAMERA_DEFAULT_FOCAL_RANGE; //TODO: server config
        public static final ModConfigSpec.BooleanValue CAMERA_VIEWFINDER_ATTACK;
        public static final ModConfigSpec.BooleanValue CAMERA_GUI_RIGHT_CLICK_ATTACHMENTS_SCREEN;
        public static final ModConfigSpec.BooleanValue CAMERA_GUI_RIGHT_CLICK_HOTSWAP;

        // Lightroom
        public static final ModConfigSpec.IntValue LIGHTROOM_BW_PRINT_TIME;
        public static final ModConfigSpec.IntValue LIGHTROOM_COLOR_PRINT_TIME;
        public static final ModConfigSpec.IntValue LIGHTROOM_CHROMATIC_PRINT_TIME;
        public static final ModConfigSpec.IntValue LIGHTROOM_EXPERIENCE_PER_PRINT_BW;
        public static final ModConfigSpec.IntValue LIGHTROOM_EXPERIENCE_PER_PRINT_COLOR;
        public static final ModConfigSpec.IntValue LIGHTROOM_EXPERIENCE_PER_PRINT_CHROMATIC;

        // Photographs
        public static final ModConfigSpec.IntValue STACKED_PHOTOGRAPHS_MAX_SIZE;

        // Misc
        public static final ModConfigSpec.BooleanValue FILM_ROLL_RENAMING;
        public static final ModConfigSpec.BooleanValue LOOT_ADDITION;

        // Compatibility
//        public static final ModConfigSpec.BooleanValue CREATE_SPOUT_DEVELOPING_ENABLED;
//        public static final ModConfigSpec.ConfigValue<List<? extends String>> CREATE_SPOUT_DEVELOPING_SEQUENCE_COLOR;
//        public static final ModConfigSpec.ConfigValue<List<? extends String>> CREATE_SPOUT_DEVELOPING_SEQUENCE_BW;

        static {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

            builder.push("Camera");
            {
                CAMERA_DEFAULT_FOCAL_RANGE = builder
                        .comment("Default focal range of the camera (without a lens attached).",
                                "Allowed range: " + FocalRange.ALLOWED_MIN + "-" + FocalRange.ALLOWED_MAX,
                                "Default: 18-55")
                        .define("DefaultFocalRange", "18-55");

                CAMERA_VIEWFINDER_ATTACK = builder
                        .comment("Can attack while looking through Viewfinder.",
                                "Default: true")
                        .define("ViewfinderAttacking", true);

                CAMERA_GUI_RIGHT_CLICK_ATTACHMENTS_SCREEN = builder
                        .comment("Right-clicking Camera in GUI will open Camera attachments screen. Only in player inventory.",
                                "Default: true")
                        .define("RightClickAttachmentsScreen", true);

                CAMERA_GUI_RIGHT_CLICK_HOTSWAP = builder
                        .comment("Right-clicking Camera in GUI with attachment item will insert/swap it.",
                                "Default: true")
                        .define("RightClickHotswap", true);
            }
            builder.pop();

            builder.push("Lightroom");
            {
                LIGHTROOM_BW_PRINT_TIME = builder
                        .comment("Time in ticks to print black and white photograph. Default: 80")
                        .defineInRange("BlackAndWhitePrintTime", 80, 1, Integer.MAX_VALUE);
                LIGHTROOM_COLOR_PRINT_TIME = builder
                        .comment("Time in ticks to print color photograph. Default: 200")
                        .defineInRange("ColorPrintTime", 200, 1, Integer.MAX_VALUE);
                LIGHTROOM_CHROMATIC_PRINT_TIME = builder
                        .comment("Time in ticks to print one channel of a chromatic photograph. Default: 120")
                        .defineInRange("ChromaticPrintTime", 120, 1, Integer.MAX_VALUE);
                LIGHTROOM_EXPERIENCE_PER_PRINT_BW = builder
                        .comment("Amount of experience awarded per printed black and white Photograph. Set to 0 to disable. Default: 2")
                        .defineInRange("ExperiencePerPrintBW", 2, 0, 99);
                LIGHTROOM_EXPERIENCE_PER_PRINT_COLOR = builder
                        .comment("Amount of experience awarded per printed color Photograph. Set to 0 to disable. Default: 4")
                        .defineInRange("ExperiencePerPrintColor", 4, 0, 99);
                LIGHTROOM_EXPERIENCE_PER_PRINT_CHROMATIC = builder
                        .comment("Amount of experience awarded per printed chromatic Photograph (when all three channels have been printed). Set to 0 to disable. Default: 5")
                        .defineInRange("ExperiencePerPrintChromatic", 5, 0, 99);
            }
            builder.pop();

            builder.push("Photographs");
            {
                STACKED_PHOTOGRAPHS_MAX_SIZE = builder
                        .comment("How many photographs can be stacked in Stacked Photographs item. Default: 16.",
                                "Larger numbers may cause errors. Use at your own risk.")
                        .defineInRange("StackedPhotographsMaxSize", 16, 2, 64);
            }
            builder.pop();

            builder.push("Misc");
            {
                FILM_ROLL_RENAMING = builder
                        .comment("Film rolls can be renamed by using the item. Default: true")
                        .define("FilmRollRenaming", true);

                LOOT_ADDITION = builder
                        .comment("Generate items in loot chests. Default: true")
                        .define("AddItemsToLootChests", true);
            }
            builder.pop();

            builder.push("Integration");
            {
//                builder.push("Create");
//                {
//                    builder.push("SequencedSpoutFilmDeveloping");
//                    {
//                        CREATE_SPOUT_DEVELOPING_ENABLED = builder
//                                .comment("Film can be developed with create Spout Filling. Default: true")
//                                .define("Enabled", true);
//                        CREATE_SPOUT_DEVELOPING_SEQUENCE_COLOR = builder
//                                .comment("Fluid spouting sequence required to develop color film.")
//                                .defineList("ColorFilmSequence", PlatformHelper.getDefaultSpoutDevelopmentColorSequence(), o -> true);
//                        CREATE_SPOUT_DEVELOPING_SEQUENCE_BW = builder
//                                .comment("Fluid spouting sequence required to develop black and white film.")
//                                .defineList("BlackAndWhiteFilmSequence", PlatformHelper.getDefaultSpoutDevelopmentBWSequence(), o -> true);
//                    }
//                    builder.pop();
//                }
//                builder.pop();
            }
            builder.pop();

            SPEC = builder.build();
        }

//        public static ModConfigSpec.ConfigValue<List<? extends String>> spoutDevelopingSequence(ExposureType exposureType) {
//            return exposureType == ExposureType.COLOR ? CREATE_SPOUT_DEVELOPING_SEQUENCE_COLOR : CREATE_SPOUT_DEVELOPING_SEQUENCE_BW;
//        }
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

        // CAPTURE
        public static final ModConfigSpec.IntValue FLASH_CAPTURE_DELAY_TICKS;
        public static final ModConfigSpec.BooleanValue FORCE_DIRECT_SCREENSHOT_CAPTURE;
        public static final ModConfigSpec.IntValue DIRECT_CAPTURE_DELAY_FRAMES;
        public static final ModConfigSpec.BooleanValue DISABLE_POST_EFFECT;

        // VIEWFINDER
        public static final ModConfigSpec.ConfigValue<String> VIEWFINDER_BACKGROUND_COLOR;
        public static final ModConfigSpec.ConfigValue<String> VIEWFINDER_FONT_MAIN_COLOR;
        public static final ModConfigSpec.ConfigValue<String> VIEWFINDER_FONT_SECONDARY_COLOR;
        public static final ModConfigSpec.BooleanValue VIEWFINDER_MIDDLE_CLICK_CONTROLS;
        public static final ModConfigSpec.DoubleValue VIEWFINDER_ZOOM_SENSITIVITY_INFLUENCE;

        // RENDER
        public static final ModConfigSpec.BooleanValue HIDE_LOADED_PHOTOGRAPHS_MADE_BY_OTHERS;
        public static final ModConfigSpec.BooleanValue HIDE_ALL_PHOTOGRAPHS_MADE_BY_OTHERS;
        public static final ModConfigSpec.IntValue PHOTOGRAPH_FRAME_CULLING_DISTANCE;
        public static final ModConfigSpec.BooleanValue PHOTOGRAPH_RENDERS_IN_ITEM_FRAME;

        // INTEGRATION
        public static final ModConfigSpec.BooleanValue SHOW_JEI_INFORMATION;
        public static final ModConfigSpec.BooleanValue REAL_CAMERA_DISABLE_IN_VIEWFINDER;

        // IMAGE SAVING
        public static final ModConfigSpec.BooleanValue SAVE_EXPOSURE_TO_FILE_WHEN_VIEWED;
        public static final ModConfigSpec.BooleanValue EXPOSURE_SAVING_LEVEL_SUBFOLDER;
        public static final ModConfigSpec.EnumValue<ExposureSize> EXPOSURE_SAVING_SIZE;

        static {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

            {
                builder.push("UI");

                RECIPE_TOOLTIPS_WITHOUT_JEI = builder
                        .comment("Tooltips for Developing Film Rolls and Copying Photographs will be shown on Film Rolls and Photographs respectively, describing the crafting recipe. ",
                                "Only when JEI is not installed. (Only JEI shows these recipes, not REI or EMI)")
                        .define("RecipeTooltipsWithoutJei", true);

                CAMERA_SHOW_TOOLTIP_DETAILS = builder
                        .comment("Details about Camera configuring will be shown in Camera item tooltip.")
                        .define("CameraDetailsInTooltip", true);

                CAMERA_SHOW_FILM_FRAMES_IN_TOOLTIP = builder
                        .comment("Film Roll Frames will be shown in the camera tooltip.",
                                "Default: true")
                        .define("CameraFilmFramesTooltip", true);

                CAMERA_SHOW_FILM_BAR_ON_ITEM = builder
                        .comment("Film Roll fullness bar will be shown on the Camera item. Same as it does on Film Roll item.",
                                "Default: false")
                        .define("CameraShowsFilmBar", false);

                PHOTOGRAPH_SHOW_PHOTOGRAPHER_IN_TOOLTIP = builder
                        .comment("Photographer name will be shown in Photograph's tooltip.")
                        .define("PhotographPhotographerNameTooltip", false);

                PHOTOGRAPH_IN_HAND_HIDE_CROSSHAIR = builder
                        .comment("Crosshair will not get in the way when holding a photograph.")
                        .define("PhotographInHandHideCrosshair", true);

                ALBUM_SHOW_PHOTOS_COUNT = builder
                        .comment("Album will show how many photographs they contain in a tooltip.")
                        .define("AlbumShowPhotosCount", true);

                SIGNED_ALBUM_GLINT = builder
                        .comment("Signed Album item will have an enchantment glint.")
                        .define("SignedAlbumGlint", true);

                DIFFERENT_DEVELOPING_POTION_COLORS = builder
                        .comment("Mundane, Awkward and Thick potions will have their color changed slightly, so it's easier to tell them apart. Default: true")
                        .define("DifferentDevelopingPotionsColors", true);

                {
                    builder.push("Viewfinder");
                    VIEWFINDER_BACKGROUND_COLOR = builder.define("BackgroundColor", "FA1F1D1B");
                    VIEWFINDER_FONT_MAIN_COLOR = builder.define("FontMainColor", "FF2B2622");
                    VIEWFINDER_FONT_SECONDARY_COLOR = builder.define("FontSecondaryColor", "FF7A736C");
                    VIEWFINDER_MIDDLE_CLICK_CONTROLS = builder
                            .comment("Clicking middle mouse button will open Viewfinder Controls. This is independent of Open Camera Controls keybind.",
                                    "Allows opening camera controls without dismounting from a vehicle - and keeping controls on sneak or other button as well.",
                                    "Default: true")
                            .define("MiddleClickOpensControls", true);
                    VIEWFINDER_ZOOM_SENSITIVITY_INFLUENCE = builder
                            .comment("How much zooming influences mouse sensitivity." +
                                    "0 - no change to sensitivity.",
                                    "1 - full effect.",
                                    "Default: 0.75")
                            .defineInRange("ZoomSensitivityInfluence", 0.75, 0.0, 1.0);
                    builder.pop();
                }

                builder.pop();
            }

            {
                builder.push("Capture");
                FORCE_DIRECT_SCREENSHOT_CAPTURE = builder
                        .comment("Force legacy (pre 1.21) capturing method for taking images. Enable if you experiencing issues with resulting images.",
                                "If Iris or Oculus is installed legacy method will be used regardless of this setting.",
                                "Default: false")
                        .define("ForceDirectScreenshotCapture", false);
                DIRECT_CAPTURE_DELAY_FRAMES = builder
                        .comment("Delay in frames before capturing an image.",
                                "Set to higher value when leftovers of GUI elements (such as nameplates) are visible on the images",
                                "(some shaders have temporal effects that take several frames to disappear fully)")
                        .defineInRange("CaptureDelayFrames", 0, 0, 100);
                FLASH_CAPTURE_DELAY_TICKS = builder
                        .comment("Delay in ticks before capturing an image when shooting with flash." +
                                "\nIf you experience flash synchronization issues (Flash having no effect on the image) - try increasing the value.")
                        .defineInRange("FlashCaptureDelayTicks", 4, 1, FlashBlock.LIFETIME_TICKS);
                DISABLE_POST_EFFECT = builder
                        .comment("Post Effect (vanilla shader) will be disabled when image is captured.",
                                "It is sometimes used by mods to change how player sees the world. (Cold Sweat's overheating blur, Supplementaries mob heads, for example).",
                                "In vanilla, it's only used when spectating a creeper/enderman/etc.",
                                "Default: true")
                        .define("DisablePostEffect", true);
                builder.pop();
            }

            {
                builder.push("Render");

                HIDE_LOADED_PHOTOGRAPHS_MADE_BY_OTHERS = builder
                        .comment("Projected photographs (using Interplanar Projector) made by other players will not be rendered.")
                        .define("HideProjectedPhotographsMadeByOthers", false);

                HIDE_ALL_PHOTOGRAPHS_MADE_BY_OTHERS = builder
                        .comment("All photographs made by other players will not be rendered.")
                        .define("HideAllPhotographsMadeByOthers", false);

                PHOTOGRAPH_FRAME_CULLING_DISTANCE = builder
                        .comment("Distance from the player beyond which Photograph Frame would not be rendered. Default: 64",
                                "Note: this number may not relate to distance in blocks exactly. It's influenced by onRender distance and entity distance settings.")
                        .defineInRange("PhotographFrameCullingDistance", 64, 8, 128);

                PHOTOGRAPH_RENDERS_IN_ITEM_FRAME = builder
                        .comment("Photographs in Item Frame will be rendered as an image instead of an item icon. Default: false")
                        .define("PhotographRendersInItemFrame", false);

                builder.pop();
            }

            {
                builder.push("Integration");
                SHOW_JEI_INFORMATION = builder
                        .comment("Useful information about some items will be shown in JEI description category. Default: true")
                        .define("JeiInformation", true);
                REAL_CAMERA_DISABLE_IN_VIEWFINDER = builder
                        .comment("[Real Camera] Disable player model rendering when looking through viewfinder. Default: true")
                        .define("RealCameraDisableInViewfinder", true);
                builder.pop();
            }

            {
                builder.push("FileSaving");
                SAVE_EXPOSURE_TO_FILE_WHEN_VIEWED = builder
                        .comment("When the Photograph is viewed in UI, image will be saved to 'exposures' folder as a png.")
                        .define("SavePhotographs", true);
                EXPOSURE_SAVING_LEVEL_SUBFOLDER = builder
                        .comment("When saving, exposures will be placed into folder corresponding to current world name.")
                        .define("WorldNameSubfolder", true);
                EXPOSURE_SAVING_SIZE = builder
                        .comment("Saved exposures will be enlarged by this multiplier.",
                                "Given the default exposure size of 320 - this will produce:",
                                "320/640/960/1280px png image. Be careful with larger frame sizes.",
                                "Default: X2")
                        .defineEnum("Size", ExposureSize.X2);

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
