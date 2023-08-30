package io.github.mortuusars.exposure.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import io.github.mortuusars.exposure.storage.ExposureSavedData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.MaterialColor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ExposureRenderer implements AutoCloseable {
    private final Map<String, ExposureRenderer.ExposureInstance> exposures = new HashMap<>();
    private final Map<String, ExposureRenderer.ExposureInstance> negativeExposures = new HashMap<>();

    // Regular:

    public void render(String id, @NotNull ExposureSavedData exposureData, PoseStack poseStack, MultiBufferSource bufferSource,
                       float minX, float minY, float maxX, float maxY,
                       float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
        render(id, exposureData, false, false, poseStack, bufferSource, minX, minY, maxX, maxY, minU, minV, maxU, maxV, packedLight, r, g, b, a);
    }

    public void render(String id, @NotNull ExposureSavedData exposureData, PoseStack poseStack,
                       float minX, float minY, float maxX, float maxY,
                       float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        render(id, exposureData, false, false, poseStack, bufferSource, minX, minY, maxX, maxY, minU, minV, maxU, maxV, packedLight, r, g, b, a);
        bufferSource.endBatch();
    }

    public void render(String id, @NotNull ExposureSavedData exposureData, PoseStack poseStack, MultiBufferSource bufferSource,
                       float width, float height, int packedLight, int r, int g, int b, int a) {
        render(id, exposureData, false, false, poseStack, bufferSource, 0, 0, width, height, 0, 0, 1, 1, packedLight, r, g, b, a);
    }

    public void render(String id, @NotNull ExposureSavedData exposureData, PoseStack poseStack,
                       float width, float height, int packedLight, int r, int g, int b, int a) {
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        render(id, exposureData, false, false, poseStack, bufferSource, 0, 0, width, height, 0, 0, 1, 1, packedLight, r, g, b, a);
        bufferSource.endBatch();
    }

    // Negative:

    public void renderNegative(String id, @NotNull ExposureSavedData exposureData, boolean simulateFilm, PoseStack poseStack, MultiBufferSource bufferSource,
                       float minX, float minY, float maxX, float maxY,
                       float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
        render(id, exposureData, true, simulateFilm, poseStack, bufferSource, minX, minY, maxX, maxY, minU, minV, maxU, maxV, packedLight, r, g, b, a);
    }

    public void renderNegative(String id, @NotNull ExposureSavedData exposureData, boolean simulateFilm, PoseStack poseStack, MultiBufferSource bufferSource,
                       float width, float height, int packedLight, int r, int g, int b, int a) {
        render(id, exposureData, true, simulateFilm, poseStack, bufferSource, 0, 0, width, height, 0, 0, 1, 1, packedLight, r, g, b, a);
    }

    public void renderNegative(String id, @NotNull ExposureSavedData exposureData, boolean simulateFilm, PoseStack poseStack,
                       float width, float height, int packedLight, int r, int g, int b, int a) {
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        render(id, exposureData, true, simulateFilm, poseStack, bufferSource, 0, 0, width, height, 0, 0, 1, 1, packedLight, r, g, b, a);
        bufferSource.endBatch();
    }

    private void render(String id, @NotNull ExposureSavedData exposureData, boolean negative, boolean simulateFilm, PoseStack poseStack, MultiBufferSource bufferSource,
                        float minX, float minY, float maxX, float maxY,
                        float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
        getOrCreateMapInstance(id, exposureData, negative, simulateFilm).draw(poseStack, bufferSource, minX, minY, maxX, maxY, minU, minV, maxU, maxV, packedLight, r, g, b, a);
    }

    private ExposureRenderer.ExposureInstance getOrCreateMapInstance(String id, ExposureSavedData exposureData, boolean negative, boolean simulateFilm) {
        return (negative ? negativeExposures : exposures).compute(id, (expId, expData) -> {
            if (expData == null) {
                return new ExposureInstance(expId, exposureData, negative, simulateFilm);
            } else {
                expData.replaceExposureData(exposureData);
                return expData;
            }
        });
    }

    public void clearData() {
        for(ExposureRenderer.ExposureInstance instance : exposures.values()) {
            instance.close();
        }

        exposures.clear();

        for(ExposureRenderer.ExposureInstance instance : negativeExposures.values()) {
            instance.close();
        }

        negativeExposures.clear();
    }

    @Override
    public void close() {
        clearData();
    }

    static class ExposureInstance implements AutoCloseable {
        private final boolean negative;
        private final boolean simulateFilm;
        private final RenderType renderType;
        private ExposureSavedData exposureData;
        private DynamicTexture texture;
        private boolean requiresUpload = true;

        ExposureInstance(String id, ExposureSavedData data, boolean negative, boolean simulateFilm) {
            this.exposureData = data;
            this.texture = new DynamicTexture(data.getWidth(), data.getHeight(), true);
            this.negative = negative;
            this.simulateFilm = simulateFilm;
            ResourceLocation resourcelocation = Minecraft.getInstance().textureManager
                    .register("exposure/" + id.toLowerCase() + (negative ? "negative" : ""), this.texture);
            this.renderType = RenderType.text(resourcelocation);
        }

        private void replaceExposureData(ExposureSavedData exposureData) {
            boolean flag = this.exposureData != exposureData;
            this.exposureData = exposureData;
            if (flag)
                this.texture = new DynamicTexture(exposureData.getWidth(), exposureData.getHeight(), true);
            this.requiresUpload |= flag;
        }

        @SuppressWarnings("unused")
        public void forceUpload() {
            this.requiresUpload = true;
        }

        private void updateTexture() {
            if (texture.getPixels() == null)
                return;

            for(int y = 0; y < exposureData.getHeight(); y++) {
                for(int x = 0; x < exposureData.getWidth(); x++) {
                    int BGR = MaterialColor.getColorFromPackedId(this.exposureData.getPixel(x, y));

                    if (negative) {
                        int blue = BGR >> 16 & 0xFF;
                        int green = BGR >> 8 & 0xFF;
                        int red = BGR & 0xFF;

                        // Invert:
                        BGR = BGR ^ 0x00FFFFFF;

                        // Modify opacity to make lighter colors transparent, like in real film.
                        if (simulateFilm) {
                            int brightness = (blue + green + red) / 3;
                            int opacity = (int)Mth.clamp(brightness * 1.5f, 0, 255);
                            BGR = (BGR & 0x00FFFFFF) | (opacity << 24);
                        }
                    }

                    this.texture.getPixels().setPixelRGBA(x, y, BGR); // Texture is in BGR format
                }
            }

            this.texture.upload();
        }

        void draw(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float width, float height, int r, int g, int b, int a) {
            draw(poseStack, bufferSource, 0, 0, width, height, 0, 0, 1, 1, packedLight, r, g, b, a);
        }

        void draw(PoseStack poseStack, MultiBufferSource bufferSource, float minX, float minY, float maxX, float maxY,
                  float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
            if (this.requiresUpload) {
                this.updateTexture();
                this.requiresUpload = false;
            }

            Matrix4f matrix4f = poseStack.last().pose();
            VertexConsumer vertexconsumer = bufferSource.getBuffer(this.renderType);
            vertexconsumer.vertex(matrix4f, minX, maxY, 0).color(r, g, b, a).uv(minU, maxV).uv2(packedLight).endVertex();
            vertexconsumer.vertex(matrix4f, maxX, maxY, 0).color(r, g, b, a).uv(maxU, maxV).uv2(packedLight).endVertex();
            vertexconsumer.vertex(matrix4f, maxX, minY, 0).color(r, g, b, a).uv(maxU, minV).uv2(packedLight).endVertex();
            vertexconsumer.vertex(matrix4f, minX, minY, 0).color(r, g, b, a).uv(minU, minV).uv2(packedLight).endVertex();
        }

        public void close() {
            this.texture.close();
        }
    }
}
