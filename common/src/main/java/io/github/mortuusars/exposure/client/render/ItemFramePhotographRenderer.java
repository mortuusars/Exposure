package io.github.mortuusars.exposure.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.render.photograph.HasPhotographRenderState;
import io.github.mortuusars.exposure.client.render.photograph.PhotographRenderState;
import io.github.mortuusars.exposure.client.render.photograph.PhotographStyle;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;

public class ItemFramePhotographRenderer {
    public static void render(ItemFrameRenderState renderState, PoseStack poseStack, MultiBufferSource bufferSource,
                              int packedLight) {
        if (renderState.isGlowFrame)
            packedLight = LightTexture.FULL_BRIGHT;

        PhotographRenderState photographRenderState = ((HasPhotographRenderState) renderState).getPhotographRenderState();

        poseStack.pushPose();

        // should maybe fix this, but quark isn't even on 1.21.1 yet,
        // and they seem to only follow popular modding versions,
        // so it probably doesn't matter
        /*String entityName = BuiltInRegistries.ENTITY_TYPE.getKey(itemFrame.getType()).toString();
        if (entityName.equals("quark:glass_frame")) {
            poseStack.translate(0, 0, 0.475f);
        }*/

        // Snap to 90 degrees like a map.
        poseStack.mulPose(Axis.ZP.rotationDegrees(45 * renderState.rotation));

        float pixelSize = 0.0625f;
        float scale = 1f - pixelSize * 6; // 3px from each side

        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-0.5, -0.5, 0.045);

        PhotographStyle style = photographRenderState.style;
        Frame frame = photographRenderState.frame;

        ExposureClient.photographRenderer().renderPhotograph(poseStack, bufferSource, style, frame,
                false, false, packedLight, 255, 255, 255, 255);

        poseStack.popPose();
    }
}
