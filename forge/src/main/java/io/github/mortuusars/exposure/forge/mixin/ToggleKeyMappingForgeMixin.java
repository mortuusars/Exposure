package io.github.mortuusars.exposure.forge.mixin;

import io.github.mortuusars.exposure.client.gui.screen.camera.ViewfinderControlsScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ToggleKeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ToggleKeyMapping.class)
public abstract class ToggleKeyMappingForgeMixin extends KeyMapping {
    public ToggleKeyMappingForgeMixin(String pName, int pKeyCode, String pCategory) {
        super(pName, pKeyCode, pCategory);
    }

    /**
     * Allows moving when ControlsScreen is open.
     */
    @Inject(method = "isDown", at = @At(value = "HEAD"), cancellable = true)
    private void isDown(CallbackInfoReturnable<Boolean> cir) {
        if (Minecraft.getInstance().screen instanceof ViewfinderControlsScreen)
            cir.setReturnValue(this.isDown);
    }
}