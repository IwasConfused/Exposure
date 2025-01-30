package io.github.mortuusars.exposure.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

import java.util.function.Supplier;

@FunctionalInterface
public interface Key {
    boolean matches(int keyCode, int scanCode, int action, int modifiers);

    default Key or(Key matcher) {
        return (key, code, action, mods) -> this.matches(key, code, action, mods) || matcher.matches(key, code, action, mods);
    }

    default KeyBinding executes(Supplier<Boolean> handler) {
        return new KeyBinding(this, handler);
    }

    default KeyBinding executes(Runnable runnable) {
        return new KeyBinding(this, () -> {
            runnable.run();
            return true;
        });
    }

    static boolean actionMatches(int definedAction, int action) {
        if (definedAction == 1 || definedAction == 2) {
            return action == 1 || action == 2;
        }
        return definedAction == action;
    }

    // --

    static Key press(int keyCode) {
        return press(Modifier.NONE, keyCode);
    }

    static Key release(int keyCode) {
        return release(Modifier.NONE, keyCode);
    }

    static Key press(int modifiers, int keyCode) {
        return (key, code, action, mods) -> Key.actionMatches(InputConstants.PRESS, action)
                && keyCode == key && mods == modifiers;
    }

    static Key release(int modifiers, int keyCode) {
        return (key, code, action, mods) -> Key.actionMatches(InputConstants.RELEASE, action)
                && keyCode == key && mods == modifiers;
    }

    static Key press(KeyMapping keyMapping) {
        return (key, code, action, mods) -> Key.actionMatches(InputConstants.PRESS, action)
                && keyMapping.matches(key, code) && mods == 0;
    }

    static Key release(KeyMapping keyMapping) {
        return (key, code, action, mods) -> Key.actionMatches(InputConstants.RELEASE, action)
                && keyMapping.matches(key, code) && mods == 0;
    }
}
