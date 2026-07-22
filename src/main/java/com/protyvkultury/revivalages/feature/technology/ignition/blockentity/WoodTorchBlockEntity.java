package com.protyvkultury.revivalages.feature.technology.ignition.blockentity;

import com.protyvkultury.revivalages.feature.technology.ignition.IgnitionFeature;
import com.protyvkultury.revivalages.feature.technology.ignition.block.WoodTorchBlock;
import com.protyvkultury.revivalages.feature.technology.ignition.block.WoodTorchState;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class WoodTorchBlockEntity extends BlockEntity {

    private int remainingTicks = -1;

    public WoodTorchBlockEntity(BlockPos pos, BlockState state) {
        super(IgnitionFeature.WOOD_TORCH_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WoodTorchBlockEntity torch) {
        if (state.getValue(WoodTorchBlock.STATE) != WoodTorchState.LIT || level.getGameTime() % 20L != 0L) {
            return;
        }
        if (PrimitiveTechnologyConfig.WOOD_TORCH_RAIN_EXTINGUISHES.get()
                && level.isRainingAt(pos)) {
            torch.douse();
            return;
        }
        if (!PrimitiveTechnologyConfig.WOOD_TORCH_BURNS_UP.get()) {
            return;
        }
        torch.ensureDuration();
        torch.remainingTicks -= 20;
        if (torch.remainingTicks <= 0) {
            level.removeBlock(pos, false);
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6F, 1.2F);
        } else {
            torch.sync();
        }
    }

    public boolean ignite() {
        if (level == null || getBlockState().getValue(WoodTorchBlock.STATE) == WoodTorchState.LIT) {
            return false;
        }
        if (PrimitiveTechnologyConfig.WOOD_TORCH_RAIN_EXTINGUISHES.get()
                && level.isRainingAt(worldPosition)) {
            return false;
        }
        ensureDuration();
        level.setBlock(worldPosition, getBlockState().setValue(WoodTorchBlock.STATE, WoodTorchState.LIT), Block.UPDATE_ALL);
        sync();
        return true;
    }

    public void douse() {
        if (level == null || getBlockState().getValue(WoodTorchBlock.STATE) != WoodTorchState.LIT) {
            return;
        }
        level.setBlock(worldPosition, getBlockState().setValue(WoodTorchBlock.STATE, WoodTorchState.DOUSED), Block.UPDATE_ALL);
        level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 1.0F);
        sync();
    }

    private void ensureDuration() {
        if (remainingTicks >= 0 || level == null) {
            return;
        }
        int duration = PrimitiveTechnologyConfig.WOOD_TORCH_DURATION.get();
        int variance = PrimitiveTechnologyConfig.WOOD_TORCH_DURATION_VARIANCE.get();
        remainingTicks = Math.max(0, duration + (variance == 0 ? 0 : level.random.nextInt(variance * 2 + 1) - variance));
    }

    public int remainingTicks() {
        return remainingTicks;
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        remainingTicks = tag.contains("RemainingTicks") ? tag.getInt("RemainingTicks") : -1;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("RemainingTicks", remainingTicks);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
