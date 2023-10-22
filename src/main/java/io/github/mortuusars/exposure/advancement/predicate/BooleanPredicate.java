package io.github.mortuusars.exposure.advancement.predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

public class BooleanPredicate {
    public static final BooleanPredicate ANY = new BooleanPredicate(null);

    @Nullable
    private final Boolean value;

    public BooleanPredicate(@Nullable Boolean value) {
        this.value = value;
    }

    public static BooleanPredicate mustBeTrue() {
        return new BooleanPredicate(true);
    }

    public static BooleanPredicate mustBeFalse() {
        return new BooleanPredicate(false);
    }

    public boolean matches(boolean value) {
        return this.value == null || value == this.value;
    }

    public static BooleanPredicate fromJson(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull())
            return ANY;

        JsonObject jsonobject = GsonHelper.convertToJsonObject(json, "boolean");
        String str = jsonobject.get("value").getAsString();

        return str.equals("any") ? ANY : new BooleanPredicate(Boolean.parseBoolean(str));
    }

    public JsonElement serializeToJson() {
        if (this == ANY || this.value == null)
            return JsonNull.INSTANCE;

        JsonObject jsonobject = new JsonObject();
        jsonobject.addProperty("value", this.value.toString());
        return jsonobject;
    }
}
