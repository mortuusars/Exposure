package io.github.mortuusars.exposure.client.gui.toast;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public interface ToastIcon {
    ToastIcon MOVEMENT_KEYS = new TutorialIcon(TutorialToast.Icons.MOVEMENT_KEYS);
    ToastIcon MOUSE = new TutorialIcon(TutorialToast.Icons.MOUSE);
    ToastIcon TREE = new TutorialIcon(TutorialToast.Icons.TREE);
    ToastIcon RECIPE_BOOK = new TutorialIcon(TutorialToast.Icons.RECIPE_BOOK);
    ToastIcon WOODEN_PLANKS = new TutorialIcon(TutorialToast.Icons.WOODEN_PLANKS);
    ToastIcon SOCIAL_INTERACTIONS = new TutorialIcon(TutorialToast.Icons.SOCIAL_INTERACTIONS);
    ToastIcon RIGHT_CLICK = new TutorialIcon(TutorialToast.Icons.RIGHT_CLICK);
    ToastIcon HOVER = new SpriteIcon(Exposure.resource("toast/hover"));
    ToastIcon F1 = new SpriteIcon(Exposure.resource("toast/f1"));
    ToastIcon HEADS_UP = new SpriteIcon(Exposure.resource("toast/heads_up"));

    void render(GuiGraphics guiGraphics, int x, int y);

    class SpriteIcon implements ToastIcon {
        protected final Identifier sprite;

        public SpriteIcon(Identifier sprite) {
            this.sprite = sprite;
        }

        public void render(GuiGraphics guiGraphics, int x, int y) {
            //RenderSystem.enableBlend();
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprite, x, y, 20, 20);
        }
    }

    class TutorialIcon implements ToastIcon {
        protected final TutorialToast.Icons icon;

        public TutorialIcon(TutorialToast.Icons icon) {
            this.icon = icon;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int x, int y) {
            icon.render(guiGraphics, x, y);
        }
    }
}
