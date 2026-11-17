package io.github.mortuusars.exposure.mixin.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = KeyBindsList.KeyEntry.class)
public abstract class KeyBindTooltipMixin {
    @Shadow
    @Final
    private Button changeButton;
    @Shadow
    @Final
    private Component name;

    @Inject(method = "refreshEntry", at = @At(value = "INVOKE", ordinal = 1, shift = At.Shift.AFTER,
            target = "Lnet/minecraft/client/gui/components/Button;setTooltip(Lnet/minecraft/client/gui/components/Tooltip;)V"))
    private void changeCameraControlsKeyHover(CallbackInfo ci) {
        if (name.equals(Component.translatable("key.exposure.camera_controls"))) {
            this.changeButton.setTooltip(Tooltip.create(Component.translatable("key.exposure.camera_controls.tooltip")));
        }
    }
}