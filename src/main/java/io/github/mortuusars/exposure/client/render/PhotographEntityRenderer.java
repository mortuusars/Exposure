package io.github.mortuusars.exposure.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Vector3f;
import io.github.mortuusars.exposure.entity.PhotographEntity;
import io.github.mortuusars.exposure.item.PhotographItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PhotographEntityRenderer<T extends PhotographEntity> extends EntityRenderer<T> {

    public PhotographEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T pEntity) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    @Override
    public void render(@NotNull T entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        if (entity.getItem().getItem() instanceof PhotographItem photographItem) {
            Optional<Either<String, ResourceLocation>> idOrResource = photographItem.getIdOrResource(entity.getItem());
            idOrResource.ifPresent(idOrTexture -> {
                boolean invisible = entity.isInvisible();

                poseStack.pushPose();

                poseStack.mulPose(Vector3f.XP.rotationDegrees(entity.getXRot()));
                poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - entity.getYRot()));
                poseStack.mulPose(Vector3f.ZP.rotationDegrees((entity.getRotation() * 360.0F / 4.0F)));
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));

                poseStack.translate(-0.5, -0.5, 1f / 32f - 0.001);
                float scale = 1f / PhotographRenderer.SIZE;
                poseStack.scale(scale, scale, -scale);

                int brightness = switch (entity.getDirection()) {
                    case DOWN -> 215;
                    case UP -> 255;
                    default -> 238;
                };

                if (invisible) {
                    PhotographRenderer.render(idOrTexture, poseStack, bufferSource, 0, 0, PhotographRenderer.SIZE, PhotographRenderer.SIZE,
                            0, 0, 1, 1, packedLight, brightness, brightness, brightness, 255);
                } else {
                    PhotographRenderer.renderOnPaper(idOrTexture, poseStack, bufferSource, packedLight, brightness, brightness, brightness, 255);
                }

                poseStack.popPose();
            });
        }
    }
}
