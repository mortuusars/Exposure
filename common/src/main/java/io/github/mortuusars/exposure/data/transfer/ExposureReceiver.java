package io.github.mortuusars.exposure.data.transfer;

import io.github.mortuusars.exposure.data.storage.ExposureSavedData;
import io.github.mortuusars.exposure.data.storage.IExposureStorage;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;

public class ExposureReceiver implements IExposureReceiver {
    private final Map<String, byte[]> PARTS = new HashMap<>();
    private final IExposureStorage storage;

    public ExposureReceiver(IExposureStorage storage) {
        this.storage = storage;
    }

    public void receivePart(String id, int width, int height, CompoundTag properties, int offset, byte[] partBytes) {
        byte[] exposureBytes = PARTS.compute(id, (key, data) ->
                data == null ? new byte[width * height] : data);

        System.arraycopy(partBytes, 0, exposureBytes, offset, partBytes.length);
        PARTS.put(id, exposureBytes);

        if (offset + partBytes.length >= exposureBytes.length) {
            PARTS.remove(id);
            storage.put(id, new ExposureSavedData(width, height, exposureBytes, properties));
        }
    }
}
