package io.github.mortuusars.exposure.camera.infrastructure;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Objects;

public class ShutterSpeed {
    public static final ShutterSpeed DEFAULT = new ShutterSpeed("60");

    private final String text;
    private final float valueMilliseconds;

    public ShutterSpeed(String shutterSpeed) {
        this.text = shutterSpeed;
        this.valueMilliseconds = parseMilliseconds(shutterSpeed);
        Preconditions.checkState(valueMilliseconds != -1,  shutterSpeed + " is not valid. (format should be: '1/60', '60', '2\"')");
        Preconditions.checkState(valueMilliseconds > 0, "Shutter Speed cannot be 0 or smaller.");
    }

    private float parseMilliseconds(String shutterSpeed) {
        shutterSpeed = shutterSpeed.trim();

        if (shutterSpeed.contains("\""))
            return Integer.parseInt(shutterSpeed.replace("\"", "")) * 1000;
        else if (shutterSpeed.contains("1/"))
            return 1f / Integer.parseInt(shutterSpeed.replace("1/", "")) * 1000;
        else
            return 1f / Integer.parseInt(shutterSpeed) * 1000;
    }

    public String getFormattedText() {
        if (getMilliseconds() < 999 && !text.startsWith("1/"))
            return "1/" + text;

        return text;
    }

    public float getMilliseconds() {
        return valueMilliseconds;
    }

    public int getTicks() {
        return Math.max(1, (int)(valueMilliseconds * 20 / 1000f));
    }

    public float getStopsDifference(ShutterSpeed relative) {
        return (float) (Math.log(valueMilliseconds / relative.getMilliseconds()) / Math.log(2));
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putString("ShutterSpeed", text);
        return tag;
    }

    public static ShutterSpeed loadOrDefault(CompoundTag tag) {
        try {
            if (tag.contains("ShutterSpeed", Tag.TAG_STRING)) {
                String shutterSpeed = tag.getString("ShutterSpeed");
                return new ShutterSpeed(shutterSpeed);
            }
        }
        catch (IllegalStateException e) {
            Exposure.LOGGER.error("Cannot load a shutter speed from tag: " + e);
        }

        return DEFAULT;
    }
    
    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(text);
    }

    public static ShutterSpeed fromBuffer(FriendlyByteBuf buffer) {
        return new ShutterSpeed(buffer.readUtf());
    }

    @Override
    public String toString() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShutterSpeed that = (ShutterSpeed) o;
        return Float.compare(that.valueMilliseconds, valueMilliseconds) == 0 && Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, valueMilliseconds);
    }
}
