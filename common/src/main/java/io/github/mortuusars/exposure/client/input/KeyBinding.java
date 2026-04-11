package io.github.mortuusars.exposure.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.input.KeyEvent;

import java.util.function.*;

public record KeyBinding(Key matcher, Supplier<Boolean> handler) {
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return matchesPress(keyCode, scanCode, modifiers) && handler().get();
    }

    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return matchesRelease(keyCode, scanCode, modifiers) && handler().get();
    }

    public boolean matches(int keyCode, int scanCode, int press, int modifiers) {
        return matcher.matches(new KeyEvent(press, keyCode, modifiers));
    }

    public boolean matchesPress(int keyCode, int scanCode, int modifiers) {
        return matcher.matches(new KeyEvent(InputConstants.PRESS, keyCode, modifiers));
    }

    public boolean matchesRelease(int keyCode, int scanCode, int modifiers) {
        return matcher.matches(new KeyEvent(InputConstants.RELEASE, keyCode, modifiers));
    }
}
