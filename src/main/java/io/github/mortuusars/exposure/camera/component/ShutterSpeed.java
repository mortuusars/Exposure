package io.github.mortuusars.exposure.camera.component;

import com.google.common.base.Preconditions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Objects;

public class ShutterSpeed {
    private final float value;
    private final int visibleDuration;

    public ShutterSpeed(float seconds, int visibleDuration) {
        this.value = seconds;
        this.visibleDuration = visibleDuration;
        Preconditions.checkState(seconds != 0f, "Shutter Speed cannot be 0.");
    }

    public float getValue() {
        return value;
    }

    public int getVisibleDuration() {
        return visibleDuration;
    }

    public float getStopsDifference(ShutterSpeed relative) {
        return (float) (Math.log(value / relative.getValue()) / Math.log(2));
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putFloat("Value", value);
        tag.putInt("VisibleDuration", visibleDuration);
        return tag;
    }

    public static ShutterSpeed loadOrDefault(CompoundTag tag, ShutterSpeed defaultShutterSpeed) {
        return new ShutterSpeed(
                tag.contains("Value", Tag.TAG_ANY_NUMERIC) ? tag.getFloat("Value") : defaultShutterSpeed.getValue(),
                tag.contains("VisibleDuration", Tag.TAG_ANY_NUMERIC) ? tag.getInt("VisibleDuration") : defaultShutterSpeed.getVisibleDuration());
    }
    
    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeFloat(value);
        buffer.writeInt(visibleDuration);
    }

    public static ShutterSpeed fromBuffer(FriendlyByteBuf buffer) {
        return new ShutterSpeed(buffer.readFloat(), buffer.readInt());
    }

    @Override
    public String toString() {
        return value >= 1d ? Math.round(value) + "\"" : Integer.toString(Math.round(1f / value));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShutterSpeed that = (ShutterSpeed) o;
        return Float.compare(that.value, value) == 0 && visibleDuration == that.visibleDuration;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, visibleDuration);
    }
}
