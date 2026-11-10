package io.github.mortuusars.exposure.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.client.render.image.RenderCoordinates;
import io.github.mortuusars.exposure.client.render.photograph.PhotographStyle;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.entity.PhotographFrameEntity;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.mortaar.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class PhotographFrameEntityRenderer<T extends PhotographFrameEntity> extends EntityRenderer<T> {
    protected final BlockRenderDispatcher blockRenderer;

    public PhotographFrameEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T pEntity) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    public ModelResourceLocation getModelLocation(T entity, int size) {
        return switch (size) {
            case 0 -> ExposureClient.Models.PHOTOGRAPH_FRAME_SMALL;
            case 1 -> ExposureClient.Models.PHOTOGRAPH_FRAME_MEDIUM;
            case 2 -> ExposureClient.Models.PHOTOGRAPH_FRAME_LARGE;
            default -> throw new IllegalArgumentException("size " + size + " is not valid. Expected 0-2.");
        };
    }

    protected @NotNull RenderType getRenderType() {
        return Sheets.solidBlockSheet();
    }

    @Override
    public void render(@NotNull T entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        if (Minecraft.getInstance().hitResult instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() == entity) {
            Minecraft.getInstance().crosshairPickEntity = entity;
        }

        Direction direction = entity.getDirection();
        int size = entity.getSize();

        poseStack.pushPose();
        // Offsets name tag rendering to be like item frame:
        poseStack.translate(direction.getStepX() * 0.3f, direction.getStepY() * 0.3f, direction.getStepZ() * 0.3f);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();

        poseStack.pushPose();

        // thickness of the frame is 1px (0.5 - (1/16 * 0.5)) (0.5 is because we are offsetting from the center)
        // stripped frame is thin, so 1/16 becomes 0.15/16 (thickness of the backplate)
        double hangOffset = 0.46875;
        poseStack.translate(direction.getStepX() * hangOffset, direction.getStepY() * hangOffset, direction.getStepZ() * hangOffset);

        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entity.getYRot()));

        ItemStack item = entity.getItem();
        if (!item.isEmpty()) {
            boolean photographRendered = renderPhotograph(entity, poseStack, bufferSource, packedLight, item, size);

            if (!photographRendered) {
                poseStack.pushPose();
                float scale = 0.65f + entity.getSize() * 0.5f;
                poseStack.translate(0, 0, 0.46875);
                poseStack.scale(scale, scale, scale * 0.75f);
                poseStack.mulPose(Axis.ZP.rotationDegrees((entity.getItemRotation() * 360.0F / 4.0F)));
                Minecrft.get().getItemRenderer().renderStatic(item, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, entity.level(), 0);
                poseStack.popPose();
            }
        }

        if (!entity.isFrameInvisible()) {
            renderFrame(entity, poseStack, bufferSource, packedLight, size);
        }

        poseStack.popPose();
    }

    protected void renderFrame(@NotNull T entity, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource,
                             int packedLight, int size) {
        poseStack.pushPose();
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        ModelResourceLocation modelLocation = getModelLocation(entity, size);
        BakedModel model = Platform.Client.getModel(modelLocation);
        blockRenderer.getModelRenderer().renderModel(poseStack.last(), bufferSource.getBuffer(getRenderType()),
                null, model, 1.0f, 1.0f, 1.0f, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    protected boolean renderPhotograph(@NotNull T entity, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource,
                                  int packedLight, ItemStack item, int size) {
        poseStack.pushPose();

        boolean frameInvisible = entity.isFrameInvisible();

        float frameBorderOffset = frameInvisible ? 0f : 0.125f; // (2px / 16px = 0.125)
        float offsetFromCenter = frameInvisible ? 0.497f : 0.48f;
        offsetFromCenter -= Config.Client.PHOTOGRAPH_FRAME_IMAGE_OFFSET.get();
        float desiredSize = size + 1 - frameBorderOffset * 2;

        poseStack.mulPose(Axis.ZP.rotationDegrees((entity.getItemRotation() * 360.0F / 4.0F)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.translate(-0.5 * (size + 1) + frameBorderOffset, -0.5 * (size + 1) + frameBorderOffset, offsetFromCenter);
        poseStack.scale(desiredSize, desiredSize, 1);

        boolean isGlowing = entity.isGlowing();
        if (isGlowing) {
            packedLight = LightTexture.FULL_BRIGHT;
        }

        int brightness = isGlowing ? 255 : getPhotographBrightness(entity);

        boolean photographRendered = false;

        if (Config.Client.PIXEL_PERFECT_PHOTOGRAPH_FRAME.get()) {
            if (item.getItem() instanceof PhotographItem photographItem) {
                PhotographStyle style = PhotographStyle.of(item);
                Frame frame = photographItem.getFrame(item);

                RenderableImage image = style.process(ExposureClient.renderedExposures().getOrCreate(frame));

                int pixels = 16 * (entity.getSize() + 1);
                if (!frameInvisible) {
                    pixels -= 4;
                }
                image = image.modifyWith(ImageEffect.Resize.to(pixels)::modify, "pixels-" + pixels);

                ExposureClient.imageRenderer().render(image, poseStack, bufferSource, RenderCoordinates.DEFAULT,
                        packedLight, brightness, brightness, brightness, 255);
                photographRendered = !image.isEmpty();
            }
        } else {
            photographRendered = ExposureClient.photographRenderer().render(item, false, false,
                    poseStack, bufferSource, packedLight, brightness, brightness, brightness, 255);
        }

        poseStack.popPose();

        return photographRendered;
    }

    public int getPhotographBrightness(T entity) {
        if (entity.getDirection() == Direction.UP)
            return 255;

        // Darken the photo same way as the block sides darken,
        // but not quite as much and allow light sources to brighten it:
        int lightLevel = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition());
        float shadeFactor = entity.level().getShade(entity.getDirection(), true);
        shadeFactor += (1f - shadeFactor) * 0.2f;

        int shadedBrightness = (int)(255 * shadeFactor);
        int missingLight = 255 - shadedBrightness;
        int lightUp = (int)(missingLight * (lightLevel / 15f * 0.5f));
        return Math.min(255, shadedBrightness + lightUp);
    }

    @Override
    protected void renderNameTag(T entity, Component displayName, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float partialTick) {
        double d = this.entityRenderDispatcher.distanceToSqr(entity);
        if (!(d > 4096.0)) {
            Vec3 vec3 = entity.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, entity.getViewYRot(partialTick));
            if (vec3 != null) {
                boolean bl = !entity.isDiscrete();
                poseStack.pushPose();

                double yOffset = entity.getDirection().getAxis().isHorizontal()
                        ? vec3.y - 0.2 + entity.getSize() * 0.5
                        : entity.getDirection().getStepY() > 0
                            ? vec3.y - 0.5
                            : vec3.y - 1;

                poseStack.translate(vec3.x, vec3.y + yOffset, vec3.z);
                poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
                poseStack.scale(0.025F, -0.025F, 0.025F);
                Matrix4f matrix4f = poseStack.last().pose();
                float f = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
                int j = (int)(f * 255.0F) << 24;
                Font font = this.getFont();
                float g = (float)(-font.width(displayName) / 2);
                font.drawInBatch(
                        displayName, g, 0, 553648127, false, matrix4f, bufferSource, bl ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, j, packedLight
                );
                if (bl) {
                    font.drawInBatch(displayName, g, 0, -1, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);
                }

                poseStack.popPose();
            }
        }
    }

    @Override
    protected boolean shouldShowName(T entity) {
        if (Minecraft.renderNames() && (!entity.getItem().isEmpty() && entity.getItem().has(DataComponents.CUSTOM_NAME)
                && Minecraft.getInstance().crosshairPickEntity == entity)) {
            double distSqr = Minecraft.getInstance().crosshairPickEntity.distanceToSqr(entity);
            float showRangeSqr = entity.isDiscrete() ? 32.0f : 64.0f;
            return distSqr < (double) (showRangeSqr * showRangeSqr);
        }
        return false;
    }
}