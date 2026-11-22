package io.github.mortuusars.exposure.mixin.client;

import io.github.mortuusars.exposure.PlatformHelper;
import io.github.mortuusars.exposure.client.util.bugger.Bugger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.util.profiling.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugScreenOverlay.class)
public class BuggerScreenOverlayMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (!PlatformHelper.isInDevEnv()) return;

        if (Bugger.page == 0) {
            Profiler.get().push("bugger_main");
            Bugger.renderMainPage(guiGraphics);
            Profiler.get().pop();
            ci.cancel();
        }

        if (Bugger.page == 1) {
            Profiler.get().push("bugger_tag");
            Bugger.renderTagPage(guiGraphics);
            Profiler.get().pop();
            ci.cancel();
        }

        String str = "[<-] and [->] to switch pages";
        int strWidth = Minecraft.getInstance().font.width(str);
        int x = guiGraphics.guiWidth() / 2 - strWidth / 2;
        guiGraphics.fill(x - 1, 1, x + strWidth + 1, 10, -1873784752);
        guiGraphics.drawString(Minecraft.getInstance().font, str, x, 2, 0xFFFFFFFF, false);
    }
}
