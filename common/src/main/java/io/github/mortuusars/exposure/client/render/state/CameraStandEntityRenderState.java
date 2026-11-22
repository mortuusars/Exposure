package io.github.mortuusars.exposure.client.render.state;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class CameraStandEntityRenderState extends EntityRenderState {
    public int hurtDir;
    public float hurtTime;
    public float damageTime;
    public float entityPitch;
    public float entityYaw;
    public float vehicleRot;
    public boolean isMalfunctioned;
    public boolean inVehicle;
    public float cameraScale;
    public final ItemStackRenderState cameraModel = new ItemStackRenderState();
}
