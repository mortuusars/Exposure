package io.github.mortuusars.exposure.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.entity.PhotographEntity;
import io.github.mortuusars.exposure.item.PhotographItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
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
                Direction direction = entity.getDirection();
                Vec3 vec3 = this.getRenderOffset(entity, partialTick);
                poseStack.translate(-vec3.x(), -vec3.y(), -vec3.z());
                double offset = 0.46875D;
                poseStack.translate((double) direction.getStepX() * offset, (double) direction.getStepY() * offset, (double) direction.getStepZ() * offset);
                poseStack.mulPose(Vector3f.XP.rotationDegrees(entity.getXRot()));
                poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - entity.getYRot()));
                boolean invisible = entity.isInvisible();

                poseStack.translate(0.0D, 0.0D, 0.5D);

                poseStack.mulPose(Vector3f.ZP.rotationDegrees((entity.getRotation() * 360.0F / 4.0F)));

                poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
                float scale = 1f / PhotographRenderer.SIZE;
                poseStack.scale(scale, scale, scale);
                poseStack.translate(-128.0D, -128.0D, 0.0D);
                poseStack.translate(0.0D, 0.0D, -1.0D);




                poseStack.pushPose();

//                RenderSystem.setShaderTexture(0, PhotographRenderer.PHOTOGRAPH_PAPER_TEXTURE);
//                RenderSystem.disableBlend();
//                RenderSystem.disableDepthTest();


                TextureAtlasSprite texture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                        .apply(Exposure.resource("gui/misc/photograph_paper"));
                VertexConsumer consumer = buffer.getBuffer(Sheets.cutoutBlockSheet());

                float minU = texture.getU(0);
                float maxU = texture.getU(16);
                float minV = texture.getV(0);
                float maxV = texture.getV(16);

                drawTextureQuad(poseStack, 0, 0, 256, 256, 0, minU, minV, maxU, maxV, consumer, packedLight);



                float offset1 = 256 * 0.03f;
                poseStack.translate(offset1, offset1, -1);
                poseStack.scale(0.94f, 0.94f, 0.94f);
                PhotographRenderer.render(idOrTexture, buffer, poseStack, packedLight);
                poseStack.popPose();

//                idOrTexture
//                        .ifLeft(id -> Exposure.getStorage().getOrQuery(id).ifPresent(exposureData ->
//                                ExposureClient.getExposureRenderer().render(id, exposureData, false, poseStack, buffer, packedLight, 256, 256)))
//                        .ifRight(resource -> renderTexture(resource, poseStack, packedLight));

//                if (invisible)
//                    PhotographRenderer.render(idOrTexture, buffer, poseStack, packedLight);
//                else
//                    PhotographRenderer.renderOnPaper(idOrTexture, poseStack, buffer, packedLight);





                poseStack.popPose();
            });
        }
    }

    private static void drawTextureQuad(PoseStack poseStack, float minX, float minY, float maxX, float maxY, float blitOffset,
                                        float minU, float minV, float maxU, float maxV, VertexConsumer builder, int packedLight/*, Direction direction*/) {
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        builder.vertex(matrix, minX, maxY, blitOffset).color(255,255,255,255).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal, 0, 0, -1).endVertex();
        builder.vertex(matrix, maxX, maxY, blitOffset).color(255,255,255,255).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal, 0, 0, -1).endVertex();
        builder.vertex(matrix, maxX, minY, blitOffset).color(255,255,255,255).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal, 0, 0, -1).endVertex();
        builder.vertex(matrix, minX, minY, blitOffset).color(255,255,255,255).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal, 0, 0, -1).endVertex();
    }
}
