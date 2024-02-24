package io.github.mortuusars.exposure.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Axis;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.render.modifiers.IPixelModifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExposureRenderer implements AutoCloseable {
    public static final ResourceLocation PHOTOGRAPH_TEXTURE = Exposure.resource("textures/photograph/photograph.png");
    public static final ResourceLocation AGED_PHOTOGRAPH_TEXTURE = Exposure.resource("textures/photograph/aged_photograph.png");
    public static final ResourceLocation AGED_PHOTOGRAPH_OVERLAY_TEXTURE = Exposure.resource("textures/photograph/aged_photograph_overlay.png");
    public static final int SIZE = 256;

    private final Map<String, ExposureInstance> cache = new HashMap<>();

    public void render(@NotNull Either<String, ResourceLocation> idOrTexture, IPixelModifier modifier,
                       PoseStack poseStack, float x, float y, float width, float height) {
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        render(idOrTexture, modifier, poseStack, bufferSource, x, y, x + width, y + height,
                0, 0, 1, 1, LightTexture.FULL_BRIGHT, 255, 255, 255, 255);
        bufferSource.endBatch();
    }

    public void render(@NotNull Either<String, ResourceLocation> idOrTexture, IPixelModifier modifier,
                       PoseStack poseStack, MultiBufferSource bufferSource, float minX, float minY, float maxX, float maxY,
                       float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
        @Nullable ExposureImage exposure = idOrTexture.map(
                id -> ExposureClient.getExposureStorage().getOrQuery(id)
                        .map(data -> new ExposureImage(id, data))
                        .orElse(null),
                texture -> {
                    @Nullable ExposureTexture exposureTexture = ExposureTexture.getTexture(texture);
                    if (exposureTexture != null)
                        return new ExposureImage(texture.toString(), exposureTexture);
                    else
                        return null;
                }
        );

        if (exposure != null) {
            String id = idOrTexture.map(expId -> expId, ResourceLocation::toString);
            getOrCreateExposureInstance(id, exposure, modifier)
                    .draw(poseStack, bufferSource, minX, minY, maxX, maxY, minU, minV, maxU, maxV, packedLight, r, g, b, a);
        }
    }

    public void renderOnPaper(@NotNull Either<String, ResourceLocation> idOrTexture, IPixelModifier modifiers,
                       PoseStack poseStack, MultiBufferSource bufferSource,
                       float minX, float minY, float maxX, float maxY,
                       float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a,
                       boolean renderBackside) {
        renderPaperTexture(poseStack, bufferSource,
                0, 0, SIZE, SIZE, 0, 0, 1, 1,
                packedLight, r, g, b, a);

        if (renderBackside) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
            poseStack.translate(-SIZE, 0, -0.5);

            renderTexture(PHOTOGRAPH_TEXTURE, poseStack, bufferSource,
                    0, 0, SIZE, SIZE, 1, 0, 0, 1,
                    packedLight, (int)(r * 0.85f), (int)(g * 0.85f), (int)(b * 0.85f), a);

            poseStack.popPose();
        }

        poseStack.pushPose();
        float offset = SIZE * 0.0625f;
        poseStack.translate(offset, offset, 1);
        poseStack.scale(0.875f, 0.875f, 0.875f);
        render(idOrTexture, modifiers, poseStack, bufferSource,
                minX, minY, maxX, maxY, minU, minV, maxU, maxV, packedLight, r, g, b, a);
        poseStack.popPose();
    }

    public void renderAgedOnPaper(@NotNull Either<String, ResourceLocation> idOrTexture, IPixelModifier modifier,
                              PoseStack poseStack, MultiBufferSource bufferSource,
                              float minX, float minY, float maxX, float maxY,
                              float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a,
                              boolean renderBackside) {
        renderTexture(AGED_PHOTOGRAPH_TEXTURE, poseStack, bufferSource,
                0, 0, SIZE, SIZE, 0, 0, 1, 1,
                packedLight, r, g, b, a);

        if (renderBackside) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
            poseStack.translate(-SIZE, 0, -0.5);

            renderTexture(AGED_PHOTOGRAPH_TEXTURE, poseStack, bufferSource,
                    0, 0, SIZE, SIZE, 1, 0, 0, 1,
                    packedLight, (int)(r * 0.85f), (int)(g * 0.85f), (int)(b * 0.85f), a);

            poseStack.popPose();
        }

        poseStack.pushPose();
        float offset = SIZE * 0.0625f;
        poseStack.translate(offset, offset, 1);
        poseStack.scale(0.875f, 0.875f, 0.875f);
        render(idOrTexture, modifier, poseStack, bufferSource,
                minX, minY, maxX, maxY, minU, minV, maxU, maxV, packedLight, r, g, b, a);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0, 0, 1);
        renderTexture(AGED_PHOTOGRAPH_OVERLAY_TEXTURE, poseStack, bufferSource,
                0, 0, SIZE, SIZE, 0, 0, 1, 1,
                packedLight, r, g, b, a);
        poseStack.popPose();
    }

    public void renderPaperTexture(PoseStack poseStack, MultiBufferSource bufferSource,
                                   float minX, float minY, float maxX, float maxY,
                                   float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
        renderTexture(PHOTOGRAPH_TEXTURE, poseStack, bufferSource, minX, minY, maxX, maxY,
                minU, minV, maxU, maxV, packedLight, r, g, b, a);
    }

    @SuppressWarnings("SameParameterValue")
    private static void renderTexture(ResourceLocation resource, PoseStack poseStack, MultiBufferSource bufferSource,
                                      float minX, float minY, float maxX, float maxY,
                                      float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
        RenderSystem.setShaderTexture(0, resource);
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer bufferBuilder = bufferSource.getBuffer(RenderType.text(resource));
        bufferBuilder.vertex(matrix, minX, maxY, 0).color(r, g, b, a).uv(minU, maxV).uv2(packedLight).endVertex();
        bufferBuilder.vertex(matrix, maxX, maxY, 0).color(r, g, b, a).uv(maxU, maxV).uv2(packedLight).endVertex();
        bufferBuilder.vertex(matrix, maxX, minY, 0).color(r, g, b, a).uv(maxU, minV).uv2(packedLight).endVertex();
        bufferBuilder.vertex(matrix, minX, minY, 0).color(r, g, b, a).uv(minU, minV).uv2(packedLight).endVertex();
    }

    private ExposureInstance getOrCreateExposureInstance(String id, ExposureImage exposure, IPixelModifier modifier) {
        String instanceId = id + modifier.getIdSuffix();
        return (this.cache).compute(instanceId, (expId, expData) -> {
            if (expData == null) {
                return new ExposureInstance(expId, exposure, modifier);
            } else {
                expData.replaceData(exposure);
                return expData;
            }
        });
    }

    public void clearData() {
        for (ExposureInstance instance : cache.values()) {
            instance.close();
        }

        cache.clear();
    }

    @Override
    public void close() {
        clearData();
    }

    static class ExposureInstance implements AutoCloseable {
        private final RenderType renderType;

        private ExposureImage exposure;
        private DynamicTexture texture;
        private final IPixelModifier pixelModifier;
        private boolean requiresUpload = true;

        ExposureInstance(String id, ExposureImage exposure, IPixelModifier modifier) {
            this.exposure = exposure;
            this.texture = new DynamicTexture(exposure.getWidth(), exposure.getHeight(), true);
            this.pixelModifier = modifier;
            String textureId = createTextureId(id);
            ResourceLocation resourcelocation = Minecraft.getInstance().getTextureManager().register(textureId, this.texture);
            this.renderType = RenderType.text(resourcelocation);
        }

        private static String createTextureId(String exposureId) {
            String id = "exposure/" + exposureId.toLowerCase();
            id = id.replace(':', '_');

            // Player nicknames can have non az09 chars
            // we need to remove all invalid chars from the id to create ResourceLocation,
            // otherwise it crashes
            Pattern pattern = Pattern.compile("[^a-z0-9_.-]");
            Matcher matcher = pattern.matcher(id);

            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                matcher.appendReplacement(sb, String.valueOf(matcher.group().hashCode()));
            }
            matcher.appendTail(sb);

            return sb.toString();
        }

        private void replaceData(ExposureImage exposure) {
            boolean hasChanged = !this.exposure.getName().equals(exposure.getName());
            this.exposure = exposure;
            if (hasChanged) {
                this.texture = new DynamicTexture(exposure.getWidth(), exposure.getHeight(), true);
            }
            this.requiresUpload |= hasChanged;
        }

        @SuppressWarnings("unused")
        public void forceUpload() {
            this.requiresUpload = true;
        }

        private void updateTexture() {
            if (texture.getPixels() == null)
                return;

            for (int y = 0; y < this.exposure.getWidth(); y++) {
                for (int x = 0; x < this.exposure.getHeight(); x++) {
                    int ABGR = this.exposure.getPixelABGR(x, y);
                    ABGR = pixelModifier.modifyPixel(ABGR);
                    this.texture.getPixels().setPixelRGBA(x, y, ABGR); // Texture is in BGR format
                }
            }

            this.texture.upload();
        }

        void draw(PoseStack poseStack, MultiBufferSource bufferSource, float minX, float minY, float maxX, float maxY,
                  float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
            if (this.requiresUpload) {
                this.updateTexture();
                this.requiresUpload = false;
            }

            Matrix4f matrix4f = poseStack.last().pose();
            VertexConsumer vertexconsumer = bufferSource.getBuffer(this.renderType);
            vertexconsumer.vertex(matrix4f, minX, maxY, 0).color(r, g, b, a).uv(minU, maxV).uv2(packedLight)
                    .endVertex();
            vertexconsumer.vertex(matrix4f, maxX, maxY, 0).color(r, g, b, a).uv(maxU, maxV).uv2(packedLight)
                    .endVertex();
            vertexconsumer.vertex(matrix4f, maxX, minY, 0).color(r, g, b, a).uv(maxU, minV).uv2(packedLight)
                    .endVertex();
            vertexconsumer.vertex(matrix4f, minX, minY, 0).color(r, g, b, a).uv(minU, minV).uv2(packedLight)
                    .endVertex();
        }

        public void close() {
            this.texture.close();
        }
    }
}
