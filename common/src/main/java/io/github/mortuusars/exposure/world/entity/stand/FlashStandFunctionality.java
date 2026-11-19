package io.github.mortuusars.exposure.world.entity.stand;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.block.FlashBlock;
import io.github.mortuusars.exposure.world.item.camera.Flash;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class FlashStandFunctionality {
//    protected boolean detecting = true;
//    protected int litTicks;
//
//    public FlashStandFunctionality(CameraStand stand) {
//        super(stand);
//    }
//
//    public boolean isDetecting() {
//        return !stand.isOnCooldown() && !isLit() && detecting;
//    }
//
//    public boolean isLit() {
//        return litTicks > 0;
//    }
//
//    // --
//
//    @Override
//    public void tick() {
//        if (litTicks > 0) {
//            litTicks--;
//            setChanged();
//        }
//    }
//
//    @Override
//    public InteractionResult handleSneakingInteraction(Player player, InteractionHand hand) {
//        if (!stand.isClientSide()) {
//            detecting = !detecting;
//            setChanged();
//        }
//        player.level().playSound(player, stand, Exposure.SoundEvents.CAMERA_GENERIC_CLICK.get(), SoundSource.PLAYERS, 1, 1);
//        return InteractionResult.SUCCESS;
//    }
//
//    @Override
//    public boolean handleActivation() {
//        if (stand.isOnCooldown()) {
//            return false;
//        }
//
//        if (stand.level() instanceof ServerLevel level) {
//            Flash flash = new Flash();
//            if (flash.fire(stand, level, stand.getCamera())) {
//                stand.startCooldown(flash.getCooldown());
//                litTicks = FlashBlock.LIFETIME_TICKS;
//                setChanged();
//            }
//        }
//
//        return true;
//    }
//
//    // --
//
//    @Override
//    public @NotNull CompoundTag save() {
//        CompoundTag tag = new CompoundTag();
//        if (detecting) tag.putBoolean("detecting", true);
//        if (litTicks >= 0) tag.putInt("lit_ticks", litTicks);
//        return tag;
//    }
//
//    @Override
//    public void load(CompoundTag tag) {
//        detecting = tag.getBoolean("detecting");
//        litTicks = tag.getInt("lit_ticks");
//    }
}
