package io.github.mortuusars.exposure.camera.viewfinder;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.mortuusars.exposure.camera.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class Viewfinder {
    public static float currentFov;
    public static float targetFov;

    private static boolean isActive;

    public static void setActive(boolean active) {
        isActive = active;
    }
    public static boolean isActive() {
        return isActive;
    }

    public static double getMouseSensitivityModifier() {
        return 1f; // TODO: modify sens
    }

    public static void render(PoseStack poseStack, float partialTicks) {
        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int size = Math.min(width, height);
        int xCenter = width / 2;
        int color = 0xFF050505;

        poseStack.pushPose();
        poseStack.translate(0, 0, 1000);

//        GuiComponent.fill(poseStack, 0, 0, (xCenter - size / 2), height, color);
//        GuiComponent.fill(poseStack, xCenter + size / 2, 0, width, height, color);

        ResourceLocation VIEWFINDER_TEXTURE = new ResourceLocation("exposure:textures/misc/viewfinder.png");

        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, VIEWFINDER_TEXTURE);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        float f = (float)Math.min(width, height);
        float f1 = Math.min((float)width / f, (float)height / f) * 1f;
        float f2 = f * f1;
        float f3 = f * f1;
        float f4 = ((float)width - f2) / 2.0F;
        float f5 = ((float)height - f3) / 2.0F;
        float f6 = f4 + f2;
        float f7 = f5 + f3;

        if (!Minecraft.getInstance().options.hideGui) {
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferbuilder.vertex((double)f4, (double)f7, -90.0D).uv(0.0F, 1.0F).endVertex();
            bufferbuilder.vertex((double)f6, (double)f7, -90.0D).uv(1.0F, 1.0F).endVertex();
            bufferbuilder.vertex((double)f6, (double)f5, -90.0D).uv(1.0F, 0.0F).endVertex();
            bufferbuilder.vertex((double)f4, (double)f5, -90.0D).uv(0.0F, 0.0F).endVertex();
            tesselator.end();
        }

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableTexture();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int colorAlpha = 240;
        bufferbuilder.vertex(0.0D, (double)height, -90.0D).color(0, 0, 0, colorAlpha).endVertex();
        bufferbuilder.vertex((double)width, (double)height, -90.0D).color(0, 0, 0, colorAlpha).endVertex();
        bufferbuilder.vertex((double)width, (double)f7, -90.0D).color(0, 0, 0, colorAlpha).endVertex();
        bufferbuilder.vertex(0.0D, (double)f7, -90.0D).color(0, 0, 0, colorAlpha).endVertex();
        bufferbuilder.vertex(0.0D, (double)f5, -90.0D).color(0, 0, 0, colorAlpha).endVertex();
        bufferbuilder.vertex((double)width, (double)f5, -90.0D).color(0, 0, 0, colorAlpha).endVertex();
        bufferbuilder.vertex((double)width, 0.0D, -90.0D).color(0, 0, 0, colorAlpha).endVertex();
        bufferbuilder.vertex(0.0D, 0.0D, -90.0D).color(0, 0, 0, colorAlpha).endVertex();
        bufferbuilder.vertex(0.0D, (double)f7, -90.0D).color(0, 0, 0, colorAlpha).endVertex();
        bufferbuilder.vertex((double)f4, (double)f7, -90.0D).color(0, 0, 0, colorAlpha).endVertex();
        bufferbuilder.vertex((double)f4, (double)f5, -90.0D).color(0, 0, 0, colorAlpha).endVertex();
        bufferbuilder.vertex(0.0D, (double)f5, -90.0D).color(0, 0, 0, colorAlpha).endVertex();
        bufferbuilder.vertex((double)f6, (double)f7, -90.0D).color(0, 0, 0, colorAlpha).endVertex();
        bufferbuilder.vertex((double)width, (double)f7, -90.0D).color(0, 0, 0, colorAlpha).endVertex();
        bufferbuilder.vertex((double)width, (double)f5, -90.0D).color(0, 0, 0, colorAlpha).endVertex();
        bufferbuilder.vertex((double)f6, (double)f5, -90.0D).color(0, 0, 0, colorAlpha).endVertex();
        tesselator.end();
        RenderSystem.enableTexture();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);


        poseStack.popPose();
    }

    public static int fovToFocalLength(float fov) {
        double sensorWidth = 36.0; // Sensor width in millimeters
        return (int) Math.round(sensorWidth / (2.0 * Math.tan(Math.toRadians(fov / 2.0))));
    }

    public static float focalLengthToFov(int focalLength) {
        double sensorWidth = 36.0; // Sensor width in millimeters
        return (float) (2.0 * Math.toDegrees(Math.atan(sensorWidth / (2.0 * focalLength))));
    }

    public static void modifyZoom(ZoomDirection direction) {
        float step = 10f * ( 1f - Mth.clamp((90 - currentFov) / 90, 0.3f, 1f));
        float inertia = Math.abs((targetFov - currentFov)) * 0.8f;

        float change = step + inertia;

        targetFov = Mth.clamp(targetFov += direction == ZoomDirection.IN ? +change : -change, 10f, 90f);
    }
}
