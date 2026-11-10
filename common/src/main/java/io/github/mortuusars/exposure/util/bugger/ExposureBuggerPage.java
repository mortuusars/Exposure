package io.github.mortuusars.exposure.util.bugger;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.client.gui.screen.test.TestImageScreen;
import io.github.mortuusars.mortaar.bugger.screen.page.BuggerScreenPage;
import io.github.mortuusars.mortaar.client.Minecrft;

public class ExposureBuggerPage implements BuggerScreenPage {
    @Override
    public String getTitle() {
        return "Exposure";
    }

    @Override
    public boolean onKeyPress(int key, int scanCode, int modifiers) {
        if (key == InputConstants.KEY_END) {
            Minecrft.get().setScreen(new TestImageScreen());
            return true;
        }
        return BuggerScreenPage.super.onKeyPress(key, scanCode, modifiers);
    }
}
