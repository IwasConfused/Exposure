package io.github.mortuusars.exposure.command.exposure;

import com.google.gson.*;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.JsonOps;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.command.argument.ColorPaletteArgument;
import io.github.mortuusars.exposure.core.color.Color;
import io.github.mortuusars.exposure.core.color.ColorPalette;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;

import static net.minecraft.commands.Commands.*;

public class PaletteCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        return literal("palette")
                .then(literal("export")
                        .then(argument("palette", new ColorPaletteArgument())
                                .then(literal("json")
                                        .then(argument("file_path", StringArgumentType.string())
                                                .executes(context -> exportAsJson(context.getSource(),
                                                        ColorPaletteArgument.getId(context, "palette"),
                                                        StringArgumentType.getString(context, "file_path")))))
                                .then(literal("png")
                                        .then(argument("file_path", StringArgumentType.string())
                                                .executes(context -> exportAsPng(context.getSource(),
                                                        ColorPaletteArgument.getId(context, "palette"),
                                                        StringArgumentType.getString(context, "file_path")))))))
                .then(literal("convert")
                        .then(literal("json_to_png")
                                .then(argument("file_path", StringArgumentType.string())
                                        .executes(context -> convertJsonToPng(context.getSource(),
                                                StringArgumentType.getString(context, "file_path")))))
                        .then(literal("png_to_json")
                                .then(argument("file_path", StringArgumentType.string())
                                        .executes(context -> convertPngToJson(context.getSource(),
                                                StringArgumentType.getString(context, "file_path"))))));
    }

    private static int exportAsJson(CommandSourceStack source, ResourceLocation paletteId, String filePath) {
        @Nullable ColorPalette palette = source.registryAccess().registryOrThrow(Exposure.Registries.COLOR_PALETTE).get(paletteId);
        if (palette == null) {
            source.sendFailure(Component.literal(paletteId + " is not found."));
            return 0;
        }

        try {
            savePaletteAsJson(palette, new File(filePath));
        } catch (Exception e) {
            Exposure.LOGGER.error("Exporting palette '{}' failed: ", paletteId, e);
            source.sendFailure(Component.literal("Exporting palette '" + paletteId + "' failed: " + e.getMessage()));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Exported palette '" + paletteId + "' to file ")
                .append(Component.literal(filePath).withStyle(Style.EMPTY
                        .withUnderlined(true))), true);

        return 0;
    }

    private static int exportAsPng(CommandSourceStack source, ResourceLocation paletteId, String filePath) {
        @Nullable ColorPalette palette = source.registryAccess().registryOrThrow(Exposure.Registries.COLOR_PALETTE).get(paletteId);
        if (palette == null) {
            source.sendFailure(Component.literal(paletteId + " is not found."));
            return 0;
        }

        try {
            savePaletteAsPng(palette, new File(filePath));
            source.sendSuccess(() -> Component.literal("Exported palette '" + paletteId + "' to file ")
                    .append(Component.literal(filePath).withStyle(Style.EMPTY
                            .withUnderlined(true))), true);
        } catch (Exception e) {
            Exposure.LOGGER.error("Exporting palette '{}' failed: ", paletteId, e);
            source.sendFailure(Component.literal("Exporting palette '" + paletteId + "' failed: " + e.getMessage()));
        }

        return 0;
    }

    private static int convertJsonToPng(CommandSourceStack source, String filePath) {
        try {
            File file = new File(filePath);
            ColorPalette palette = loadPaletteFromJson(file);
            savePaletteAsPng(palette, replaceExtension(file, "png"));
            source.sendSuccess(() -> Component.literal("Converted palette '" + filePath + "' to png."), true);
        } catch (Exception e) {
            Exposure.LOGGER.error("Converting palette '{}' failed: ", filePath, e);
            source.sendFailure(Component.literal("Converting palette '" + filePath + "' failed: " + e.getMessage()));
        }

        return 0;
    }

    private static int convertPngToJson(CommandSourceStack source, String filePath) {

        try {
            File file = new File(filePath);
            BufferedImage image = ImageIO.read(file);

            int[] colors = new int[256];
            Arrays.fill(colors, Color.BLACK.getARGB());
            colors[255] = Color.TRANSPARENT.getARGB();

            int count = 0;

            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    if (count > 255) break;

                    int color = image.getRGB(x, y);

                    boolean alreadyAdded = false;
                    for (int addedColor : colors) {
                        if (addedColor == color) {
                            alreadyAdded = true;
                            break;
                        }
                    }

                    if (alreadyAdded) {
                        continue;
                    }

                    colors[count] = color;
                    count++;
                }
            }

            if (colors[255] != Color.TRANSPARENT.getARGB()) {
                colors[255] = Color.TRANSPARENT.getARGB();
                source.sendFailure(Component.literal("Corrected last color to be transparent in '" + filePath + "' palette."));
            }

            ColorPalette palette = new ColorPalette(colors);

            try {
                savePaletteAsJson(palette, replaceExtension(file, "json"));
            } catch (Exception e) {
                Exposure.LOGGER.error("Cannot save converted palette '{}' failed: ", filePath, e);
                source.sendFailure(Component.literal("Cannot save converted palette '" + filePath + "' failed: " + e.getMessage()));
                return 0;
            }

        } catch (Exception e) {
            Exposure.LOGGER.error("Converting palette '{}' failed: ", filePath, e);
            source.sendFailure(Component.literal("Converting palette '" + filePath + "' failed: " + e.getMessage()));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Converted palette '" + filePath + "' to json."), true);

        return 0;
    }

    private static void savePaletteAsJson(ColorPalette palette, File file) throws IOException {
        JsonElement jsonElement = ColorPalette.CODEC.encodeStart(JsonOps.INSTANCE, palette).getOrThrow();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(jsonElement);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json);
        }
    }

    private static void savePaletteAsPng(ColorPalette palette, File file) throws IOException {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int[] colors = palette.colors();

        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                image.setRGB(x, y, colors[y * 16 + x]);
            }
        }

        ImageIO.write(image, "png", file);
    }

    private static ColorPalette loadPaletteFromJson(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);
            return ColorPalette.CODEC.decode(JsonOps.INSTANCE, jsonObject).getOrThrow().getFirst();
        }
    }

    private static File replaceExtension(File originalFile, String newExtension) {
        String originalPath = originalFile.getPath();

        int lastDotIndex = originalPath.lastIndexOf('.');

        String newPath = (lastDotIndex == -1)
                ? originalPath + "." + newExtension
                : originalPath.substring(0, lastDotIndex) + "." + newExtension;

        return new File(newPath);
    }
}
