package io.github.mortuusars.exposure.integration;

import io.github.mortuusars.mortaar.Platform;

public class Mods {
    public static final Mod CREATE = new Mod("create");
    public static final Mod REAL_CAMERA = new Mod("realcamera");
    public static final Mod SHOULDER_SURFING = new Mod("shouldersurfing");

    public record Mod(String id) {
        public boolean isLoaded() {
            return Platform.isModLoaded(id);
        }

        public boolean isLoading() {
            return Platform.isModLoading(id);
        }
    }
}