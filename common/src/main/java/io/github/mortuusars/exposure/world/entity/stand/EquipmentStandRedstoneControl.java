package io.github.mortuusars.exposure.world.entity.stand;

import net.minecraft.nbt.CompoundTag;

public class EquipmentStandRedstoneControl {
    public int activationDelay = 2;

    protected EquipmentStand stand;

    protected boolean hasSignal;
    protected int delay;

    public EquipmentStandRedstoneControl(EquipmentStand stand) {
        this.stand = stand;
    }

    /**
     * @return 'true' if activated.
     */
    public boolean tick() {
        boolean hasSignal = stand.level().hasNeighborSignal(stand.blockPosition());

        if (hasSignal && !this.hasSignal && delay <= 0) {
            // Start delay countdown when receiving a new pulse.
            // Delay helps to resolve some visual issues. Redstone will lit up on the client in that time, for example.
            delay = activationDelay;
        }

        boolean activated = false;

        if (delay > 0) {
            delay--;
            if (delay == 0) {
                stand.activateByRedstone();
                activated = true;
            }
        }

        this.hasSignal = hasSignal;
        return activated;
    }

    public void load(CompoundTag tag) {
        hasSignal = tag.getBoolean("HasRedstoneSignal");
        delay = tag.getInt("RedstoneReleaseDelay");
    }

    public void save(CompoundTag tag) {
        tag.putBoolean("HasRedstoneSignal", hasSignal);
        tag.putInt("RedstoneReleaseDelay", delay);
    }
}
