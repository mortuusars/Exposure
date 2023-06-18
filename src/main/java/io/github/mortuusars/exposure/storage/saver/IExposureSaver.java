package io.github.mortuusars.exposure.storage.saver;

public interface IExposureSaver {
    void save(String id, byte[] materialColorPixels, int width, int height);
}
