package io.github.mortuusars.exposure.client.render;

import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.world.entity.GlassPhotographFrame;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GlassPhotographFrameEntityRenderer extends PhotographFrameEntityRenderer<GlassPhotographFrame> {
    public GlassPhotographFrameEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ModelResourceLocation getModelLocation(GlassPhotographFrame entity, int size) {
        return switch (size) {
            case 0 -> ExposureClient.Models.CLEAR_PHOTOGRAPH_FRAME_SMALL;
            case 1 -> ExposureClient.Models.CLEAR_PHOTOGRAPH_FRAME_MEDIUM;
            case 2 -> ExposureClient.Models.CLEAR_PHOTOGRAPH_FRAME_LARGE;
            default -> throw new IllegalArgumentException("size " + size + " is not valid. Expected 0-2.");
        };
    }

    @Override
    protected @NotNull RenderType getRenderType() {
        return Sheets.cutoutBlockSheet();
    }
}