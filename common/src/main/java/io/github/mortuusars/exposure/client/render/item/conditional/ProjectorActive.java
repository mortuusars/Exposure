package io.github.mortuusars.exposure.client.render.item.conditional;

import com.mojang.serialization.MapCodec;
import io.github.mortuusars.exposure.Config;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record ProjectorActive() implements ConditionalItemModelProperty {
    public static final MapCodec<ProjectorActive> MAP_CODEC = MapCodec.unit(new ProjectorActive());

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext displayContext) {
        return Config.Server.CAN_PROJECT.get() && stack.has(DataComponents.CUSTOM_NAME);
    }

    @Override
    public MapCodec<? extends ConditionalItemModelProperty> type() {
        return MAP_CODEC;
    }
}
