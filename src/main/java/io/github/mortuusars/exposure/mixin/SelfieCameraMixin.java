package io.github.mortuusars.exposure.mixin;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class SelfieCameraMixin {
    @Inject(method = "getMaxZoom", at = @At(value = "RETURN"), cancellable = true)
    private void getMaxZoom(double pStartingDistance, CallbackInfoReturnable<Double> cir) {
//        cir.setReturnValue(Math.min(2.0, cir.getReturnValue()));
    }
}
