package io.github.mortuusars.exposure.world.item;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.List;
import java.util.function.Consumer;

public class BrokenInterplanarProjectorItem extends Item {
    public BrokenInterplanarProjectorItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipComponents, TooltipFlag flag) {
        if (stack.has(DataComponents.CUSTOM_NAME)) {
            tooltipComponents.accept(Component.translatable("item.exposure.broken_interplanar_projector.tooltip.broken").withStyle(ChatFormatting.DARK_RED));
        }
    }

    public String getErrorCode(ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.INTERPLANAR_PROJECTOR_ERROR_CODE, "ERR_FAILURE_SEE_LOGS");
    }
}
