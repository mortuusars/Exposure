package io.github.mortuusars.exposure.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.PlatformHelperClient;
import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.client.render.image.RenderCoordinates;
import io.github.mortuusars.exposure.client.render.photograph.PhotographStyle;
import io.github.mortuusars.exposure.client.render.state.PhotographFrameEntityRenderState;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.entity.PhotographFrameEntity;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

public class PhotographFrameEntityRenderer<T extends PhotographFrameEntity> extends EntityRenderer<T, PhotographFrameEntityRenderState> {
    protected final BlockRenderDispatcher blockRenderer;

    public PhotographFrameEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public @NotNull PhotographFrameEntityRenderState createRenderState() {
        return new PhotographFrameEntityRenderState();
    }

    public Identifier getModelLocation(PhotographFrameEntityRenderState state, int size) {
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
    public void extractRenderState(T entity, PhotographFrameEntityRenderState reusedState, float partialTick) {
        super.extractRenderState(entity, reusedState, partialTick);
        reusedState.direction = entity.getDirection();
        reusedState.item = entity.getItem();
        reusedState.size = entity.getSize();
        reusedState.isGlowing = entity.isGlowing();
        reusedState.rotation = entity.getItemRotation();
        reusedState.photographBrightness = getPhotographBrightness(entity, reusedState);

        // TODO: idk where to put this?
        if (Minecraft.getInstance().hitResult instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() == entity) {
            Minecraft.getInstance().crosshairPickEntity = entity;
        }
    }

    @Override
    public void submit(PhotographFrameEntityRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        Direction direction = state.direction;
        int size = state.size;
        MultiBufferSource.BufferSource bufferSource = Minecrft.get().renderBuffers().bufferSource();
        int packedLight = state.lightCoords;

        poseStack.pushPose();
        // Offsets name tag rendering to be like item frame:
        poseStack.translate(direction.getStepX() * 0.3f, direction.getStepY() * 0.3f, direction.getStepZ() * 0.3f);
        super.submit(state, poseStack, nodeCollector, cameraRenderState);
        poseStack.popPose();

        poseStack.pushPose();

        // thickness of the frame is 1px (0.5 - (1/16 * 0.5)) (0.5 is because we are offsetting from the center)
        // stripped frame is thin, so 1/16 becomes 0.15/16 (thickness of the backplate)
        double hangOffset = 0.46875;
        poseStack.translate(direction.getStepX() * hangOffset, direction.getStepY() * hangOffset, direction.getStepZ() * hangOffset);

        float xRot;
        float yRot;
        if (direction.getAxis().isHorizontal()) {
            xRot = 0.0F;
            yRot = 180.0F - direction.toYRot();
        } else {
            xRot = (float)(-90 * direction.getAxisDirection().getStep());
            yRot = 180.0F;
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));

        ItemStack item = state.item;
        if (!item.isEmpty()) {
            renderPhotograph(state, poseStack, bufferSource, packedLight, item, size);
        }

        if (!state.isInvisible) {
            renderFrame(state, poseStack, bufferSource, packedLight, size);
        }

        poseStack.popPose();
        bufferSource.endBatch();
    }

    protected void renderFrame(@NotNull PhotographFrameEntityRenderState state, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource,
                             int packedLight, int size) {
        poseStack.pushPose();
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        Identifier modelLocation = getModelLocation(state, size);
        BlockStateModel model = PlatformHelperClient.getModel(modelLocation);
        blockRenderer.getModelRenderer().renderModel(poseStack.last(), bufferSource.getBuffer(getRenderType()),
                model, 1.0f, 1.0f, 1.0f, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    protected boolean renderPhotograph(@NotNull PhotographFrameEntityRenderState state, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource,
                                  int packedLight, ItemStack item, int size) {
        poseStack.pushPose();

        boolean frameInvisible = state.isInvisible;

        float frameBorderOffset = frameInvisible ? 0f : 0.125f; // (2px / 16px = 0.125)
        float offsetFromCenter = frameInvisible ? 0.497f : 0.48f;
        offsetFromCenter -= Config.Client.PHOTOGRAPH_FRAME_IMAGE_OFFSET.get();
        float desiredSize = size + 1 - frameBorderOffset * 2;

        poseStack.mulPose(Axis.ZP.rotationDegrees((state.rotation * 360.0F / 4.0F)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.translate(-0.5 * (size + 1) + frameBorderOffset, -0.5 * (size + 1) + frameBorderOffset, offsetFromCenter);
        poseStack.scale(desiredSize, desiredSize, 1);

        boolean isGlowing = state.isGlowing;
        if (isGlowing) {
            packedLight = LightTexture.FULL_BRIGHT;
        }

        int brightness = isGlowing ? 255 : state.photographBrightness;

        boolean photographRendered = false;

        if (Config.Client.PIXEL_PERFECT_PHOTOGRAPH_FRAME.get()) {
            if (item.getItem() instanceof PhotographItem photographItem) {
                PhotographStyle style = PhotographStyle.of(item);
                Frame frame = photographItem.getFrame(item);

                RenderableImage image = style.process(ExposureClient.renderedExposures().getOrCreate(frame));

                int pixels = 16 * (state.size + 1);
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

    public int getPhotographBrightness(T entity, PhotographFrameEntityRenderState reusedState) {
        if (reusedState.direction == Direction.UP)
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
    protected boolean shouldShowName(T entity, double distanceToCameraSq) {
        if (Minecraft.renderNames() && (!entity.getItem().isEmpty() && entity.getItem().has(DataComponents.CUSTOM_NAME)
                && Minecraft.getInstance().crosshairPickEntity == entity)) {
            double distSqr = Minecraft.getInstance().crosshairPickEntity.distanceToSqr(entity);
            float showRangeSqr = entity.isDiscrete() ? 32.0f : 64.0f;
            return distSqr < (double) (showRangeSqr * showRangeSqr);
        }
        return false;
    }
}
