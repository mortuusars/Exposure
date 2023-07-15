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

    public Photograph getPhotographData(ItemStack photographStack) {
        if (!photographStack.hasTag()) {
            Exposure.LOGGER.error(photographStack + " doesn't have a tag.");
            return Photograph.EMPTY;
        }

        return Photograph.load(photographStack.getOrCreateTag());
    }

    public ItemStack setPhotographData(ItemStack photographStack, Photograph photograph) {
        CompoundTag tag = photographStack.getOrCreateTag();
        photograph.save(tag);
        return photographStack;
    }
}
