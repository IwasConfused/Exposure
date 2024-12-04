package io.github.mortuusars.exposure.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.io.Serial;

public class TranslatableError extends Error {
    public static final String GENERIC = "gui.exposure.error_message.generic";

    @Serial
    private static final long serialVersionUID = 0L;

    public TranslatableError(String baseTranslationKey) {
        super(baseTranslationKey);
    }

    public TranslatableError(String baseTranslationKey, Throwable cause) {
        super(baseTranslationKey, cause);
    }

    public static ErrorMessage create(String translationKey) {
        return new ErrorMessage(translationKey + ".technical", translationKey + ".casual");
    }

    public MutableComponent technical() {
        return Component.translatable(getMessage() + ".technical");
    }

    public MutableComponent casual() {
        return Component.translatable(getMessage() + ".casual");
    }

    @Override
    public String getLocalizedMessage() {
        return technical().getString();
    }
}
