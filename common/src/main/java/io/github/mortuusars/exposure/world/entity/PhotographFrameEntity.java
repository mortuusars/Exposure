package io.github.mortuusars.exposure.world.entity;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.item.PhotographFrameItem;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class PhotographFrameEntity extends HangingEntity {
    public static final Logger LOGGER = Exposure.LOGGER;

    protected static final EntityDataAccessor<Integer> DATA_SIZE = SynchedEntityData.defineId(PhotographFrameEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<ItemStack> DATA_FRAME_ITEM = SynchedEntityData.defineId(PhotographFrameEntity.class, EntityDataSerializers.ITEM_STACK);
    protected static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(PhotographFrameEntity.class, EntityDataSerializers.ITEM_STACK);
    protected static final EntityDataAccessor<Integer> DATA_ITEM_ROTATION = SynchedEntityData.defineId(PhotographFrameEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Boolean> DATA_GLOWING = SynchedEntityData.defineId(PhotographFrameEntity.class, EntityDataSerializers.BOOLEAN);

    protected int size = 0;

    public PhotographFrameEntity(EntityType<? extends PhotographFrameEntity> entityType, Level level) {
        super(entityType, level);
    }

    public PhotographFrameEntity(Level level, BlockPos pos, Direction facingDirection) {
        this(Exposure.EntityTypes.PHOTOGRAPH_FRAME.get(), level, pos, facingDirection);
    }

    protected PhotographFrameEntity(EntityType<? extends PhotographFrameEntity> entityType, Level level, BlockPos pos, Direction facingDirection) {
        super(entityType, level, pos);
        setDirection(facingDirection);
        setItem(ItemStack.EMPTY);
    }

    @Override
    public @NotNull Component getDisplayName() {
        ItemStack item = getItem();
        return !item.isEmpty() ? item.getHoverName() : CommonComponents.EMPTY;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        // Return defaults when called on server. Some mods can do that.
        if (!level().isClientSide) {
            double d = 64 * getViewScale();
            return distance < d * d;
        }

        double d = Config.Client.PHOTOGRAPH_FRAME_CULLING_DISTANCE.get() * getViewScale();
        return distance < d * d;
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (key.equals(DATA_ITEM)) {
            onItemChanged(getItem());
        }
        if (key.equals(DATA_SIZE)) {
            size = getEntityData().get(DATA_SIZE);
            recalculateBoundingBox();
        }
    }

    @Override
    public void recreateFromPacket(@NotNull ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        int packedData = packet.getData();
        int size = (packedData >> 8) & 0xFF;
        int direction = packedData & 0xFF;
        setSize(size);
        setDirection(Direction.from3DDataValue(direction));
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
        int packedData = (size << 8) | direction.get3DDataValue();
        return new ClientboundAddEntityPacket(this, packedData, this.getPos());
    }

    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ItemStack item = getItem();
        if (!item.isEmpty()) {
            tag.put("Item", item.save(this.registryAccess()));
            tag.putBoolean("IsGlowing", this.isGlowing()); // "Glowing" is used in vanilla
            tag.putByte("ItemRotation", (byte) this.getItemRotation());
        }
        ItemStack frameItem = getFrameItem();
        if (!frameItem.isEmpty())
            tag.put("FrameItem", frameItem.save(this.registryAccess()));

        tag.putByte("Size", (byte) getSize());
        tag.putByte("Facing", (byte) direction.get3DDataValue());
        tag.putBoolean("Invisible", isInvisible());
    }

    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        CompoundTag frameItemTag = tag.getCompound("FrameItem");
        if (!frameItemTag.isEmpty()) {
            ItemStack stack = ItemStack.parse(registryAccess(), frameItemTag).orElse(new ItemStack(getBaseFrameItem()));
            setFrameItem(stack);
        }

        CompoundTag itemTag = tag.getCompound("Item");
        if (!itemTag.isEmpty()) {
            ItemStack itemstack = ItemStack.parse(registryAccess(), itemTag).orElse(ItemStack.EMPTY);
            setItem(itemstack);
            setGlowing(tag.getBoolean("IsGlowing")); // "Glowing" is used in vanilla
            setItemRotation(tag.getByte("ItemRotation"));
        }

        setSize(tag.getByte("Size"));
        setDirection(Direction.from3DDataValue(tag.getByte("Facing")));
        setInvisible(tag.getBoolean("Invisible"));
    }

    @Override
    public @NotNull Vec3 trackingPosition() {
        return Vec3.atLowerCornerOf(this.pos);
    }

    public int getWidth() {
        return getSize() * 16 + 16;
    }

    public int getHeight() {
        return getSize() * 16 + 16;
    }

    @Nullable
    @Override
    public ItemStack getPickResult() {
        ItemStack item = getItem();
        if (!item.isEmpty())
            return item.copy();

        return getFrameItem().copy();
    }

    @Override
    protected @NotNull AABB calculateBoundingBox(BlockPos pos, Direction direction) {
        double x = (double)pos.getX() + 0.5;
        double y = (double)pos.getY() + 0.5;
        double z = (double)pos.getZ() + 0.5;

        double widthOffset = getWidth() % 32 == 0 ? 0.5 : 0.0;
        double heightOffset = getHeight() % 32 == 0 ? 0.5 : 0.0;
        if (getSize() == 2) {
            widthOffset += 1;
            heightOffset += 1;
        }

        double hangOffset = 0.46875;

        if (direction.getAxis().isHorizontal()) {
            x -= getDirection().getStepX() * hangOffset;
            z -= getDirection().getStepZ() * hangOffset;
            Direction ccwDirection = direction.getCounterClockWise();
            setPosRaw(x += widthOffset * (double)ccwDirection.getStepX(), y += heightOffset, z += widthOffset * (double)ccwDirection.getStepZ());
            double xSize = this.getWidth();
            double ySize = this.getHeight();
            double zSize = this.getWidth();
            if (getDirection().getAxis() == Direction.Axis.Z)
                zSize = 1.0;
            else
                xSize = 1.0;
            return new AABB(x - (xSize /= 32.0), y - (ySize /= 32.0), z - (zSize /= 32.0), x + xSize, y + ySize, z + zSize);
        }
        else {
            y -= getDirection().getStepY() * hangOffset;
            setPosRaw(x += widthOffset, y, z -= heightOffset);
            double xSize = getWidth();
            double zSize = getHeight();
            return new AABB(x - (xSize /= 32.0), y - (1.0 / 32.0), z - (zSize /= 32.0), x + xSize, y + 1.0 / 32.0, z + zSize);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean survives() {
        if (!level().noCollision(this))
            return false;

        int sizeX = Math.max(1, getWidth() / 16);
        int sizeY = Math.max(1, getHeight() / 16);
        BlockPos baseBlockPos = pos.relative(direction.getOpposite());

        if (getDirection().getAxis().isHorizontal()) {
            Direction direction = getDirection().getCounterClockWise();
            BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();
            for (int pX = 0; pX < sizeX; ++pX) {
                for (int pY = 0; pY < sizeY; ++pY) {
                    mPos.set(baseBlockPos).move(direction, pX).move(Direction.UP, pY);
                    BlockState blockState = level().getBlockState(mPos);
                    if (blockState.isSolid() || DiodeBlock.isDiode(blockState)) continue;
                    return false;
                }
            }
        } else {
            BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();
            for (int pX = 0; pX < sizeX; ++pX) {
                for (int pY = 0; pY < sizeY; ++pY) {
                    mPos.set(baseBlockPos).move(Direction.NORTH, pX).move(Direction.EAST, pY);
                    BlockState blockState = level().getBlockState(mPos);
                    if (blockState.isSolid() || DiodeBlock.isDiode(blockState)) continue;
                    return false;
                }
            }
        }

        return level().getEntities(this, getBoundingBox(), HANGING_ENTITY).isEmpty();
    }

    @Override
    protected void setDirection(@NotNull Direction facingDirection) {
        Preconditions.checkNotNull(facingDirection);

        direction = facingDirection;
        if (facingDirection.getAxis().isHorizontal()) {
            setXRot(0.0f);
            setYRot(direction.get2DDataValue() * 90);
        } else {
            setXRot(-90 * facingDirection.getAxisDirection().getStep());
            setYRot(0.0f);
        }
        xRotO = getXRot();
        yRotO = getYRot();
        recalculateBoundingBox();
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        getEntityData().set(DATA_SIZE, Mth.clamp(size, 0, 2));
        this.size = size;
        recalculateBoundingBox();
    }

    public PhotographFrameItem getBaseFrameItem() {
        return Exposure.Items.PHOTOGRAPH_FRAME.get();
    }

    public ItemStack getFrameItem() {
        return getEntityData().get(DATA_FRAME_ITEM);
    }

    public void setFrameItem(ItemStack stack) {
        getEntityData().set(DATA_FRAME_ITEM, stack);
    }

    public ItemStack getItem() {
        return getEntityData().get(DATA_ITEM);
    }

    public void setItem(ItemStack stack) {
        getEntityData().set(DATA_ITEM, stack);
    }

    protected void onItemChanged(ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            itemStack.setEntityRepresentation(this);
        }
    }

    public int getItemRotation() {
        return getEntityData().get(DATA_ITEM_ROTATION);
    }

    public void setItemRotation(int rotation) {
        getEntityData().set(DATA_ITEM_ROTATION, rotation % 4);
    }

    public boolean isGlowing() {
        return getEntityData().get(DATA_GLOWING);
    }

    public void setGlowing(boolean glowing) {
        getEntityData().set(DATA_GLOWING, glowing);
    }

    public boolean isFrameInvisible() {
        return isInvisible();
    }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);

        if (itemInHand.getItem() instanceof PhotographItem && getItem().isEmpty()) {
            setItem(itemInHand.copy());
            itemInHand.shrink(1);
            gameEvent(GameEvent.BLOCK_CHANGE, player);
            playSound(getAddItemSound(), 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        if (itemInHand.is(Items.GLOW_INK_SAC) && !isGlowing()) {
            setGlowing(true);
            itemInHand.shrink(1);
            if (!level().isClientSide) {
                playSound(SoundEvents.GLOW_INK_SAC_USE);
                gameEvent(GameEvent.BLOCK_CHANGE, player);
            }
            return InteractionResult.SUCCESS;
        }

        if (!getItem().isEmpty()) {
            if (!level().isClientSide) {
                playSound(getRotateSound(), 1.0F, level().getRandom().nextFloat() * 0.2f + 0.9f);
                setItemRotation(getItemRotation() + 1);
                gameEvent(GameEvent.BLOCK_CHANGE, player);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean hurt(@NotNull DamageSource damageSource, float amount) {
        if (isInvulnerableTo(damageSource))
            return false;

        if (!damageSource.is(DamageTypeTags.IS_EXPLOSION) && !getItem().isEmpty()) {
            if (!level().isClientSide) {
                dropItem(damageSource.getEntity(), false);
                gameEvent(GameEvent.BLOCK_CHANGE, damageSource.getEntity());
                playSound(getRemoveItemSound(), 1.0f, 1.0f);
            }
            return true;
        }

        return super.hurt(damageSource, amount);
    }

    @Override
    public void dropItem(@Nullable Entity brokenEntity) {
        playSound(getBreakSound(), 1.0f, 1.0f);
        dropItem(brokenEntity, true);
        gameEvent(GameEvent.BLOCK_CHANGE, brokenEntity);
    }

    protected void dropItem(@Nullable Entity entity, boolean dropSelf) {
        ItemStack itemStack = getItem();
        setItem(ItemStack.EMPTY);

        if (!level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) return;
        if (entity instanceof Player player && player.isCreative()) return;

        // Prevent item phasing through the block when placed on the ceiling (pointing DOWN)
        float yOffset = getDirection() == Direction.DOWN ? -0.3f : 0f;

        if (dropSelf) {
            spawnAtLocation(getFrameItem(), yOffset);
        }

        if (!itemStack.isEmpty()) {
            spawnAtLocation(itemStack.copy(), yOffset);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_SIZE, 0);
        builder.define(DATA_FRAME_ITEM, ItemStack.EMPTY);
        builder.define(DATA_ITEM, ItemStack.EMPTY);
        builder.define(DATA_ITEM_ROTATION, 0);
        builder.define(DATA_GLOWING, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide && isGlowing() && level().getRandom().nextFloat() < 0.003f) {
            AABB bb = getBoundingBox();
            Vec3i normal = getDirection().getNormal();
            level().addParticle(ParticleTypes.END_ROD,
                    position().x + (level().getRandom().nextFloat() * (bb.getXsize() * 0.75f) - bb.getXsize() * 0.75f / 2),
                    position().y + (level().getRandom().nextFloat() * (bb.getYsize() * 0.75f) - bb.getYsize() * 0.75f / 2),
                    position().z + (level().getRandom().nextFloat() * (bb.getZsize() * 0.75f) - bb.getZsize() * 0.75f / 2),
                    level().getRandom().nextFloat() * 0.02f * normal.getX(),
                    level().getRandom().nextFloat() * 0.02f * normal.getY(),
                    level().getRandom().nextFloat() * 0.02f * normal.getZ());
        }
    }

    @Override
    public @NotNull SlotAccess getSlot(int slot) {
        if (slot == 0) {
            return new SlotAccess() {

                @Override
                public @NotNull ItemStack get() {
                    return PhotographFrameEntity.this.getItem();
                }

                @Override
                public boolean set(ItemStack carried) {
                    PhotographFrameEntity.this.setItem(carried);
                    return true;
                }
            };
        }
        return super.getSlot(slot);
    }

    @Override
    protected @NotNull Component getTypeName() {
        if (isGlowing())
            return Component.translatable("entity.exposure.glow_photograph_frame");
        return super.getTypeName();
    }

    @Override
    public void playPlacementSound() {
        playSound(getPlaceSound(), 1.0F, level().getRandom().nextFloat() * 0.2f + 0.7f);
    }

    public SoundEvent getPlaceSound() {
        return Exposure.SoundEvents.PHOTOGRAPH_FRAME_PLACE.get();
    }

    public SoundEvent getBreakSound() {
        return Exposure.SoundEvents.PHOTOGRAPH_FRAME_BREAK.get();
    }

    public SoundEvent getAddItemSound() {
        return Exposure.SoundEvents.PHOTOGRAPH_FRAME_ADD_ITEM.get();
    }

    public SoundEvent getRemoveItemSound() {
        return Exposure.SoundEvents.PHOTOGRAPH_FRAME_REMOVE_ITEM.get();
    }

    public SoundEvent getRotateSound() {
        return Exposure.SoundEvents.PHOTOGRAPH_FRAME_ROTATE_ITEM.get();
    }
}
