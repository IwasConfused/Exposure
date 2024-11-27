package io.github.mortuusars.exposure.util;

import net.minecraft.network.chat.Component;

public record ErrorMessage(String technicalTranslationKey, String casualTranslationKey) {
    public static final ErrorMessage EMPTY = new ErrorMessage("", "");

    public static ErrorMessage create(String translationKey) {
        return new ErrorMessage(translationKey + ".technical", translationKey + ".casual");
    }

    public boolean isEmpty() {
        return this.equals(EMPTY);
    }

    public Component getTechnicalTranslation() {
        return isEmpty() ? Component.empty() : Component.translatable(technicalTranslationKey);
    }

    public Component getCasualTranslation() {
        return isEmpty() ? Component.empty() : Component.translatable(casualTranslationKey);
    }
}
