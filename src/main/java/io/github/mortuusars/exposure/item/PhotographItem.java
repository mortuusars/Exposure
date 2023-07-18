package io.github.mortuusars.exposure.item;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.Photograph;
import io.github.mortuusars.exposure.client.ClientGUI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PhotographItem extends Item {
    public PhotographItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);
        Photograph photographData = getPhotographData(itemInHand);
        if (photographData != Photograph.EMPTY) {
            if (level.isClientSide)
                ClientGUI.showPhotographScreen(photographData);

            return InteractionResultHolder.success(itemInHand);
        }

        return super.use(level, player, hand);
    }

    public Photograph getPhotographData(ItemStack stack) {

        if (!stack.hasTag()) {
            Exposure.LOGGER.error(stack + " doesn't have a tag.");
            return Photograph.EMPTY;
        }

        try {
            return Photograph.load(stack.getOrCreateTag());
        }
        catch (Exception e) {
            Exposure.LOGGER.error("Cannot get photograph data: " + e);
        }

        return Photograph.EMPTY;
    }

    public ItemStack setPhotographData(ItemStack photographStack, Photograph photograph) {
        CompoundTag tag = photographStack.getOrCreateTag();
        photograph.save(tag);
        return photographStack;
    }
}
