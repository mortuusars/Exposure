package io.github.mortuusars.exposure.event;

import io.github.mortuusars.exposure.Exposure;
import net.minecraftforge.event.TickEvent;

public class CommonEvents {
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        Exposure.getCamera().tick(event.player);
    }
}
