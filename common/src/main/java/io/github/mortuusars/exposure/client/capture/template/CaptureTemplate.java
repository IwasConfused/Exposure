package io.github.mortuusars.exposure.client.capture.template;

import io.github.mortuusars.exposure.client.image.PalettedImage;
import io.github.mortuusars.exposure.client.image.processor.Processor;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.core.CaptureProperties;
import io.github.mortuusars.exposure.core.ExposureType;
import io.github.mortuusars.exposure.core.camera.PhotographerEntity;
import io.github.mortuusars.exposure.core.cycles.task.Task;
import io.github.mortuusars.exposure.core.warehouse.ExposureData;
import io.github.mortuusars.exposure.util.TranslatableError;
import io.github.mortuusars.exposure.util.UnixTimestamp;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

public interface CaptureTemplate {
    Task<?> createTask(CaptureProperties captureProperties);

    default Processor chooseColorProcessor(CaptureProperties data) {
        return data.filmType() == ExposureType.BLACK_AND_WHITE
                ? data.chromaChannel().map(Processor::singleChannelBlackAndWhite).orElse(Processor.blackAndWhite(1.15f))
                : Processor.EMPTY;
    }

    default Function<PalettedImage, ExposureData> convertToExposureData(ResourceLocation paletteId, ExposureData.Tag tag) {
        return image -> new ExposureData(image.width(), image.height(), image.pixels(), paletteId, tag);
    }

    default ExposureData.Tag createExposureTag(PhotographerEntity photographer, CaptureProperties data, boolean isFromFile) {
        return new ExposureData.Tag(data.filmType(), photographer.getExecutingPlayer().getScoreboardName(),
                UnixTimestamp.Seconds.now(), isFromFile, false);
    }

    default @NotNull Consumer<TranslatableError> printCasualErrorInChat() {
        return err -> Minecrft.execute(() ->
                Minecrft.player().displayClientMessage(err.casual().withStyle(ChatFormatting.RED), false));
    }
}
