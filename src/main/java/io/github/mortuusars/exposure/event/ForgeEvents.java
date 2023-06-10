package io.github.mortuusars.exposure.event;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.ImageCapture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = Exposure.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {
    public static final class PL {
        public BlockPos lightPos;
        public BlockState storedState;
        public int ticks;

        public PL(BlockPos lightPos, BlockState storedState, int ticks) {
            this.lightPos = lightPos;
            this.storedState = storedState;
            this.ticks = ticks;
        }
    }

    private static final Map<Player, PL> MAP = new HashMap<>();

    @SubscribeEvent
    public static void tick(TickEvent.LevelTickEvent event) {
//        if (event.side != LogicalSide.SERVER)
//            return;

        List<Player> toRemove = new ArrayList<>();

        for (Map.Entry<Player, PL> pl : MAP.entrySet()) {
            PL val = pl.getValue();
            val.ticks--;

            if (val.ticks < 1)
                toRemove.add(pl.getKey());
        }

        for (Player player : toRemove) {
            end(player, MAP.get(player));
            MAP.remove(player);
        }
    }

    public static void start(Player player) {
//        BlockPos pos = player.blockPosition().above();
//        BlockState blockState = player.level.getBlockState(pos);
//        MAP.put(player, new PL(pos, blockState, 20));
//
//        if (blockState.isAir() || player.level.getFluidState(pos).is(Fluids.WATER)) {
//            player.level.setBlockAndUpdate(pos, Blocks.LIGHT.defaultBlockState());
//        }
    }

    public static void end(Player player, PL pl) {
//        if (player.level.isClientSide) {
//            ImageCapture.capture("photo_0");
//        }
//
//        if (player.level.getBlockState(pl.lightPos).is(Blocks.LIGHT))
//            player.level.setBlockAndUpdate(pl.lightPos, pl.storedState);
    }
}
