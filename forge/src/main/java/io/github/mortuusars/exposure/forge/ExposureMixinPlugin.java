package io.github.mortuusars.exposure.forge;

import com.google.common.collect.ImmutableMap;
import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ExposureMixinPlugin implements IMixinConfigPlugin {

    private static final Map<String, Supplier<Boolean>> CONDITIONS = ImmutableMap.of(
            "io.github.mortuusars.exposure.forge.mixin.create.CreateJEICompatMixin", ExposureMixinPlugin::isCorrectCreateVersion,
            "io.github.mortuusars.exposure.fabric.mixin.create.SpoutDevelopingMixin", ExposureMixinPlugin::isCorrectCreateVersion
    );

    private static boolean isCorrectCreateVersion() {
        return LoadingModList.get().getMods().stream().filter(i -> i.getModId().equals("create")).limit(1).findFirst()
                        .map(c -> c.getVersion().toString().startsWith("0.5.1.f"))
                        .orElse(false);
    }

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return CONDITIONS.getOrDefault(mixinClassName, () -> true).get();
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
