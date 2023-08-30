package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.client.renderer.ViewfinderRenderer;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MouseHandler.class)
public class ViewfinderSensitivityMixin {
    @ModifyVariable(method = "turnPlayer", at = @At(value = "STORE"), ordinal = 3)
    private double modifySensitivity(double sensitivity) {
        return ViewfinderRenderer.modifyMouseSensitivity(sensitivity);
    }
}
