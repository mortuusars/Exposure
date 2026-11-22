package io.github.mortuusars.exposure.client.image.renderable;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.util.Util;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringUtil;

public record RenderableImageIdentifier(String base, String variant) {
    public RenderableImageIdentifier(String base) {
        this(base, "");
    }

    public RenderableImageIdentifier appendVariant(String appendedVariant) {
        if (StringUtil.isBlank(appendedVariant)) return this;
        if (StringUtil.isBlank(variant)) return new RenderableImageIdentifier(base, appendedVariant);
        else return new RenderableImageIdentifier(base, variant + "_" + appendedVariant);
    }

    public Identifier toIdentifier() {
        String path = StringUtil.isBlank(variant) ? base : base + "/" + variant;
        String validPath = Util.sanitizeName(path, Identifier::validPathChar);
        return Exposure.resource(validPath);
    }
}
