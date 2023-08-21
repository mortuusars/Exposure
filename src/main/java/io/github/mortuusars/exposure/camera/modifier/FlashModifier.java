package io.github.mortuusars.exposure.camera.modifier;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.camera.CaptureProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public record FlashModifier(String id) implements IExposureModifier {
    @Override
    public void afterScreenshotTaken(CaptureProperties currentCapture, NativeImage screenshot) {
        ClientLevel level = Minecraft.getInstance().level;
        LocalPlayer player = Minecraft.getInstance().player;
        if (level != null && player != null) {
            Vec3 pos = player.position();
            pos = pos.add(0, 1, 0).add(player.getLookAngle().multiply(0.5, 0, 0.5));

            level.addParticle(ParticleTypes.FLASH, pos.x, pos.y, pos.z, 0, 0, 0);
            RandomSource r = level.getRandom();
            level.addParticle(ParticleTypes.END_ROD, pos.x + r.nextFloat() * 0.2f, pos.y + r.nextFloat() * 0.2f,
                    pos.z + r.nextFloat() * 0.2f, 0, 0, 0);
        }
    }
}
