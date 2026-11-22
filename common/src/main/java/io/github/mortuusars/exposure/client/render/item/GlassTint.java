package io.github.mortuusars.exposure.client.render.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.util.color.Color;
import io.github.mortuusars.exposure.world.item.camera.Attachment;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import io.github.mortuusars.exposure.world.item.component.StoredItemStack;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import org.jetbrains.annotations.Nullable;

public record GlassTint(int defaultColor) implements ItemTintSource {
    public static final MapCodec<GlassTint> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default").forGetter(GlassTint::defaultColor)).apply(instance, GlassTint::new));

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        boolean shutterOpen = stack.getItem() instanceof CameraItem cameraItem && cameraItem.getShutter().isOpen(stack);

        StoredItemStack filter = Attachment.FILTER.get(stack);
        if (filter.isEmpty()) return shutterOpen ? 0xFF333333 : ARGB.opaque(defaultColor);
        if (filter.getForReading().getItem() instanceof BlockItem item && item.getBlock() instanceof StainedGlassPaneBlock pane) {
            return shutterOpen
                    ? Color.argb(pane.getColor().getTextureDiffuseColor()).multiply(0.2f).withAlpha(255).getARGB()
                    : pane.getColor().getTextureDiffuseColor();
        }
        if (filter.getForReading().is(Exposure.Items.INTERPLANAR_PROJECTOR.get()))
            return shutterOpen ? 0xFF051A0F : 0xFF50B27E;
        if (filter.getForReading().is(Exposure.Items.BROKEN_INTERPLANAR_PROJECTOR.get()))
            return shutterOpen ? 0xFF003D76 : 0xFF54ADFF;
        return ARGB.opaque(defaultColor);
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return MAP_CODEC;
    }
}
