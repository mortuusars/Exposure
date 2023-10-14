package io.github.mortuusars.exposure.item;

import io.github.mortuusars.exposure.camera.film.FilmType;
import io.github.mortuusars.exposure.client.gui.ClientGUI;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DevelopedFilmItem extends Item implements IFilmItem {
    private final FilmType type;

    public DevelopedFilmItem(FilmType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    @Override
    public FilmType getType() {
        return type;
    }

//    @Override
//    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
//        ItemStack film = player.getItemInHand(hand);
//
//        if (level.isClientSide && getExposedFramesCount(film) > 0)
//            ClientGUI.showExposureViewScreen(film);
//
//        return InteractionResultHolder.success(film);
//    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        int exposedFrames = getExposedFramesCount(stack);
        if (exposedFrames > 0) {
            tooltipComponents.add(Component.translatable("item.exposure.developed_film.tooltip.frame_count", exposedFrames)
                    .withStyle(ChatFormatting.GRAY));
        }
    }

//    @Override
//    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
//        if (level.isClientSide)
//            ClientGUI.openDevelopedFilmScreen(new ItemAndStack<>(player.getItemInHand(usedHand)));
//
//        return InteractionResultHolder.success(player.getItemInHand(usedHand));
//    }
}
