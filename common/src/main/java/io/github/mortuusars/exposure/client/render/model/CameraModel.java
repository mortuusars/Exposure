package io.github.mortuusars.exposure.client.render.model;

import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.exposure.world.item.camera.Attachment;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import io.github.mortuusars.mortaar.Platform;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CameraModel {
    /**
     * Modifies camera model depending on its state.<br>
     * This was implemented through model overrides before, but it gets messy pretty quickly.
     * This way I have more control over the model, and it reduces number of model jsons significantly. And jsons themselves are simpler.<br>
     * Override predicates are still there, so resourcepacks should be able to override most of this anyway if they want.<br>
     * Maybe 1.21.4 override rework will make it easier?
     */
    public static BakedModel modifyCameraModel(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, BakedModel original) {
        if (!(stack.getItem() instanceof CameraItem camera)) return original;

        List<BakedModel> models = new ArrayList<>();

        if (camera.isInSelfieMode(stack)) {
            if (entity != null && entity.equals(Minecrft.get().getCameraEntity())) {
                return Platform.Client.getModel(ExposureClient.Models.SELFIE_STICK);
            }

            original = Platform.Client.getModelWithOverrides(ExposureClient.Models.CAMERA_SELFIE, stack, level, entity, seed);
            models.add(Platform.Client.getModelWithOverrides(ExposureClient.Models.CAMERA_SELFIE_STICK, stack, level, entity, seed));
        } else if (camera.isActive(stack)) {
            original = Platform.Client.getModelWithOverrides(ExposureClient.Models.CAMERA_ACTIVE, stack, level, entity, seed);
            models.add(Platform.Client.getModelWithOverrides(ExposureClient.Models.CAMERA_VIEWFINDER, stack, level, entity, seed));
        }

        if (Attachment.FLASH.isPresent(stack)) {
            models.add(Platform.Client.getModelWithOverrides(ExposureClient.Models.CAMERA_FLASH, stack, level, entity, seed));
        }

        if (Attachment.LENS.isPresent(stack)) {
            models.add(Platform.Client.getModelWithOverrides(ExposureClient.Models.CAMERA_LENS, stack, level, entity, seed));
        }

        if (!models.isEmpty()) {
            models.addFirst(original);
            return new CompositeModel(models);
        }

        return original;
    }
}
