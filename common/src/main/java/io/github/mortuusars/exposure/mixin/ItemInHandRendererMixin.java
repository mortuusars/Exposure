package io.github.mortuusars.exposure.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderClient;
import io.github.mortuusars.exposure.render.PhotographInHandRenderer;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.item.StackedPhotographsItem;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Shadow
    private ItemStack mainHandItem;
    @Shadow
    private ItemStack offHandItem;
    @Shadow
    protected abstract void renderPlayerArm(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, float pEquippedProgress, float pSwingProgress, HumanoidArm pSide);
    @Shadow
    protected abstract float calculateMapTilt(float pPitch);
    @Shadow
    protected abstract void renderMapHand(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, HumanoidArm pSide);

    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void renderPhotograph(AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand,
                                  float swingProgress, ItemStack stack, float equipProgress, PoseStack poseStack,
                                  MultiBufferSource buffer, int combinedLight, CallbackInfo ci, boolean isMainHand, HumanoidArm arm) {
        if (ViewfinderClient.isLookingThrough()) {
            poseStack.popPose();
            ci.cancel();
            return;
        }

        if (stack.getItem() instanceof PhotographItem || stack.getItem() instanceof StackedPhotographsItem) {
            if (isMainHand && this.offHandItem.isEmpty()) {
                exposure$renderTwoHandedPhotograph(player, poseStack, buffer, combinedLight, pitch, equipProgress, swingProgress);
            } else {
                exposure$renderOneHandedPhotograph(player, poseStack, buffer, combinedLight, equipProgress, arm, swingProgress, stack);
            }

            poseStack.popPose();

            ci.cancel();
        }
    }

    @Unique
    private void exposure$renderOneHandedPhotograph(AbstractClientPlayer player, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, float pEquippedProgress, HumanoidArm pHand, float pSwingProgress, ItemStack stack) {
        float f = pHand == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        poseStack.translate(f * 0.125F, -0.125D, 0.0D);
        if (!player.isInvisible()) {
            poseStack.pushPose();
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(f * 10.0F));
            this.renderPlayerArm(poseStack, buffer, combinedLight, pEquippedProgress, pSwingProgress, pHand);
            poseStack.popPose();
        }

        poseStack.pushPose();
        poseStack.translate(f * 0.51F, -0.08F + pEquippedProgress * -1.2F, -0.75D);
        float f1 = Mth.sqrt(pSwingProgress);
        float f2 = Mth.sin(f1 * (float)Math.PI);
        float f3 = -0.5F * f2;
        float f4 = 0.4F * Mth.sin(f1 * ((float)Math.PI * 2F));
        float f5 = -0.3F * Mth.sin(pSwingProgress * (float)Math.PI);
        poseStack.translate(f * f3, f4 - 0.3F * f2, f5);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(f2 * -45.0F));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(f * f2 * -30.0F));
        PhotographInHandRenderer.renderPhotograph(poseStack, buffer, combinedLight, stack);
        poseStack.popPose();
    }

    @Unique
    private void exposure$renderTwoHandedPhotograph(AbstractClientPlayer player, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, float pPitch, float pEquippedProgress, float pSwingProgress) {
        float f = Mth.sqrt(pSwingProgress);
        float f1 = -0.2F * Mth.sin(pSwingProgress * (float)Math.PI);
        float f2 = -0.4F * Mth.sin(f * (float)Math.PI);
        pMatrixStack.translate(0.0D, -f1 / 2.0F, f2);
        float f3 = this.calculateMapTilt(pPitch);
        pMatrixStack.translate(0.0D, 0.04F + pEquippedProgress * -1.2F + f3 * -0.5F, -0.72F);
        pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(f3 * -85.0F));
        if (!player.isInvisible()) {
            pMatrixStack.pushPose();
            pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
            this.renderMapHand(pMatrixStack, pBuffer, pCombinedLight, HumanoidArm.RIGHT);
            this.renderMapHand(pMatrixStack, pBuffer, pCombinedLight, HumanoidArm.LEFT);
            pMatrixStack.popPose();
        }

        float f4 = Mth.sin(f * (float)Math.PI);
        pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(f4 * 20.0F));
        pMatrixStack.scale(2.0F, 2.0F, 2.0F);
        PhotographInHandRenderer.renderPhotograph(pMatrixStack, pBuffer, pCombinedLight, this.mainHandItem);
    }
}
