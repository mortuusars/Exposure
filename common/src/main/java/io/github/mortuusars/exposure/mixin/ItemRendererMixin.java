package io.github.mortuusars.exposure.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true)
    BakedModel renderItem(BakedModel model, ItemStack itemStack, ItemDisplayContext displayContext, boolean leftHand,
                          PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (Minecraft.getInstance().level != null && itemStack.is(Exposure.Items.CAMERA.get()) && displayContext == ItemDisplayContext.GUI) {
            BakedModel guiModel = Minecraft.getInstance().getModelManager()
                    .getModel(new ModelResourceLocation("exposure", "camera_gui", "inventory"));

            return guiModel.getOverrides().resolve(guiModel, itemStack, Minecraft.getInstance().level, null, 0);
        }
        return model;
    }
}
