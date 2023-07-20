package io.github.mortuusars.exposure.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Vector3f;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.Photograph;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.storage.ExposureSavedData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.RenderItemInFrameEvent;
import net.minecraftforge.client.model.data.ModelData;

import java.util.Optional;

public class ItemFramePhotographRenderer {
    private static final ModelResourceLocation MAP_FRAME_LOCATION = new ModelResourceLocation("item_frame", "map=true");
    private static final ModelResourceLocation GLOW_MAP_FRAME_LOCATION = new ModelResourceLocation("glow_item_frame", "map=true");

    public static void render(RenderItemInFrameEvent event) {
        ItemStack itemStack = event.getItemStack();
        if (!(itemStack.getItem() instanceof PhotographItem photographItem))
            return;

        Optional<Either<String, ResourceLocation>> idOrResourceHolder = photographItem.getIdOrResource(itemStack);
        if (idOrResourceHolder.isEmpty())
            return;

        Either<String, ResourceLocation> idOrResource = idOrResourceHolder.get();

        //TODO: GLOW
        //TODO: BORDER/NO BORDER

        ItemFrame entity = event.getItemFrameEntity();
        ModelManager modelmanager = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getModelManager();
        ModelResourceLocation modelresourcelocation = entity.getType() == EntityType.GLOW_ITEM_FRAME ? GLOW_MAP_FRAME_LOCATION : MAP_FRAME_LOCATION;

        PoseStack poseStack = event.getPoseStack();

        // Snap to 90 degrees like a map.
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(45 * entity.getRotation()));

        poseStack.pushPose();
        poseStack.translate(-0.5D, -0.5D, -0.46875D * 2f);
        int packedLight = entity.getType() == EntityType.GLOW_ITEM_FRAME ? LightTexture.FULL_BRIGHT : event.getPackedLight();
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(poseStack.last(),
                event.getMultiBufferSource().getBuffer(Sheets.solidBlockSheet()), null,
                modelmanager.getModel(modelresourcelocation), 1.0F, 1.0F, 1.0F, packedLight, OverlayTexture.NO_OVERLAY,
                ModelData.EMPTY, RenderType.solid());
        poseStack.popPose();

        float size = PhotographRenderer.SIZE;

        float scale = 1f / size;
        float pixelScale = scale / 16f;
        // 1px border around a photograph:
        scale -= pixelScale * 2;

        poseStack.pushPose();
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-size / 2f, -size / 2f, -1);
        PhotographRenderer.render(idOrResource, poseStack, packedLight);
        poseStack.popPose();

        event.setCanceled(true);
    }
}
