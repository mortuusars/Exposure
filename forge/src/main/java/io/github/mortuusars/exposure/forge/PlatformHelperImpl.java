package io.github.mortuusars.exposure.forge;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;
import java.util.function.Consumer;

public class PlatformHelperImpl {
    public static boolean canShear(ItemStack stack) {
        return stack.canPerformAction(ToolActions.SHEARS_CARVE);
    }

    public static void openMenu(ServerPlayer serverPlayer, MenuProvider menuProvider, Consumer<FriendlyByteBuf> extraDataWriter) {
        NetworkHooks.openScreen(serverPlayer, menuProvider, extraDataWriter);
    }

    public static List<String> getDefaultSpoutDevelopmentColorSequence() {
        return List.of(
                "{FluidName:\"create:potion\",Amount:250,Tag:{Potion:\"minecraft:awkward\"}}",
                "{FluidName:\"create:potion\",Amount:250,Tag:{Potion:\"minecraft:thick\"}}",
                "{FluidName:\"create:potion\",Amount:250,Tag:{Potion:\"minecraft:mundane\"}}");
    }

    public static List<String> getDefaultSpoutDevelopmentBWSequence() {
        return List.of(
                "{FluidName:\"minecraft:water\",Amount:250}");
    }

    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}
