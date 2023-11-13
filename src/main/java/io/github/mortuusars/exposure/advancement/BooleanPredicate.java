package io.github.mortuusars.exposure.advancement;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.Nullable;

public class BooleanPredicate {
    public static final BooleanPredicate ANY = new BooleanPredicate(null);
    public static final BooleanPredicate MUST_BE_TRUE = new BooleanPredicate(true);
    public static final BooleanPredicate MUST_BE_FALSE = new BooleanPredicate(false);

    @Nullable
    private final Boolean value;

    private BooleanPredicate(@Nullable Boolean value) {
        this.value = value;
    }

    public boolean matches(boolean value) {
        return this.value == null || this.value.equals(value);
    }

    public static BooleanPredicate fromJson(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull())
            return ANY;

        return new BooleanPredicate(json.getAsBoolean());
    }

    public JsonElement serializeToJson() {
        return this.value != null ? new JsonPrimitive(this.value) : JsonNull.INSTANCE;
    }
}
