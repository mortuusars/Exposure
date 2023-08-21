package io.github.mortuusars.exposure.camera.component;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum FlashMode implements StringRepresentable {
    OFF("off"),
    ON("on"),
    AUTO("auto");

    private final String id;

    FlashMode(String id) {
        this.id = id;
    }

    public static FlashMode byIdOrOff(String id) {
        for (FlashMode guide : values()) {
            if (guide.id.equals(id))
                return guide;
        }

        return OFF;
    }

    public String getId() {
        return id;
    }

    @Override
    public @NotNull String getSerializedName() {
        return id;
    }

    public Component translate() {
        return Component.translatable("gui." + Exposure.ID + ".flash_mode." + id);
    }
    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(getId());
    }

    public static FlashMode fromBuffer(FriendlyByteBuf buffer) {
        return FlashMode.byIdOrOff(buffer.readUtf());
    }
}
