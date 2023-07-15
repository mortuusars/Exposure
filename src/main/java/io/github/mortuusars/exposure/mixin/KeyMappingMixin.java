package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.client.screen.ViewfinderControlsScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyMapping.class)
public class KeyMappingMixin {
    @Shadow public boolean isDown;

    /**
     * Allows moving when ControlsScreen is open.
     */
    @Inject(method = "isDown", at = @At(value = "HEAD"), cancellable = true)
    private void isDown(CallbackInfoReturnable<Boolean> cir) {
        if (Minecraft.getInstance().screen instanceof ViewfinderControlsScreen)
            cir.setReturnValue(this.isDown);
    }
}
