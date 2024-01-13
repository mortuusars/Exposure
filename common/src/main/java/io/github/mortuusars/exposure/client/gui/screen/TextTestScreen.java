package io.github.mortuusars.exposure.client.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.client.gui.screen.element.TextBlock;
import io.github.mortuusars.exposure.client.gui.screen.element.TextBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TextTestScreen extends Screen {

    private MultiLineEditBox textBox;

    public TextTestScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
//        textBox = new TextBox(Minecraft.getInstance().font, 50, 50, 114, 27)
//                .setFontColor(0xFFb59774);
//
//        textBox.setText("initial");
//        textBox.setCursorToEnd();
//        textBox.selectionColor = 0xFF8888FF;
//        textBox.selectionUnfocusedColor = 0xFFBBBBFF;
//        textBox.fontUnfocusedColor = 0xFFb59774;
//
//        addRenderableWidget(textBox);

        Button button1 = Button.builder(Component.literal("Button"), button -> {
                })
                .pos(this.width / 2 - 20 / 2, 100)
                .size(40, 30)
                .build();

        addRenderableWidget(button1);

        MultiLineEditBox note = new MultiLineEditBox(font, 50, 50, 114, 27, Component.literal("Note"), Component.empty());
        addRenderableWidget(note);
        textBox = note;

        TextBlock textBlock = new TextBlock(font, 50, 50, 114, 27, Component.literal("Testing a looooooooooooooooooooong tooltip. textBox.setText(\"initial\");\n" +
                "//        textBox.setCursorToEnd();\n" +
                "//        textBox.selectionColor = 0xFF8888FF;\n" +
                "//        textBox.selectionUnfocusedColor = 0xFFBBBBFF;\n" +
                "//        textBox.fontUnfocusedColor = 0xFFb59774;\n" +
                "//\n" +
                "//        addRenderableWidget(textBox);"));

        textBlock.fontColor = 0xFFb59774;

        addRenderableWidget(textBlock);
    }

    @Override
    public void tick() {
//        textBox.tick();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        textBox.setX(this.width / 2 - textBox.getWidth() / 2);

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
