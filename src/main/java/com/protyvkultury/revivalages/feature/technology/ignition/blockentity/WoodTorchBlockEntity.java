package com.protyvkultury.revivalages.feature.technology.ignition.blockentity;

import com.protyvkultury.revivalages.feature.content.ContentAvailability;
import com.protyvkultury.revivalages.feature.content.ContentKey;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class WoodTorchBlockEntity extends BlockEntity {

    private int remainingTicks = -1;
    private long lastTimeStamp;
    private long nextCheckTime;

    public WoodTorchBlockEntity(BlockPos pos, BlockState state) {
        super(IgnitionFeature.WOOD_TORCH_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WoodTorchBlockEntity torch) {
        if (!ContentAvailability.isEnabled(ContentKey.WOOD_TORCH)) {
            return;
        }
        torch.ensureDuration();
        long now = level.getGameTime();
        if (state.getValue(WoodTorchBlock.STATE) == WoodTorchState.LIT
                && !PrimitiveTechnologyConfig.WOOD_TORCH_BURNS_UP.get()
                && torch.lastTimeStamp != 0L) {
            torch.remainingTicks = torch.remainingTicksAt(now);
            torch.lastTimeStamp = 0L;
            torch.sync();
        }
        if (state.getValue(WoodTorchBlock.STATE) == WoodTorchState.LIT
                && PrimitiveTechnologyConfig.WOOD_TORCH_BURNS_UP.get()) {
            if (torch.lastTimeStamp == 0L) {
                torch.lastTimeStamp = now;
                torch.sync();
            }
            if (torch.remainingTicksAt(now) <= 0) {
                level.removeBlock(pos, false);
                return;
            }
        }
        if (torch.nextCheckTime == 0L) {
            torch.scheduleNextCheck(now);
            torch.setChanged();
        }
        if (now < torch.nextCheckTime) {
            return;
        }
        torch.scheduleNextCheck(now);
        torch.setChanged();
        if (state.getValue(WoodTorchBlock.STATE) != WoodTorchState.LIT) {
            return;
        }
        if (PrimitiveTechnologyConfig.WOOD_TORCH_RAIN_EXTINGUISHES.get()
                && level.isRainingAt(pos.above())) {
            torch.douseFromRain();
            return;
        }
    }

    public boolean ignite() {
        if (!ContentAvailability.isEnabled(ContentKey.WOOD_TORCH)
                || level == null
                || getBlockState().getValue(WoodTorchBlock.STATE) == WoodTorchState.LIT) {
            return false;
        }
        if (PrimitiveTechnologyConfig.WOOD_TORCH_RAIN_EXTINGUISHES.get()
                && level.isRainingAt(worldPosition.above())) {
            return false;
        }
        ensureDuration();
        lastTimeStamp = PrimitiveTechnologyConfig.WOOD_TORCH_BURNS_UP.get() ? level.getGameTime() : 0L;
        level.setBlock(worldPosition, getBlockState().setValue(WoodTorchBlock.STATE, WoodTorchState.LIT), Block.UPDATE_ALL);
        sync();
        return true;
    }

    public void douseManually() {
        douse();
    }

    private void douseFromRain() {
        douse();
    }

    private void douse() {
        if (!ContentAvailability.isEnabled(ContentKey.WOOD_TORCH)
                || level == null
                || getBlockState().getValue(WoodTorchBlock.STATE) != WoodTorchState.LIT) {
            return;
        }
        if (PrimitiveTechnologyConfig.WOOD_TORCH_BURNS_UP.get()) {
            remainingTicks = remainingTicksAt(level.getGameTime());
        }
        lastTimeStamp = 0L;
        level.setBlock(worldPosition, getBlockState().setValue(WoodTorchBlock.STATE, WoodTorchState.DOUSED), Block.UPDATE_ALL);
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

    public int remainingTicksAt(long gameTime) {
        if (getBlockState().getValue(WoodTorchBlock.STATE) != WoodTorchState.LIT || lastTimeStamp == 0L) {
            return remainingTicks;
        }
        long elapsed = Math.max(0L, gameTime - lastTimeStamp);
        return (int) Math.max(0L, remainingTicks - elapsed);
    }

    private void scheduleNextCheck(long now) {
        int min = PrimitiveTechnologyConfig.WOOD_TORCH_MIN_CHECK_INTERVAL.get();
        int max = PrimitiveTechnologyConfig.WOOD_TORCH_MAX_CHECK_INTERVAL.get();
        int lower = Math.min(min, max);
        int upper = Math.max(min, max);
        nextCheckTime = now + lower + (upper == lower ? 0 : level.random.nextInt(upper - lower + 1));
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
        lastTimeStamp = tag.getLong("LastTimeStamp");
        nextCheckTime = tag.getLong("NextCheckTime");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("RemainingTicks", remainingTicks);
        tag.putLong("LastTimeStamp", lastTimeStamp);
        tag.putLong("NextCheckTime", nextCheckTime);
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
