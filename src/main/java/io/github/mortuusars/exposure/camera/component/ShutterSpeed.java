package io.github.mortuusars.exposure.camera.component;

import com.google.common.base.Preconditions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;

import java.util.Objects;

public class ShutterSpeed {
    private final float value;
    private final int cooldown;

    public ShutterSpeed(float value, int cooldown) {
        this.value = value;
        this.cooldown = cooldown;
        Preconditions.checkState(value != 0f, "Shutter Speed cannot be 0.");
    }

    public float getValue() {
        return value;
    }

    public int getCooldown() {
        return cooldown;
    }

    public float getStopsDifference(ShutterSpeed relative) {
        return (float) (Math.log(value / relative.getValue()) / Math.log(2));
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putFloat("Value", value);
        tag.putInt("Cooldown", cooldown);
        return tag;
    }

    public static ShutterSpeed loadOrDefault(CompoundTag tag, ShutterSpeed defaultShutterSpeed) {
        return new ShutterSpeed(
                tag.contains("Value", Tag.TAG_ANY_NUMERIC) ? tag.getFloat("Value") : defaultShutterSpeed.getValue(),
                tag.contains("Cooldown", Tag.TAG_ANY_NUMERIC) ? tag.getInt("Cooldown") : defaultShutterSpeed.getCooldown());
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
        return Float.compare(that.value, value) == 0 && cooldown == that.cooldown;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, cooldown);
    }
}
