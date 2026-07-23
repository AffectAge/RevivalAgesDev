package com.protyvkultury.revivalages.core.machine;

import java.util.function.BooleanSupplier;
import net.minecraft.nbt.CompoundTag;

/**
 * Shared implementation of the designated reference's structure validation lifecycle.
 * Healthy structures are checked periodically; an invalid structure receives a
 * bounded grace period before failure and can recover during that period.
 */
public final class BurnableStructureTracker {

    private static final String INVALID_TICKS_TAG = "InvalidStructureTicks";
    private static final String VALIDATION_COUNTDOWN_TAG = "StructureValidationCountdown";

    private final int validationInterval;
    private final int maximumInvalidTicks;
    private int validationCountdown;
    private int invalidTicks;
    private boolean validationRequired = true;

    public BurnableStructureTracker(int validationInterval, int maximumInvalidTicks) {
        if (validationInterval <= 0 || maximumInvalidTicks <= 0) {
            throw new IllegalArgumentException("Structure validation timings must be positive");
        }
        this.validationInterval = validationInterval;
        this.maximumInvalidTicks = maximumInvalidTicks;
    }

    public Result tick(BooleanSupplier structureValid) {
        if (!validationRequired) {
            validationCountdown--;
            if (validationCountdown <= 0) {
                validationRequired = true;
            }
        }

        if (!validationRequired) {
            return Result.VALID;
        }
        if (structureValid.getAsBoolean()) {
            invalidTicks = 0;
            validationCountdown = validationInterval;
            validationRequired = false;
            return Result.VALID;
        }

        if (invalidTicks < maximumInvalidTicks) {
            invalidTicks++;
            return Result.INVALID_GRACE;
        }
        return Result.FAILED;
    }

    public void requireValidation() {
        validationRequired = true;
    }

    public int invalidTicks() {
        return invalidTicks;
    }

    public int maximumInvalidTicks() {
        return maximumInvalidTicks;
    }

    public void load(CompoundTag tag) {
        invalidTicks = Math.clamp(tag.getInt(INVALID_TICKS_TAG), 0, maximumInvalidTicks);
        validationCountdown = Math.clamp(tag.getInt(VALIDATION_COUNTDOWN_TAG), 0, validationInterval);
        validationRequired = validationCountdown == 0 || invalidTicks > 0;
    }

    public void save(CompoundTag tag) {
        tag.putInt(INVALID_TICKS_TAG, invalidTicks);
        tag.putInt(VALIDATION_COUNTDOWN_TAG, validationCountdown);
    }

    public enum Result {
        VALID,
        INVALID_GRACE,
        FAILED
    }
}
