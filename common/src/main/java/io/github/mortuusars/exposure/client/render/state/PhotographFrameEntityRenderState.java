package io.github.mortuusars.exposure.client.render.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class PhotographFrameEntityRenderState extends EntityRenderState {
    public Direction direction;
    public ItemStack item;
    public int rotation;
    public int size;
    public int photographBrightness;
    public boolean isGlowing;
    @Nullable
    public
    ItemModel itemModel;

    public PhotographFrameEntityRenderState() {
        this.direction = Direction.NORTH;
        this.item = ItemStack.EMPTY;
    }
}
