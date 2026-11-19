package io.github.mortuusars.exposure.world.camera;

import io.github.mortuusars.mortaar.network.packet.Packet;
import io.github.mortuusars.exposure.network.packet.clientbound.ClientboundActiveCameraOnStandSetPacket;
import io.github.mortuusars.exposure.world.entity.CameraOperator;
import io.github.mortuusars.exposure.world.entity.stand.CameraStand;
import net.minecraft.world.item.ItemStack;

public class CameraOnStand extends Camera {
    protected final CameraOperator cameraOperator;
    protected final CameraStand cameraStand;

    public CameraOnStand(CameraOperator cameraOperator, CameraStand cameraStand, CameraId id) {
        super(cameraStand, id);
        this.cameraOperator = cameraOperator;
        this.cameraStand = cameraStand;
    }

    public CameraOperator getOperator() {
        return cameraOperator;
    }

    public CameraStand getStand() {
        return cameraStand;
    }

    @Override
    public ItemStack getItemStack() {
        return cameraStand.getCamera();
    }

    @Override
    public void release() {
        getStand().activate();
    }

    @Override
    public Packet createSyncPacket() {
        return new ClientboundActiveCameraOnStandSetPacket(cameraOperator.asOperatorEntity().getId(), cameraStand.getId(), id);
    }
}
