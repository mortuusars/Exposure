package io.github.mortuusars.exposure.camera.component;

import com.google.common.collect.ImmutableList;
import io.github.mortuusars.exposure.Exposure;

import java.util.ArrayList;
import java.util.List;

public class CompositionGuides {
    private static final List<CompositionGuide> GUIDES = new ArrayList<>();

    public static final CompositionGuide NONE = register(new CompositionGuide("none",
            Exposure.resource("textures/misc/composition_guide/none.png")));
    public static final CompositionGuide CROSS = register(new CompositionGuide("cross",
            Exposure.resource("textures/misc/composition_guide/cross.png")));
    public static final CompositionGuide RULE_OF_THIRDS = register(new CompositionGuide("rule_of_thirds",
            Exposure.resource("textures/misc/composition_guide/rule_of_thirds.png")));

    public static List<CompositionGuide> getGuides() {
        return ImmutableList.copyOf(GUIDES);
    }

    public static CompositionGuide byIdOrNone(String id) {
        for (CompositionGuide guide : GUIDES) {
            if (guide.getId().equals(id))
                return guide;
        }

        return NONE;
    }

    public static CompositionGuide register(CompositionGuide guide) {
        GUIDES.add(guide);
        return guide;
    }
}
