package io.github.mortuusars.exposure.client.capture.action;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.block.FlashBlock;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FlashAction implements CaptureAction {
    protected final Entity cameraHolder;
    protected long initializedAt;

    public FlashAction(Entity cameraHolder) {
        this.cameraHolder = cameraHolder;
    }

    @Override
    public int requiredDelayTicks() {
        // Adding the delay is the primary task of this action.
        // Without the delay, light from the flash block might not apply in time.
        return Config.Client.FLASH_CAPTURE_DELAY_TICKS.get();
    }

    @Override
    public void initialize() {
        initializedAt = cameraHolder.level().getGameTime();
    }

    @Override
    public void beforeCapture() {
        long ticksPassed = cameraHolder.level().getGameTime() - initializedAt;
        if (ticksPassed > FlashBlock.LIFETIME_TICKS) {
            Exposure.LOGGER.warn("Capturing with delay of '{}' ticks can be too long for a flash to have an effect. " +
                    "The flash might disappear in that time. Try lowering the flash delay in config if flash is not applied.", ticksPassed);
        }
    }

    @Override
    public void afterCapture() {
        // Secondary task of this action is to spawn particles for the capturing player.
        // This is done after the image is captured to not make them visible on the photo.
        // Particles for other players are spawned immediately by the item logic.

        Level level = cameraHolder.level();
        Vec3 pos = cameraHolder.position();
        Vec3 lookAngle = cameraHolder.getLookAngle();
        pos = pos.add(0, 1.1, 0).add(lookAngle.scale(0.8));

        RandomSource r = level.getRandom();
        for (int i = 0; i < 3; i++) {
            level.addParticle(ParticleTypes.END_ROD,
                    pos.x + r.nextFloat() - 0.5f,
                    pos.y + r.nextFloat() + 0.15f,
                    pos.z + r.nextFloat() - 0.5f,
                    lookAngle.x * 0.025f + r.nextFloat() * 0.025f,
                    lookAngle.y * 0.025f + r.nextFloat() * 0.025f,
                    lookAngle.z * 0.025f + r.nextFloat() * 0.025f);
        }

        // FLASH is omitted from the player held camera
        if (cameraHolder instanceof CameraStandEntity) {
            level.addParticle(ParticleTypes.FLASH, pos.x, pos.y, pos.z, 0, 0, 0);
        }
    }
}
