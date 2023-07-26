package io.github.mortuusars.exposure.data.generation.provider;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinition;
import net.minecraftforge.common.data.SoundDefinitionsProvider;

public class SoundsProvider extends SoundDefinitionsProvider {
    public SoundsProvider(DataGenerator generator, ExistingFileHelper helper) {
        super(generator, Exposure.ID, helper);
    }

    @Override
    public void registerSounds() {
        add(Exposure.SoundEvents.VIEWFINDER_OPEN.get(), definition()
                .subtitle("subtitle.exposure.camera.viewfinder_open")
                .with(sound(Exposure.ID + ":item/camera/viewfinder_open")));

        add(Exposure.SoundEvents.VIEWFINDER_CLOSE.get(), definition()
                .subtitle("subtitle.exposure.camera.viewfinder_close")
                .with(sound(Exposure.ID + ":item/camera/viewfinder_close")));

        add(Exposure.SoundEvents.SHUTTER_OPEN.get(), definition()
                .subtitle("subtitle.exposure.camera.shutter")
                .with(multiple(2, Exposure.ID + ":item/camera/shutter_open", 1f, 1)));

        add(Exposure.SoundEvents.SHUTTER_CLOSE.get(), definition()
                .subtitle("subtitle.exposure.camera.shutter")
                .with(multiple(2, Exposure.ID + ":item/camera/shutter_close", 1f, 1)));

        add(Exposure.SoundEvents.SHUTTER_TICKING.get(), definition()
                .with(sound(Exposure.ID + ":item/camera/shutter_ticking")));

        add(Exposure.SoundEvents.FILM_ADVANCE.get(), definition()
                .subtitle("subtitle.exposure.camera.film_advance")
                .with(multiple(2, Exposure.ID + ":item/camera/film_advance", 1f, 1)));

        add(Exposure.SoundEvents.CAMERA_BUTTON_CLICK.get(), definition()
                .subtitle("subtitle.exposure.camera.button_click")
                .with(sound(Exposure.ID + ":item/camera/button_click")));

        add(Exposure.SoundEvents.LENS_RING_CLICK.get(), definition()
                .with(multiple(4, Exposure.ID + ":item/camera/lens_ring", 1f, 1)));
    }

    private SoundDefinition.Sound[] multiple(int count, String name, float volume, float pitch) {
        SoundDefinition.Sound[] sounds = new SoundDefinition.Sound[count];
        for (int i = 0; i < count; i++) {
            sounds[i] = sound(name + (i + 1)).volume(volume).pitch(pitch);
        }
        return sounds;
    }
}
