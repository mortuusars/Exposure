package io.github.mortuusars.exposure.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Vector3f;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.entity.PhotographEntity;
import io.github.mortuusars.exposure.item.PhotographItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.model.data.ModelData;
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
    public void render(@NotNull T entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);

        if (entity.getItem().getItem() instanceof PhotographItem photographItem) {
            Optional<Either<String, ResourceLocation>> idOrResource = photographItem.getIdOrResource(entity.getItem());
            idOrResource.ifPresent(idOrTexture -> {
                poseStack.pushPose();

                boolean invisible = entity.isInvisible();

                poseStack.mulPose(Vector3f.XP.rotationDegrees(entity.getXRot()));
                poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - entity.getYRot()));
                poseStack.mulPose(Vector3f.ZP.rotationDegrees((entity.getRotation() * 360.0F / 4.0F)));

                if (!invisible) {
                    poseStack.pushPose();

                    poseStack.translate(-0.5f, -0.5f, -0.972f);

                    Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(poseStack.last(), buffer.getBuffer(Sheets.cutoutBlockSheet()),
                            null, Minecraft.getInstance().getModelManager().getModel(Exposure.resource("block/photograph")),
                            1f, 1f, 1f, packedLight, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
                    poseStack.popPose();
                }

                poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
                float scale = 1f / PhotographRenderer.SIZE;
                poseStack.scale(scale, scale, scale);
                poseStack.translate(-128.0D, -128.0D, invisible ? 7.5 : 5.5);
                poseStack.pushPose();

                float phScale = invisible ? 1f : 0.9f;
                float offset = PhotographRenderer.SIZE * (1f - phScale) / 2f;
                poseStack.translate(offset, offset, 0);
                poseStack.scale(phScale, phScale, phScale);
                PhotographRenderer.render(idOrTexture, buffer, poseStack, packedLight);
                poseStack.popPose();

                poseStack.popPose();
            });
        }
    }
}
