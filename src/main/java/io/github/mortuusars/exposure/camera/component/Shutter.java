package io.github.mortuusars.exposure.camera.component;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class Shutter {
    private final Map<Player, OpenShutter> openShutters = new HashMap<>();
    @Nullable
    private BiConsumer<Player, OpenShutter> onShutterClosed;

    public void setOnShutterClosed(@Nullable BiConsumer<Player, OpenShutter> onShutterClosed) {
        this.onShutterClosed = onShutterClosed;
    }

    public boolean isOpen(Player player) {
        return openShutters.containsKey(player);
    }

    public void open(Player player, ShutterSpeed shutterSpeed) {
        openShutters.put(player, new OpenShutter(shutterSpeed, System.currentTimeMillis()));
        player.getLevel().playSound(player, player, SoundEvents.UI_BUTTON_CLICK, SoundSource.PLAYERS, 0.8f, 0.65f);
    }

    public void close(Player player) {
        @Nullable OpenShutter shutter = openShutters.remove(player);
        if (shutter != null) {
            player.getLevel().playSound(player, player, SoundEvents.UI_BUTTON_CLICK, SoundSource.PLAYERS, 0.8f, 0.9f);
            if (onShutterClosed != null)
                onShutterClosed.accept(player, shutter);
        }
    }

    public void tick(Player player) {
        if (!isOpen(player))
            return;

        List<Player> toClose = new ArrayList<>();

        for (Map.Entry<Player, OpenShutter> shutter : openShutters.entrySet()) {
            if (System.currentTimeMillis() - shutter.getValue().openedAt > shutter.getValue().shutterSpeed.getValue() * 1000)
                toClose.add(shutter.getKey());
        }

        for (Player pl : toClose) {
            close(pl);
        }
    }

    public record OpenShutter(ShutterSpeed shutterSpeed, long openedAt) {}
}
