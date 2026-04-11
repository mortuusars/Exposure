package io.github.mortuusars.exposure.world.entity;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class CameraStandRedstoneControl {
    public int delay = 2;

    protected CameraStandEntity stand;

    protected boolean hasSignal;
    protected int releaseDelay;

    public CameraStandRedstoneControl(CameraStandEntity stand) {
        this.stand = stand;
    }

    /**
     * @return 'true' if shutter was released.
     */
    public boolean tick() {
        boolean hasSignal = stand.level().hasNeighborSignal(stand.blockPosition());

        if (hasSignal && !this.hasSignal && releaseDelay <= 0) {
            releaseDelay = delay; // Start delay countdown when receiving a new pulse
            // Delay helps to resolve some visual issues. Redstone will lit up on the client in that time, for example.
        }

        boolean released = false;

        if (releaseDelay > 0) {
            releaseDelay--;
            if (releaseDelay == 0) {
                stand.release(); // Due to release delay, shortest time between releases seems to be 3 ticks instead of default 2-tick cooldown.
                released = true;
            }
        }

        this.hasSignal = hasSignal;
        return released;
    }

    public void load(ValueInput input) {
        hasSignal = input.getBooleanOr("HasRedstoneSignal", false);
        releaseDelay = input.getIntOr("RedstoneReleaseDelay", 0);
    }

    public void save(ValueOutput output) {
        output.putBoolean("HasRedstoneSignal", hasSignal);
        output.putInt("RedstoneReleaseDelay", releaseDelay);
    }
}
