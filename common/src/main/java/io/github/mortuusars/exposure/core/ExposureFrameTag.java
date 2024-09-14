package io.github.mortuusars.exposure.core;

import io.github.mortuusars.exposure.core.frame.Photographer;
import io.github.mortuusars.exposure.item.component.EntityInFrame;
import io.github.mortuusars.exposure.item.component.ExposureFrame;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;

import java.util.List;
import java.util.Optional;

public class ExposureFrameTag extends CompoundTag {
    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String IS_FROM_FILE = "is_from_file";
    public static final String CHROMATIC = "chromatic";
    public static final String PHOTOGRAPHER_NAME = "photographer";
    public static final String PHOTOGRAPHER_ID = "photographer_id";

    public static final String CHROMATIC_CHANNEL = "chromatic_channel";
//    public static final String RED_CHANNEL = "red_channel";
//    public static final String GREEN_CHANNEL = "green_channel";
//    public static final String BLUE_CHANNEL = "blue_channel";
    public static final String SHUTTER_SPEED_MS = "shutter_speed_ms";
    public static final String FOCAL_LENGTH = "focal_length";
    public static final String TIMESTAMP = "timestamp";

    public static final String FLASH = "flash";
    public static final String SELFIE = "selfie";
    public static final String POSITION = "pos";
    public static final String DIMENSION = "dimension";
    public static final String BIOME = "biome";
    public static final String UNDERWATER = "underwater";
    public static final String IN_CAVE = "in_cave";
    public static final String WEATHER = "weather";
    public static final String LIGHT_LEVEL = "light_level";
}
