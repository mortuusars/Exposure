package io.github.mortuusars.exposure.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Axis;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.entity.PhotographEntity;
import io.github.mortuusars.exposure.render.modifiers.ExposurePixelModifiers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PhotographEntityRenderer<T extends PhotographEntity> extends EntityRenderer<T> {

    public PhotographEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T pEntity) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    @Override
    public boolean shouldRender(T livingEntity, Frustum camera, double camX, double camY, double camZ) {
        return super.shouldRender(livingEntity, camera, camX, camY, camZ);
    }

    @Override
    public void render(@NotNull T entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        boolean invisible = entity.isInvisible();

        poseStack.pushPose();

        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entity.getYRot()));
        poseStack.mulPose(Axis.ZP.rotationDegrees((entity.getRotation() * 360.0F / 4.0F)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));

        poseStack.translate(-0.5, -0.5, 1f / 32f - 0.005);
        float scale = 1f / ExposureRenderer.SIZE;
        poseStack.scale(scale, scale, -scale);

        int brightness = switch (entity.getDirection()) {
            case DOWN -> 210;
            case UP -> 255;
            default -> 235;
        };

        if (entity.isGlowing())
            packedLight = LightTexture.FULL_BRIGHT;

        @Nullable Either<String, ResourceLocation> idOrTexture = entity.getIdOrTexture();

        if (idOrTexture != null) {
            if (invisible) {
                ExposureClient.getExposureRenderer().render(idOrTexture, ExposurePixelModifiers.EMPTY, poseStack, bufferSource,
                        0, 0, ExposureRenderer.SIZE, ExposureRenderer.SIZE, 0, 0, 1, 1,
                        packedLight, brightness, brightness, brightness, 255);
            } else {
                ExposureClient.getExposureRenderer().renderOnPaper(idOrTexture, ExposurePixelModifiers.EMPTY, poseStack, bufferSource,
                        0, 0, ExposureRenderer.SIZE, ExposureRenderer.SIZE, 0, 0, 1, 1,
                        packedLight, brightness, brightness, brightness, 255, false);
            }
        }
        else if (!invisible){
            ExposureClient.getExposureRenderer().renderPaperTexture(poseStack, bufferSource,
                    0, 0, ExposureRenderer.SIZE, ExposureRenderer.SIZE, 0, 0, 1, 1,
                    packedLight, brightness, brightness, brightness, 255);
        }

        poseStack.popPose();
    }
}
