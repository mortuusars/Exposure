package io.github.mortuusars.exposure.mixin.stand_spyglass;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.mortuusars.exposure.client.input.Mouse;
import io.github.mortuusars.exposure.world.entity.stand.CameraStand;
import io.github.mortuusars.mortaar.client.Minecrft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyReturnValue(method = "isScoping", at = @At("RETURN"))
    private boolean isScoping(boolean original) {
        if (original) {
            return true;
        }

        return level().isClientSide()
              && Minecrft.options().keyUse.isDown()
              && Mouse.getControlledStand() instanceof CameraStand stand
              && stand.getCamera().is(Items.SPYGLASS);
    }
}
