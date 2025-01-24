package io.github.mortuusars.exposure.client.capture.template;

import io.github.mortuusars.exposure.client.image.PalettedImage;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.world.camera.capture.CaptureProperties;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import io.github.mortuusars.exposure.util.TranslatableError;
import io.github.mortuusars.exposure.util.UnixTimestamp;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

public interface CaptureTemplate {
    Task<?> createTask(CaptureProperties captureProperties);

    default Function<PalettedImage, ExposureData> convertToExposureData(Holder<ColorPalette> palette, ExposureData.Tag tag) {
        ResourceLocation paletteId = palette.unwrapKey().orElseThrow().location();
        return image -> new ExposureData(image.width(), image.height(), image.pixels(), paletteId, tag);
    }

    default ExposureData.Tag createExposureTag(Player executingPlayer, CaptureProperties data, boolean isLoaded) {
        return new ExposureData.Tag(data.filmType(), executingPlayer.getScoreboardName(),
                UnixTimestamp.Seconds.now(), isLoaded, false);
    }

    default @NotNull Consumer<TranslatableError> printCasualErrorInChat() {
        return err -> Minecrft.execute(() ->
                Minecrft.player().displayClientMessage(err.casual().withStyle(ChatFormatting.RED), false));
    }
}
