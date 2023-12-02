package io.github.mortuusars.exposure.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Either;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

public class NewExposureRenderer implements AutoCloseable {
    private final Map<String, ExposureInstance> cache = new HashMap<>();

    public void render(@NotNull Either<String, ResourceLocation> idOrTexture, boolean negative, boolean simulateFilm,
                       PoseStack poseStack, MultiBufferSource bufferSource,
                       float minX, float minY, float maxX, float maxY,
                       float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
        @Nullable ExposureImage exposure = idOrTexture.map(
                id -> Exposure.getStorage().getOrQuery(id).map(data -> new ExposureImage(id, data)).orElse(null),
                texture -> {
                    AbstractTexture texture1 = Minecraft.getInstance().getTextureManager().getTexture(texture);// Load

                    return new ExposureImage(texture.toString(), Minecraft.getInstance()
                            .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                            .apply(texture));
                });

        if (exposure != null) {
            String id = idOrTexture.map(expId -> expId, ResourceLocation::toString);
            getOrCreateExposureInstance(id, exposure, negative, simulateFilm)
                    .draw(poseStack, bufferSource, minX, minY, maxX, maxY, minU, minV, maxU, maxV, packedLight, r, g, b, a);
        }
    }

    private ExposureInstance getOrCreateExposureInstance(String id, ExposureImage exposure,
                                                         boolean negative, boolean simulateFilm) {
        id = id.toLowerCase();
        id = "exposure/" + id + (negative ? "_negative" : "") + (simulateFilm ? "_film" : "");
        // colon will throw further down because of ResourceLocation init:
        id = id.replace(':', '_');

        return (this.cache).compute(id, (expId, expData) -> {
            if (expData == null) {
                exposure.validate();
                return new ExposureInstance(expId, exposure, negative, simulateFilm);
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
        private final boolean negative;
        private final boolean simulateFilm;
        private final RenderType renderType;

        private ExposureImage exposure;
        private DynamicTexture texture;
        private boolean requiresUpload = true;

        ExposureInstance(String id, ExposureImage exposure, boolean negative, boolean simulateFilm) {
            this.exposure = exposure;
            this.texture = new DynamicTexture(exposure.getWidth(), exposure.getHeight(), true);
            this.negative = negative;
            this.simulateFilm = simulateFilm;
            ResourceLocation resourcelocation = Minecraft.getInstance().textureManager.register(id, this.texture);
            this.renderType = RenderType.text(resourcelocation);
        }

        private void replaceData(ExposureImage exposure) {
            boolean hasChanged = !this.exposure.equals(exposure);
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

                    if (negative) {
                        int blue = ABGR >> 16 & 0xFF;
                        int green = ABGR >> 8 & 0xFF;
                        int red = ABGR & 0xFF;

                        // Invert:
                        ABGR = ABGR ^ 0x00FFFFFF;

                        // Modify opacity to make lighter colors transparent, like in real film.
                        if (simulateFilm) {
                            int brightness = (blue + green + red) / 3;
                            int opacity = (int) Mth.clamp(brightness * 1.5f, 0, 255);
                            ABGR = (ABGR & 0x00FFFFFF) | (opacity << 24);
                        }
                    }

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
