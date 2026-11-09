package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.camera.CameraOnStand;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.entity.CameraOperator;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Player.class)
@SuppressWarnings("AddedMixinMembersNamePattern")
public abstract class PlayerMixin extends LivingEntity implements CameraHolder, CameraOperator {
    @Shadow
    public abstract Inventory getInventory();

    @Unique
    @Nullable
    protected Camera activeExposureCamera;

    @Unique
    protected float oExposureCameraActionAnim = 0F;
    @Unique
    protected float exposureCameraActionAnim = 0F;

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    // --

    @Override
    public Optional<Player> getPlayerExecutingExposure() {
        return Optional.of((Player) (Object) this);
    }

    @Override
    public Optional<Player> getPlayerAwardedForExposure() {
        return Optional.of((Player) (Object) this);
    }

    @Override
    public @NotNull Entity getExposureAuthorEntity() {
        return this;
    }

    @Override
    public Optional<CameraOperator> getExposureCameraOperator() {
        return Optional.of(this);
    }

    // --

    @Override
    public @Nullable Camera getActiveExposureCamera() {
        if (activeExposureCamera != null && !activeExposureCamera.isActive()) {
            return null;
        }
        return activeExposureCamera;
    }

    @Override
    public void setActiveExposureCamera(@Nullable Camera camera) {
        activeExposureCamera = camera;
    }

    @Override
    public void removeActiveExposureCamera() {
        activeExposureCamera = null;
    }

    @Override
    public float getExposureCameraActionAnim(float partialTick) {
        float delta = exposureCameraActionAnim - oExposureCameraActionAnim;
        if (delta < 0.0F) {
            delta++;
        }

        return oExposureCameraActionAnim + delta * partialTick;
    }

    // --

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (!level().isClientSide() && activeExposureCamera != null && !activeExposureCamera.isActive()) {
            activeExposureCamera.deactivate(((Player)(Object)this));
            removeActiveExposureCamera();
        }

        oExposureCameraActionAnim = exposureCameraActionAnim;

        long lastActionTime = -1L;

        if (activeExposureCamera != null) {
            lastActionTime = activeExposureCamera.map(CameraItem::getLastActionTime).orElse(-1L);
        } else if (getMainHandItem().getItem() instanceof CameraItem item) {
            lastActionTime = item.getLastActionTime(getMainHandItem());
        } else if (getOffhandItem().getItem() instanceof CameraItem item) {
            lastActionTime = item.getLastActionTime(getOffhandItem());
        }

        int actionTime = (int) (level().getGameTime() - lastActionTime);
        exposureCameraActionAnim = Mth.clamp(actionTime / 10F, 0F, 1F);
    }

    @Inject(method = "blockActionRestricted", at = @At("HEAD"), cancellable = true)
    private void onBlockActionRestricted(Level level, BlockPos pos, GameType gameMode, CallbackInfoReturnable<Boolean> cir) {
        if (getActiveExposureCamera() instanceof CameraOnStand) {
            cir.setReturnValue(true);
        }
    }
}
