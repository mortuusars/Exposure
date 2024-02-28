package io.github.mortuusars.exposure.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Vector3f;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.render.modifiers.ExposurePixelModifiers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ItemFramePhotographRenderer {
    public static boolean render(ItemFrame itemFrameEntity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        ItemStack itemStack = itemFrameEntity.getItem();
        if (!(itemStack.getItem() instanceof PhotographItem photographItem))
            return false;

        // TODO: Check for frame entity
//        ResourceLocation frameEntityKey = ForgeRegistries.ENTITY_TYPES.getKey(event.getItemFrameEntity().getType());
//        if (frameEntityKey == null || frameEntityKey.toString().equals("quark:glass_frame"))
//            return;

        @Nullable Either<String, ResourceLocation> idOrTexture = photographItem.getIdOrTexture(itemStack);
        if (idOrTexture == null)
            return false;

        if (itemFrameEntity.getType() == EntityType.GLOW_ITEM_FRAME)
            packedLight = LightTexture.FULL_BRIGHT;

        poseStack.pushPose();

        // Snap to 90 degrees like a map.
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(45 * itemFrameEntity.getRotation()));

        float size = ExposureClient.getExposureRenderer().getSize();

        float scale = 1f / size;
        float pixelScale = scale / 16f;
        scale -= pixelScale * 6;

        poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-size / 2f, -size / 2f, 10);

        PhotographRenderer.renderPhotograph(photographItem, itemStack, false, false,
                poseStack, bufferSource, packedLight, 255, 255, 255, 255);

        poseStack.popPose();

        return true;
    }
}
