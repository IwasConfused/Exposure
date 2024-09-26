package io.github.mortuusars.exposure.core.frame;

import com.mojang.datafixers.util.Either;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FrameProperties {
    public static final String ID = "Id";
    public static final String TEXTURE = "Texture";
    public static final String TYPE = "Type";
    public static final String CHROMATIC = "Chromatic";
    public static final String CHROMATIC_CHANNEL = "ChromaticChannel";
    public static final String RED_CHANNEL = "RedChannel";
    public static final String GREEN_CHANNEL = "GreenChannel";
    public static final String BLUE_CHANNEL = "BlueChannel";
    public static final String SHUTTER_SPEED_MS = "ShutterSpeedMS";
    public static final String FOCAL_LENGTH = "FocalLength";
    public static final String TIMESTAMP = "Timestamp";
    public static final String PHOTOGRAPHER = "Photographer";
    public static final String PHOTOGRAPHER_ID = "PhotographerId";
    public static final String PROJECTED = "Projected";
    public static final String FLASH = "Flash";
    public static final String SELFIE = "Selfie";
    public static final String POSITION = "Pos";
    public static final String DIMENSION = "Dimension";
    public static final String BIOME = "Biome";
    public static final String UNDERWATER = "Underwater";
    public static final String IN_CAVE = "InCave";
    public static final String WEATHER = "Weather";
    public static final String LIGHT_LEVEL = "LightLevel";
    public static final String SUN_ANGLE = "SunAngle";
    public static final String ENTITIES_IN_FRAME = "Entities";

    public static final String ENTITY_ID = "Id";
    public static final String ENTITY_POSITION = "Pos";
    public static final String ENTITY_DISTANCE = "Distance";
    public static final String ENTITY_PLAYER_NAME = "Name";
    public static final String DAYTIME = "DayTime";

//    /**
//     * If both are defined - ID takes priority.
//     * @return 'Either.left("")' if nothing is defined.
//     */
//    public static Either<String, ResourceLocation> getIdOrTexture(@NotNull CompoundTag tag) {
//        String exposureId = tag.getString(ID);
//        if (!exposureId.isEmpty())
//            return Either.left(exposureId);
//
//        String texture = tag.getString(TEXTURE);
//        if (!texture.isEmpty())
//            return Either.right(new ResourceLocation(texture));
//
//        return Either.left("");
//    }
//
//    /**
//     * If both are defined - ID takes priority.
//     * @return 'Either.left("")' if nothing is defined or stack does not have a tag.
//     */
//    public static Either<String, ResourceLocation> getIdOrTexture(ItemStack photographStack) {
//        if (photographStack.getTag() == null) {
//            return Either.left("");
//        }
//
//        return getIdOrTexture(photographStack.getTag());
//    }
//
//    public static boolean hasIdOrTexture(ItemStack photographStack) {
//        CompoundTag tag = photographStack.getTag();
//        if (tag == null) {
//            return false;
//        }
//
//        String exposureId = tag.getString(ID);
//        if (!exposureId.isEmpty())
//            return true;
//
//        String texture = tag.getString(TEXTURE);
//        if (!texture.isEmpty())
//            return true;
//
//        return false;
//    }
}
