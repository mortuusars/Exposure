package io.github.mortuusars.exposure.client.gui.toast;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class BetterTutorialToast implements Toast {
    private static final Identifier BACKGROUND_SPRITE = Identifier.withDefaultNamespace("toast/tutorial");
    public static final int DEFAULT_SHOW_DURATION_MS = 7000;

    public Identifier backgroundSprite = BACKGROUND_SPRITE;
    public Runnable onHide = () -> { };

    protected final ToastIcon icon;
    protected final Component title;
    @Nullable
    protected final Component message;
    protected Toast.Visibility visibility = Toast.Visibility.SHOW;

    protected final long shownAt = System.currentTimeMillis();
    protected final int showDurationMs;
    protected final Supplier<Boolean> hideIf;

    public BetterTutorialToast(ToastIcon icon, Component title, @Nullable Component message, int showDurationMs, Supplier<Boolean> hideIf) {
        this.icon = icon;
        this.title = title;
        this.message = message;
        this.showDurationMs = showDurationMs;
        this.hideIf = hideIf;
    }

    public BetterTutorialToast(ToastIcon icon, Component title, @Nullable Component message, int showDurationMs) {
        this.icon = icon;
        this.title = title;
        this.message = message;
        this.showDurationMs = showDurationMs;
        this.hideIf = () -> false;
    }

    public BetterTutorialToast(ToastIcon icon, Component title, @Nullable Component message, Supplier<Boolean> hideIf) {
        this.icon = icon;
        this.title = title;
        this.message = message;
        this.showDurationMs = -1;
        this.hideIf = hideIf;
    }

    public void hide() {
        this.visibility = Toast.Visibility.HIDE;
    }

    @Override
    public void update(ToastManager toastManager, long visibilityTime) {
        if (visibility == Visibility.SHOW && hideIf.get() || (showDurationMs > 0 && System.currentTimeMillis() > shownAt + showDurationMs)) {
            hide();
            onHide.run();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, Font font, long timeSinceLastVisible) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, backgroundSprite, 0, 0, this.width(), this.height());
        this.icon.render(guiGraphics, 6, 6);
        if (this.message == null) {
            guiGraphics.drawString(font, this.title, 30, 12, 0xFF500050, false);
        } else {
            guiGraphics.drawString(font, this.title, 30, 7, 0xFF500050, false);
            guiGraphics.drawString(font, this.message, 30, 18, 0xFF000000, false);
        }

    }

    @Override
    public @NotNull Visibility getWantedVisibility() {
        return this.visibility;
    }
}
