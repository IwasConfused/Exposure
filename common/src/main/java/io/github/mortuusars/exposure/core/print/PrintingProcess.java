package io.github.mortuusars.exposure.core.print;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.core.ExposureType;
import net.minecraft.world.item.DyeColor;

import java.util.List;
import java.util.function.Supplier;

public enum PrintingProcess {
    BLACK_AND_WHITE(List.of(DyeColor.BLACK),
            Config.Common.LIGHTROOM_BW_PRINT_TIME,
            Config.Common.LIGHTROOM_EXPERIENCE_PER_PRINT_BW),
    COLOR(List.of(DyeColor.CYAN, DyeColor.MAGENTA, DyeColor.YELLOW, DyeColor.BLACK),
            Config.Common.LIGHTROOM_COLOR_PRINT_TIME,
            Config.Common.LIGHTROOM_EXPERIENCE_PER_PRINT_COLOR),
    CHROMATIC_R(List.of(DyeColor.MAGENTA, DyeColor.YELLOW),
            Config.Common.LIGHTROOM_CHROMATIC_PRINT_TIME,
            () -> 0),
    CHROMATIC_G(List.of(DyeColor.CYAN, DyeColor.YELLOW),
            Config.Common.LIGHTROOM_CHROMATIC_PRINT_TIME,
            () -> 0),
    CHROMATIC_B(List.of(DyeColor.CYAN, DyeColor.MAGENTA),
            Config.Common.LIGHTROOM_CHROMATIC_PRINT_TIME,
            Config.Common.LIGHTROOM_EXPERIENCE_PER_PRINT_CHROMATIC);

    private final List<DyeColor> requiredDyes;
    private final Supplier<Integer> printTime;
    private final Supplier<Integer> xpPerPrint;

    PrintingProcess(List<DyeColor> requiredDyes, Supplier<Integer> printTime, Supplier<Integer> xpPerPrint) {
        this.requiredDyes = requiredDyes;
        this.printTime = printTime;
        this.xpPerPrint = xpPerPrint;
    }

    public List<DyeColor> getRequiredDyes() {
        return requiredDyes;
    }

    public int getPrintTime() {
        return printTime.get();
    }

    public int getExperiencePerPrint() {
        return xpPerPrint.get();
    }

    public boolean isRegular() {
        return this == BLACK_AND_WHITE || this == COLOR;
    }

    public boolean isChromatic() {
        return this == CHROMATIC_R || this == CHROMATIC_G || this == CHROMATIC_B;
    }

    public static PrintingProcess fromExposureType(ExposureType type) {
        return type == ExposureType.COLOR ? COLOR : BLACK_AND_WHITE;
    }

    public static PrintingProcess fromChromaticStep(int step) {
        return switch (step) {
            case 0 -> CHROMATIC_R;
            case 1 -> CHROMATIC_G;
            case 2 -> CHROMATIC_B;
            default -> throw new IllegalStateException("Unexpected step value: " + step + ", 0|1|2 corresponding to R|G|B is expected.");
        };
    }
}
