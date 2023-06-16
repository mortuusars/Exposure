package io.github.mortuusars.exposure.network;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.storage.ExposureSavedData;

import java.util.HashMap;
import java.util.Map;

public class ExposureReceiver {
    private static final Map<String, byte[]> PARTS = new HashMap<>();

    public static void receivePart(String id, int width, int height, int offset, byte[] partBytes) {
        byte[] exposureBytes = PARTS.compute(id, (key, data) ->
                data == null ? new byte[width * height] : data);

        System.arraycopy(partBytes, 0, exposureBytes, offset, partBytes.length);
        PARTS.put(id, exposureBytes);

        if (offset + partBytes.length >= exposureBytes.length) {
            PARTS.remove(id);
            Exposure.getStorage().put(id, new ExposureSavedData(width, height, exposureBytes));
        }
    }
}
