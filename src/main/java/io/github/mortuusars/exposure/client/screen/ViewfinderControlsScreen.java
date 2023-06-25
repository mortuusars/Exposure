package io.github.mortuusars.exposure.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class ViewfinderControlsScreen extends Screen {
    private final Player player;
    private final ClientLevel level;
    private long openedAtTimestamp;

    private InputConstants.Key sneakKey;
    private boolean sneakWasReleased;

    public ViewfinderControlsScreen() {
        super(Component.empty());

        player = Minecraft.getInstance().player;
        level = Minecraft.getInstance().level;
        assert level != null;
        openedAtTimestamp = level.getGameTime();

        sneakKey = Minecraft.getInstance().options.keyShift.getKey();
        sneakWasReleased = false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (!Camera.getViewfinder().isActive(player)) {
            this.onClose();
            return;
        }

        fill(poseStack, 200, 50, 420, 250, 0xAAFF88AA);

        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (Minecraft.getInstance().options.keyShift.matches(keyCode, scanCode)) {
            if (level.getGameTime() - openedAtTimestamp >= 5)
                this.onClose();

            return true;
        }

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {


        return super.mouseScrolled(mouseX, mouseY, delta);
    }
}
