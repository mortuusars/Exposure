package io.github.mortuusars.exposure.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;

import java.util.function.Supplier;

@FunctionalInterface
public interface Key {
    boolean matches(KeyEvent event);

    default Key or(Key anotherKey) {
        return (event) -> this.matches(event) || anotherKey.matches(event);
    }

    default KeyWithPredicate onlyIf(Supplier<Boolean> predicate) {
        return new KeyWithPredicate(this, predicate);
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
        return (event) -> Key.actionMatches(InputConstants.PRESS, event.input())
                && keyCode == event.key() && event.modifiers() == modifiers;
    }

    static Key release(int modifiers, int keyCode) {
        return (event) -> Key.actionMatches(InputConstants.RELEASE, event.input())
                && keyCode == event.key() && event.modifiers() == modifiers;
    }

    static Key press(KeyMapping keyMapping) {
        return (event) -> Key.actionMatches(InputConstants.PRESS, event.input())
                && keyMapping.matches(event);
    }

    static Key release(KeyMapping keyMapping) {
        return (event) -> Key.actionMatches(InputConstants.RELEASE, event.input())
                && keyMapping.matches(event);
    }

    record KeyWithPredicate(Key key, Supplier<Boolean> predicate) implements Key {
        @Override
        public boolean matches(KeyEvent event) {
            return key.matches(event) && predicate.get();
        }
    }
}
