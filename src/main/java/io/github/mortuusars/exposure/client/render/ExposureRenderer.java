package io.github.mortuusars.exposure.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.storage.ExposureSavedData;
import io.github.mortuusars.exposure.util.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Exposure.ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ExposureRenderer {
    private static final Map<String, ExposureRenderer.ExposureInstance> exposures = new HashMap<>();

    @SubscribeEvent
    public static void onLevelClear(LevelEvent.Unload event) {
        close();
    }

    public static void render(PoseStack poseStack, MultiBufferSource bufferSource, String id, @NotNull ExposureSavedData exposureData, int packedLight) {
        getOrCreateMapInstance(id, exposureData).draw(poseStack, bufferSource, packedLight);
    }

    private static ExposureRenderer.ExposureInstance getOrCreateMapInstance(String id, ExposureSavedData exposureData) {
        return exposures.compute(id, (expId, expData) -> {
            if (expData == null) {
                return new ExposureInstance(expId, exposureData);
            } else {
                expData.replaceExposureData(exposureData);
                return expData;
            }
        });
    }

    public static void resetData() {
        for(ExposureRenderer.ExposureInstance instance : exposures.values()) {
            instance.close();
        }

        exposures.clear();
    }

    public static void close() {
        resetData();
    }

    static class ExposureInstance implements AutoCloseable {
        private ExposureSavedData exposureData;
        private final DynamicTexture texture;
        private final RenderType renderType;
        private boolean requiresUpload = true;

        ExposureInstance(String id, ExposureSavedData data) {
            this.exposureData = data;
            this.texture = new DynamicTexture(data.getWidth(), data.getHeight(), true);
            ResourceLocation resourcelocation = Minecraft.getInstance().textureManager.register("exposure/" + id.toLowerCase(), this.texture);
            this.renderType = RenderType.text(resourcelocation);
        }

        private void replaceExposureData(ExposureSavedData exposureData) {
            boolean flag = this.exposureData != exposureData;
            this.exposureData = exposureData;
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
                    this.texture.getPixels().setPixelRGBA(x, y, bgr); // Texture is in BGR format
                }
            }

            this.texture.upload();
        }

        void draw(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
            if (this.requiresUpload) {
                this.updateTexture();
                this.requiresUpload = false;
            }

            int width = exposureData.getWidth();
            int height = exposureData.getHeight();
            Matrix4f matrix4f = poseStack.last().pose();
            VertexConsumer vertexconsumer = bufferSource.getBuffer(this.renderType);
            vertexconsumer.vertex(matrix4f, 0, height, -0.01F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(packedLight).endVertex();
            vertexconsumer.vertex(matrix4f, width, height, -0.01F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(packedLight).endVertex();
            vertexconsumer.vertex(matrix4f, width, 0, -0.01F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(packedLight).endVertex();
            vertexconsumer.vertex(matrix4f, 0, 0, -0.01F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(packedLight).endVertex();
        }

        public void close() {
            this.texture.close();
        }
    }
}
