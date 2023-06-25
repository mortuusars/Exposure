package io.github.mortuusars.exposure.camera.viewfinder;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;

public class Viewfinder implements IViewfinder {
    private static final Viewfinder INSTANCE = new Viewfinder();

    private final ViewfinderClient CLIENT = new ViewfinderClient();
    private final ViewfinderServer SERVER = new ViewfinderServer();

    private Viewfinder() {}

    public static Viewfinder get() {
        return INSTANCE;
    }

    public void activate(Player player) {
        getSidedViewfinder().activate(player);
    }

    public void deactivate(Player player) {
        getSidedViewfinder().deactivate(player);
    }

    @Override
    public boolean isActive(Player player) {
        return getSidedViewfinder().isActive(player);
    }

    @Override
    public void update() {
        getSidedViewfinder().update();
    }

    public IViewfinder getSidedViewfinder() {
        return Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER ?
                SERVER : CLIENT;
    }
}
