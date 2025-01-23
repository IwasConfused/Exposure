package io.github.mortuusars.exposure.advancements.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public record CameraPredicate(Optional<ItemPredicate> camera,
                              Optional<MinMaxBounds.Doubles> shutterSpeedMS,
                              Optional<MinMaxBounds.Ints> focalLength,
                              Optional<ItemPredicate> film,
                              Optional<ItemPredicate> flash,
                              Optional<ItemPredicate> lens,
                              Optional<ItemPredicate> filter) {

    public static final Codec<CameraPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ItemPredicate.CODEC.optionalFieldOf("camera").forGetter(CameraPredicate::camera),
                    MinMaxBounds.Doubles.CODEC.optionalFieldOf("shutter_speed_ms").forGetter(CameraPredicate::shutterSpeedMS),
                    MinMaxBounds.Ints.CODEC.optionalFieldOf("focal_length").forGetter(CameraPredicate::focalLength),
                    ItemPredicate.CODEC.optionalFieldOf("film").forGetter(CameraPredicate::film),
                    ItemPredicate.CODEC.optionalFieldOf("flash").forGetter(CameraPredicate::flash),
                    ItemPredicate.CODEC.optionalFieldOf("lens").forGetter(CameraPredicate::lens),
                    ItemPredicate.CODEC.optionalFieldOf("filter").forGetter(CameraPredicate::filter))
            .apply(instance, CameraPredicate::new));

    public boolean matches(ItemAndStack<CameraItem> cameraItemAndStack) {
        ItemStack stack = cameraItemAndStack.getItemStack();
        CameraItem item = cameraItemAndStack.getItem();

        return (camera.isEmpty() || camera.get().test(stack));
//                && (shutterSpeedMS.isEmpty() || shutterSpeedMS.get().matches(item.getShutterSpeed(stack).getDurationMilliseconds()))
//                && (focalLength.isEmpty() || focalLength.get().matches(Mth.ceil(item.getZoomPercentage(stack)))) //TODO wrong
//                && (film.isEmpty() || film.get().test(item.getAttachment(stack, Attachment.FILM).getForReading()))
//                && (flash.isEmpty() || flash.get().test(item.getAttachment(stack, Attachment.FLASH).getForReading()))
//                && (lens.isEmpty() || lens.get().test(item.getAttachment(stack, Attachment.LENS).getForReading()))
//                && (filter.isEmpty() || filter.get().test(item.getAttachment(stack, Attachment.FILTER).getForReading()));
    }
}
