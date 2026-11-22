package io.github.mortuusars.exposure.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.client.render.photograph.HasPhotographRenderState;
import io.github.mortuusars.exposure.client.render.photograph.PhotographRenderState;
import io.github.mortuusars.exposure.client.render.photograph.PhotographStyle;
import io.github.mortuusars.exposure.event.ClientEvents;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFrameRenderer.class)
public abstract class ItemFrameRendererMixin<T extends ItemFrame> extends EntityRenderer<T, ItemFrameRenderState> {
    protected ItemFrameRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/decoration/ItemFrame;Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;F)V",
            at = @At(value = "TAIL"))
    void onExtractRenderState(T itemFrame, ItemFrameRenderState itemFrameRenderState, float f, CallbackInfo ci) {
        ItemStack stack = itemFrame.getItem();
        if (itemFrameRenderState instanceof HasPhotographRenderState hasPhotographRenderState) {
            if (stack.getItem() instanceof PhotographItem photographItem
                    && !photographItem.getFrame(stack).identifier().isEmpty()) {
                PhotographRenderState photographRenderState = new PhotographRenderState();

                photographRenderState.style = PhotographStyle.of(stack);
                photographRenderState.frame = photographItem.getFrame(stack);

                hasPhotographRenderState.setPhotographRenderState(photographRenderState);
            } else {
                hasPhotographRenderState.setPhotographRenderState(null);
            }
        }
    }

    @Redirect(method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"))
    private void onItemFrameRender(ItemStackRenderState item, PoseStack poseStack, SubmitNodeCollector collector,
                                   int light, int overlay, int outline, @Local(argsOnly = true) ItemFrameRenderState frame) {
        if (!ClientEvents.renderItemFrameItem(frame, poseStack, Minecraft.getInstance().renderBuffers().bufferSource(), light)) {
            item.submit(poseStack, collector, light, overlay, outline);
        }
    }
}