package io.github.mortuusars.exposure.data.transfer;

import io.github.mortuusars.exposure.camera.infrastructure.FilmType;
import net.minecraft.nbt.CompoundTag;

public interface IExposureReceiver {
    void receivePart(String id, int width, int height, CompoundTag properties, int offset, byte[] partBytes);
}
