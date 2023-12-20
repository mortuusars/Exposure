package io.github.mortuusars.exposure.forge.mixin;

import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.item.forge.CameraItemForgeClientExtensions;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.extensions.IForgeItem;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Consumer;

@Mixin(value = CameraItem.class, remap = false)
public abstract class CameraItemForgeMixin extends Item implements IForgeItem {
    public CameraItemForgeMixin(Properties properties) {
        super(properties);
    }

    @Shadow abstract InteractionResult useCamera(Player player, InteractionHand hand);

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null) {
            InteractionHand hand = context.getHand();
            if (hand == InteractionHand.MAIN_HAND && CameraInHand.getActiveHand(player) == InteractionHand.OFF_HAND)
                return InteractionResult.PASS;

            return useCamera(player, hand);
        }
        return InteractionResult.CONSUME; // To not play attack animation.
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.CUSTOM;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(CameraItemForgeClientExtensions.INSTANCE);
    }
}
