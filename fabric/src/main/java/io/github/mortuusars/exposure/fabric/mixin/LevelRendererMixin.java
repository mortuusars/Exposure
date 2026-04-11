package io.github.mortuusars.exposure.fabric.mixin;

import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Shadow protected abstract boolean shouldShowEntityOutlines();
    @Shadow public abstract boolean isSectionCompiled(BlockPos blockPos);
    @Final @Shadow private EntityRenderDispatcher entityRenderDispatcher;
    @Shadow private ClientLevel level;

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "collectVisibleEntities", at = @At(value = "TAIL"), cancellable = true)
    private void onRenderLevel(Camera camera, Frustum frustum, List<Entity> output, CallbackInfoReturnable<Boolean> cir) {
        Entity entity = minecraft.player;
        Vec3 vec3 = entity.position();
        if (minecraft.getCameraEntity() instanceof CameraStandEntity) {
            if (this.entityRenderDispatcher.shouldRender(entity, frustum, vec3.x, vec3.y, vec3.z)) {
                BlockPos blockPos = entity.blockPosition();
                if ((this.level.isOutsideBuildHeight(blockPos.getY()) || this.isSectionCompiled(blockPos))) {
                    output.add(entity);
                    if (shouldShowEntityOutlines() && minecraft.shouldEntityAppearGlowing(entity)) {
                        cir.setReturnValue(true);
                    }
                }
            }
        }
    }
}
