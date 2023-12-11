package io.github.mortuusars.exposure.data.transfer;

import io.github.mortuusars.exposure.data.storage.ExposureSavedData;
import net.minecraft.world.entity.player.Player;

public interface IExposureSender {
    void send(String id, ExposureSavedData exposureData);
    void sendTo(Player player, String id, ExposureSavedData exposureData);
}
