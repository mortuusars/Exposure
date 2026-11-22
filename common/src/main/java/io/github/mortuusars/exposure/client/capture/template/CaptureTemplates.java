package io.github.mortuusars.exposure.client.capture.template;

import com.google.common.base.Preconditions;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CaptureTemplates {
    private static final Map<Identifier, CaptureTemplate> TEMPLATES = new HashMap<>();

    public static void register(Identifier id, CaptureTemplate template) {
        Preconditions.checkState(!TEMPLATES.containsKey(id), "Template with id '%s' is already registered.", id);
        TEMPLATES.put(id, template);
    }

    public static @Nullable CaptureTemplate get(Identifier id) {
        return TEMPLATES.get(id);
    }

    public static CaptureTemplate getOrThrow(Identifier id) {
        @Nullable CaptureTemplate template = TEMPLATES.get(id);
        Preconditions.checkNotNull(template, "No template for id '%s' is registered.", id);
        return template;
    }
}
