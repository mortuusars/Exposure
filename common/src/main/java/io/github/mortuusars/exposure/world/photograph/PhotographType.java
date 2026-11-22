package io.github.mortuusars.exposure.world.photograph;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.resources.Identifier;

public record PhotographType(Identifier id) {
    public static final PhotographType REGULAR = new PhotographType(Exposure.resource("regular"));
    public static final PhotographType AGED = new PhotographType(Exposure.resource("aged"));

    public String getFileSuffix() {
        return this == REGULAR ? "" : id.getPath();
    }
}
