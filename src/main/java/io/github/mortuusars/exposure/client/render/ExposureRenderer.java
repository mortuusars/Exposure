package io.github.mortuusars.exposure.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import io.github.mortuusars.exposure.storage.ExposureSavedData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.MaterialColor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ExposureRenderer implements AutoCloseable {
    private final Map<String, ExposureRenderer.ExposureInstance> exposures = new HashMap<>();
    private final Map<String, ExposureRenderer.ExposureInstance> negativeExposures = new HashMap<>();

    public void render(String id, @NotNull ExposureSavedData exposureData, boolean negative,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float width, float height) {
        getOrCreateMapInstance(id, exposureData, negative).draw(poseStack, bufferSource, packedLight, width, height);
    }

    public void render(String id, @NotNull ExposureSavedData exposureData, boolean negative,
                       PoseStack poseStack, int packedLight, float width, float height) {
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        render(id, exposureData, negative, poseStack, bufferSource, packedLight, width, height);
        bufferSource.endBatch();
    }

    public void render(String id, @NotNull ExposureSavedData exposureData, boolean negative,
                       PoseStack poseStack, float width, float height) {
        render(id, exposureData, negative, poseStack, LightTexture.FULL_BRIGHT, width, height);
    }

    private ExposureRenderer.ExposureInstance getOrCreateMapInstance(String id, ExposureSavedData exposureData, boolean negative) {
        return (negative ? negativeExposures : exposures).compute(id, (expId, expData) -> {
            if (expData == null) {
                return new ExposureInstance(expId, exposureData, negative);
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
        private final RenderType renderType;
        private ExposureSavedData exposureData;
        private DynamicTexture texture;
        private boolean requiresUpload = true;

        ExposureInstance(String id, ExposureSavedData data, boolean negative) {
            this.exposureData = data;
            this.texture = new DynamicTexture(data.getWidth(), data.getHeight(), true);
            this.negative = negative;
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

        public void forceUpload() {
            this.requiresUpload = true;
        }

        private void updateTexture() {
            if (texture.getPixels() == null)
                return;

            for(int y = 0; y < exposureData.getHeight(); y++) {
                for(int x = 0; x < exposureData.getWidth(); x++) {
                    int bgr = MaterialColor.getColorFromPackedId(this.exposureData.getPixel(x, y));

                    if (negative) {
                        bgr = bgr ^ 0x00FFFFFF;
                        int blue = bgr >> 16 & 0xFF;
                        int green = bgr >> 8 & 0xFF;
                        int red = bgr & 0xFF;
//                        int luma = Mth.clamp((int) (0.4 * red + 0.6 * green + 0.15 * blue), 0, 255);
//
//                        //TODO: Proper negative transparency
//                        luma = (int) Mth.clamp(luma * luma / 255f - 100, 0, 255);
//                        luma = (255 - luma);
//
//                        bgr = (bgr & 0x00FFFFFF) | (luma << 24);
                    }

                    this.texture.getPixels().setPixelRGBA(x, y, bgr); // Texture is in BGR format
                }
            }

            this.texture.upload();
        }

        void draw(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float width, float height) {
            if (this.requiresUpload) {
                this.updateTexture();
                this.requiresUpload = false;
            }

            Matrix4f matrix4f = poseStack.last().pose();
            VertexConsumer vertexconsumer = bufferSource.getBuffer(this.renderType);
            vertexconsumer.vertex(matrix4f, 0, height, 0).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(packedLight).endVertex();
            vertexconsumer.vertex(matrix4f, width, height, 0).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(packedLight).endVertex();
            vertexconsumer.vertex(matrix4f, width, 0, 0).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(packedLight).endVertex();
            vertexconsumer.vertex(matrix4f, 0, 0, 0).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(packedLight).endVertex();
        }

        public void close() {
            this.texture.close();
        }
    }
}
