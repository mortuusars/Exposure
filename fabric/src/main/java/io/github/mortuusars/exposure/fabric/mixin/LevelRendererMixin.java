package io.github.mortuusars.exposure.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.*;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Shadow private int renderedEntities;
    @Final @Shadow private RenderBuffers renderBuffers;
    @Shadow protected abstract boolean shouldShowEntityOutlines();
    @Shadow private void renderEntity(Entity entity, double camX, double camY, double camZ, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource) {}

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;entitiesForRendering()Ljava/lang/Iterable;"))
    private void onRenderLevel(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        if (camera.getEntity() instanceof CameraStandEntity) {
            exposure$renderEntity(deltaTracker, camera, Minecrft.playerOrThrow());
        }
    }

    @Unique
    private void exposure$renderEntity(DeltaTracker deltaTracker, Camera camera, Entity entity) {
        this.renderedEntities++;
        if (entity.tickCount == 0) {
            entity.xOld = entity.getX();
            entity.yOld = entity.getY();
            entity.zOld = entity.getZ();
        }

        MultiBufferSource multiBufferSource;
        if (this.shouldShowEntityOutlines() && Minecrft.get().shouldEntityAppearGlowing(entity)) {
            OutlineBufferSource outlineBufferSource = this.renderBuffers.outlineBufferSource();
            multiBufferSource = outlineBufferSource;
            int i = entity.getTeamColor();
            outlineBufferSource.setColor(FastColor.ARGB32.red(i), FastColor.ARGB32.green(i), FastColor.ARGB32.blue(i), 255);
        } else {
            multiBufferSource = this.renderBuffers.bufferSource();
        }

        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(!Minecrft.levelOrThrow().tickRateManager().isEntityFrozen(entity));
        Vec3 position = camera.getPosition();
        PoseStack poseStack = new PoseStack();
        this.renderEntity(entity, position.x, position.y, position.z, partialTick, poseStack, multiBufferSource);
    }
}
