package io.github.mortuusars.exposure.item;

import io.github.mortuusars.exposure.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SignedAlbumItem extends AlbumItem {
    public SignedAlbumItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public @NotNull Component getName(ItemStack stack) {
        if (stack.getTag() != null && !StringUtil.isNullOrEmpty(stack.getTag().getString(TAG_TITLE))) {
            return Component.literal(stack.getTag().getString(TAG_TITLE));
        }
        return super.getName(stack);
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        if (stack.getTag() != null) {
            CompoundTag compoundTag = stack.getTag();
            String author = compoundTag.getString(TAG_AUTHOR);
            if (!StringUtil.isNullOrEmpty(author)) {
                tooltipComponents.add(Component.translatable("gui.exposure.album.by_author", author).withStyle(ChatFormatting.GRAY));
            }
        }
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return Config.Client.SIGNED_ALBUM_GLINT.get();
    }
}
