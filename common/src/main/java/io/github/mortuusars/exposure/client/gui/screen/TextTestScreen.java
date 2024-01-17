package io.github.mortuusars.exposure.client.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.client.gui.screen.element.NewTextBox;
import io.github.mortuusars.exposure.client.gui.screen.element.TextBlock;
import io.github.mortuusars.exposure.client.gui.screen.element.TextBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TextTestScreen extends Screen {

    private NewTextBox textBox;

    public TextTestScreen() {
        super(Component.empty());
    }

    private String text = "initial";

    @Override
    protected void init() {
        textBox = new NewTextBox(font, width / 2 - 50, height / 2 - 20, 100, 40, () -> text, t -> text = t);



        addRenderableWidget(textBox);
    }

    @Override
    public void tick() {
        textBox.tick();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        renderBackground(guiGraphics);
        guiGraphics.fill(textBox.getX() - 4, textBox.getY() - 4,
                textBox.getX() + textBox.getWidth() + 4, textBox.getY() + textBox.getHeight() + 4,
                textBox.isHovered() ? 0xFFffedc5 : 0xFFfff9ec);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

//        guiGraphics.drawString(font, this.getFocused() != null ? this.getFocused().toString() : "NONE", 5, 5, 0xFFFFFFFF);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_TAB) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        return textBox.keyPressed(keyCode, scanCode,modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }
}
