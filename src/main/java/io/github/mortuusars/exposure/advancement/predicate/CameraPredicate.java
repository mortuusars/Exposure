package io.github.mortuusars.exposure.advancement.predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import io.github.mortuusars.exposure.camera.film.FilmType;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.item.FilmRollItem;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CameraPredicate {
    public static final CameraPredicate ANY = new CameraPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY,
            null, null, null);

    private final MinMaxBounds.Doubles shutterSpeedMilliseconds;
    private final MinMaxBounds.Doubles focalLength;
    @Nullable
    private final FilmType filmType;
    @Nullable
    private final Boolean flash;
    @Nullable
    private final Boolean frameExposed;
//    private final BooleanPredicate flashPredicate;
//    private final BooleanPredicate frameExposedPredicate;

    public CameraPredicate(MinMaxBounds.Doubles shutterSpeedMilliseconds, MinMaxBounds.Doubles focalLength,
                           @Nullable FilmType filmType, @Nullable Boolean flashFired, @Nullable Boolean frameExposed) {
        this.shutterSpeedMilliseconds = shutterSpeedMilliseconds;
        this.focalLength = focalLength;
        this.filmType = filmType;
        this.flash = flashFired;
        this.frameExposed = frameExposed;
    }

    public static CameraPredicate exposesFilm() {
        return new CameraPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, null, null, true);
    }

    public boolean matches(ItemAndStack<CameraItem> camera, boolean flashFired, boolean frameExposed) {
        if (flash != null && flash != flashFired)
            return false;

        if (this.frameExposed != null && this.frameExposed != frameExposed)
            return false;

        if (filmType != null) {
            Optional<ItemAndStack<FilmRollItem>> film = camera.getItem().getFilm(camera.getStack());
            if (film.isEmpty() || film.get().getItem().getType() != filmType)
                return false;
        }

        if (!shutterSpeedMilliseconds.matches(camera.getItem().getShutterSpeed(camera.getStack()).getMilliseconds()))
            return false;

        if (!focalLength.matches(camera.getItem().getFocalLength(camera.getStack())))
            return false;

        return true;
    }

    public static CameraPredicate fromJson(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull())
            return ANY;

        JsonObject jsonobject = GsonHelper.convertToJsonObject(json, "camera");
        MinMaxBounds.Doubles shutterSpeedMsPredicate = MinMaxBounds.Doubles.fromJson(jsonobject.get("shutter_speed_ms"));
        MinMaxBounds.Doubles focalLengthPredicate = MinMaxBounds.Doubles.fromJson(jsonobject.get("focal_length"));
        @Nullable FilmType filmType = jsonobject.has("film_type") ? FilmType.byName(jsonobject.get("film_type").getAsString()) : null;
        @Nullable Boolean flash = jsonobject.has("flash") ? jsonobject.get("flash").getAsBoolean() : null;
        @Nullable Boolean frameExposed = jsonobject.has("frame_exposed") ? jsonobject.get("frame_exposed").getAsBoolean() : null;
//        BooleanPredicate flash = BooleanPredicate.fromJson(jsonobject.get("flash"));
//        BooleanPredicate frameExposed = BooleanPredicate.fromJson(jsonobject.get("frame_exposed"));

        return new CameraPredicate(shutterSpeedMsPredicate, focalLengthPredicate, filmType, flash, frameExposed);
    }

    public JsonElement serializeToJson() {
        if (this == ANY)
            return JsonNull.INSTANCE;

        JsonObject jsonobject = new JsonObject();
        jsonobject.add("shutter_speed_ms", this.shutterSpeedMilliseconds.serializeToJson());
        jsonobject.add("focal_length", this.focalLength.serializeToJson());

        if (filmType != null)
            jsonobject.addProperty("film_type", filmType.getSerializedName());

        if (flash != null)
            jsonobject.addProperty("flash", flash);

        if (frameExposed != null)
            jsonobject.addProperty("frame_exposed", frameExposed);
//        jsonobject.add("flash", this.flashPredicate.serializeToJson());
//        jsonobject.add("frame_exposed", this.frameExposedPredicate.serializeToJson());
        return jsonobject;
    }
}
