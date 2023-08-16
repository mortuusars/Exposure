package io.github.mortuusars.exposure.entity;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Either;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.item.PhotographItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ToolActions;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class PhotographEntity extends HangingEntity {
    protected static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(PhotographEntity.class, EntityDataSerializers.ITEM_STACK);
    protected static final EntityDataAccessor<Integer> DATA_ROTATION = SynchedEntityData.defineId(PhotographEntity.class, EntityDataSerializers.INT);

    @Nullable
    private Either<String, ResourceLocation> idOrTexture;

    public PhotographEntity(EntityType<? extends PhotographEntity> entityType, Level level) {
        super(entityType, level);
    }

    public PhotographEntity(Level level, BlockPos pos, Direction facingDirection, ItemStack photographStack) {
        super(Exposure.EntityTypes.PHOTOGRAPH.get(), level, pos);
        setDirection(facingDirection);
        setItem(photographStack);
    }


    // Entity:

    protected void defineSynchedData() {
        this.getEntityData().define(DATA_ITEM, ItemStack.EMPTY);
        this.getEntityData().define(DATA_ROTATION, 0);
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (key.equals(DATA_ITEM)) {
            this.onItemChanged(this.getItem());
        }
    }

    @Override
    public void recreateFromPacket(@NotNull ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        this.setDirection(Direction.from3DDataValue(packet.getData()));
    }

    @Override
    public @NotNull Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this, this.direction.get3DDataValue(), this.getPos());
    }

    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (!this.getItem().isEmpty()) {
            tag.put("Item", this.getItem().save(new CompoundTag()));
            tag.putByte("ItemRotation", (byte)this.getRotation());
        }

        tag.putByte("Facing", (byte)this.direction.get3DDataValue());
        tag.putBoolean("Invisible", this.isInvisible());
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        CompoundTag compoundtag = tag.getCompound("Item");
        if (!compoundtag.isEmpty()) {
            ItemStack itemstack = ItemStack.of(compoundtag);
            if (itemstack.isEmpty())
                Exposure.LOGGER.warn("Unable to load item from: {}", compoundtag);

            setItem(itemstack);
            setRotation(tag.getByte("ItemRotation"));
        }

        this.setDirection(Direction.from3DDataValue(tag.getByte("Facing")));
        this.setInvisible(tag.getBoolean("Invisible"));
    }


    // Properties:

    public @Nullable Either<String, ResourceLocation> getIdOrTexture() {
        return idOrTexture;
    }

    @Override
    protected float getEyeHeight(@NotNull Pose pose, @NotNull EntityDimensions dimensions) {
        return 0f;
    }

    @Override
    public int getWidth() {
        return 16;
    }

    @Override
    public int getHeight() {
        return 16;
    }

    @Nullable
    @Override
    public ItemStack getPickResult() {
        return getItem().copy();
    }

    @Override
    public boolean survives() {
        if (!this.level.noCollision(this)) {
            return false;
        } else {
            BlockState blockstate = this.level.getBlockState(this.pos.relative(this.direction.getOpposite()));
            return (blockstate.getMaterial().isSolid() || this.direction.getAxis().isHorizontal()
                    && DiodeBlock.isDiode(blockstate))
                    && this.level.getEntities(this, this.getBoundingBox(), HANGING_ENTITY).isEmpty();
        }
    }

    @Override
    protected void recalculateBoundingBox() {
        //noinspection ConstantValue
        if (this.direction == null) {
            // When called from HangingEntity constructor direction is null
            return;
        }

        double value = 0.46875D;
        double d1 = (double)this.pos.getX() + 0.5D - (double)this.direction.getStepX() * value;
        double d2 = (double)this.pos.getY() + 0.5D - (double)this.direction.getStepY() * value;
        double d3 = (double)this.pos.getZ() + 0.5D - (double)this.direction.getStepZ() * value;
        this.setPosRaw(d1, d2, d3);
        double d4 = this.getWidth();
        double d5 = this.getHeight();
        double d6 = this.getWidth();
        Direction.Axis directionAxis = this.direction.getAxis();
        switch (directionAxis) {
            case X -> d4 = 1.0D;
            case Y -> d5 = 1.0D;
            case Z -> d6 = 1.0D;
        }

        d4 /= 32.0D;
        d5 /= 32.0D;
        d6 /= 32.0D;
        this.setBoundingBox(new AABB(d1 - d4, d2 - d5, d3 - d6, d1 + d4, d2 + d5, d3 + d6));
    }


    // Interaction:

    @Override
    protected void setDirection(@NotNull Direction facingDirection) {
        Validate.notNull(facingDirection);
        this.direction = facingDirection;
        if (facingDirection.getAxis().isHorizontal()) {
            this.setXRot(0.0F);
            this.setYRot((float)(this.direction.get2DDataValue() * 90));
        } else {
            this.setXRot((float)(-90 * facingDirection.getAxisDirection().getStep()));
            this.setYRot(0.0F);
        }

        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
        this.recalculateBoundingBox();
    }

    public ItemStack getItem() {
        return this.getEntityData().get(DATA_ITEM);
    }

    public void setItem(ItemStack photographStack) {
        Preconditions.checkState(photographStack.getItem() instanceof PhotographItem,  photographStack + " is not a PhotographItem");
        this.getEntityData().set(DATA_ITEM, photographStack);
    }

    protected void onItemChanged(ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            itemStack.setEntityRepresentation(this);
            if (itemStack.getItem() instanceof PhotographItem photographItem) {
                idOrTexture = photographItem.getidOrTexture(itemStack);
            }
        }

        this.recalculateBoundingBox();
    }

    public int getRotation() {
        return this.getEntityData().get(DATA_ROTATION);
    }

    public void setRotation(int rotation) {
        this.getEntityData().set(DATA_ROTATION, rotation % 4);
    }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);
        if (!isInvisible() && itemInHand.canPerformAction(ToolActions.SHEARS_CARVE)) {
            if (!level.isClientSide) {
                setInvisible(true);
                itemInHand.hurtAndBreak(1, player, (pl) -> pl.broadcastBreakEvent(hand));
                playSound(SoundEvents.SHEEP_SHEAR, 1f, level.getRandom().nextFloat() * 0.2f + 0.9f);
            }

            return InteractionResult.SUCCESS;
        }

        if (!level.isClientSide) {
            this.playSound(getRotateSound(), 1.0F, level.getRandom().nextFloat() * 0.2f + 0.9f);
            this.setRotation(getRotation() + 1);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean hurt(@NotNull DamageSource damageSource, float amount) {
        if (this.isInvulnerableTo(damageSource))
            return false;

        if (!this.isRemoved() && !this.level.isClientSide && !damageSource.isExplosion()) {
            if (!getItem().isEmpty())
                this.dropItem(damageSource.getEntity());

            this.kill();
            this.markHurt();
        }

        return true;
    }

    @Override
    public void dropItem(@Nullable Entity breaker) {
        this.playSound(this.getBreakSound(), 1.0F, level.getRandom().nextFloat() * 0.3f + 0.6f);

        if ((breaker instanceof Player player && player.isCreative()))
            return;

        ItemStack itemStack = getItem();
        spawnAtLocation(itemStack);
    }

    @Override
    public void playPlacementSound() {
        this.playSound(this.getPlaceSound(), 1.0F, level.getRandom().nextFloat() * 0.3f + 0.9f);
    }

    public SoundEvent getPlaceSound() {
        return Exposure.SoundEvents.PHOTOGRAPH_PLACE.get();
    }

    public SoundEvent getBreakSound() {
        return Exposure.SoundEvents.PHOTOGRAPH_BREAK.get();
    }

    public SoundEvent getRotateSound() {
        return Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get();
    }
}
