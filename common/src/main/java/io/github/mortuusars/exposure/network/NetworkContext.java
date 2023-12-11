package io.github.mortuusars.exposure.network;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class NetworkContext {
    @Nullable
    private final Player player;
    private final PacketDirection receivingSide;

    public NetworkContext(@Nullable Player player, PacketDirection receivingSide) {
        this.player = player;
        this.receivingSide = receivingSide;
    }

    public @Nullable Player getPlayer() {
        return player;
    }
}
