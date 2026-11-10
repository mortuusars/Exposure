package io.github.mortuusars.exposure.client.gui.tooltip;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.EntityHitResult;

import java.util.List;

public class CameraStandTooltip {
    public static void render(GuiGraphics guiGraphics, DeltaTracker partialTick) {
        if (!Config.Client.CAMERA_STAND_TOOLTIP.get()) return;

        Minecraft minecraft = Minecrft.get();
        if (minecraft.options.hideGui) return;
        if (minecraft.level == null || minecraft.player == null) return;
        if (minecraft.screen != null || !(minecraft.hitResult instanceof EntityHitResult entityHitResult)) return;
        if (!(entityHitResult.getEntity() instanceof CameraStandEntity stand)) return;
        if (stand.getCamera().isEmpty()) return;

        int x = minecraft.getWindow().getGuiScaledWidth() / 2 + 16;
        int y = minecraft.getWindow().getGuiScaledHeight() / 2 - 9;

        if (stand.isMalfunctioned()) {
            List<FormattedCharSequence> lines = Minecrft.get().font.split(Component.translatable("gui.exposure.camera_stand.tooltip.malfunctioned")
                    .withStyle(ChatFormatting.RED), 230);
            guiGraphics.renderTooltip(minecraft.font, lines, x, y + 12);
        } else {
            TooltipRenderUtil.renderTooltipBackground(guiGraphics, x, y, 18, 18, 400);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 400);
            guiGraphics.renderItem(stand.getCamera(), x + 1, y + 1);
            guiGraphics.pose().popPose();

            guiGraphics.renderTooltip(minecraft.font, stand.getCamera(), x + 16, y + 12);
        }
    }
}
