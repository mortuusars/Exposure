package io.github.mortuusars.exposure.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Vector3f;
import io.github.mortuusars.exposure.item.PhotographItem;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderItemInFrameEvent;

import javax.annotation.Nullable;

public class ItemFramePhotographRenderer {
    public static void render(RenderItemInFrameEvent event) {
        ItemStack itemStack = event.getItemStack();
        if (!(itemStack.getItem() instanceof PhotographItem photographItem))
            return;

        @Nullable Either<String, ResourceLocation> idOrTexture = photographItem.getidOrTexture(itemStack);
        if (idOrTexture == null)
            return;

        ItemFrame entity = event.getItemFrameEntity();
        PoseStack poseStack = event.getPoseStack();

        // Snap to 90 degrees like a map.
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(45 * entity.getRotation()));
        int packedLight = entity.getType() == EntityType.GLOW_ITEM_FRAME ? LightTexture.FULL_BRIGHT : event.getPackedLight();

        float size = PhotographRenderer.SIZE;

        float scale = 1f / size;
        float pixelScale = scale / 16f;
        scale -= pixelScale * 6;

        poseStack.pushPose();
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-size / 2f, -size / 2f, 10);

        PhotographRenderer.render(idOrTexture, poseStack, event.getMultiBufferSource(), packedLight);
        poseStack.popPose();

        event.setCanceled(true);
    }
}
