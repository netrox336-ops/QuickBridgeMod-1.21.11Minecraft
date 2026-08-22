package dev.netrox.quickbridge;

public final class LearningProfile {
    private long samples;
    private long successes;
    private long failures;
    private long recoveredSuccesses;
    private long cycles;
    private long successfulCycles;
    private long rollbacks;
    private double ackEma;
    private double cycleQualityEma;
    private double bestCycleQuality;
    private double cycleAdjustment;
    private double leadAdjustment;
    private double rotationAdjustment;
    private double cadenceAdjustment;

    private long checkpointCycle;
    private double checkpointQuality;
    private double checkpointCycleAdjustment;
    private double checkpointLeadAdjustment;
    private double checkpointRotationAdjustment;
    private double checkpointCadenceAdjustment;
    private int regressionStreak;

    public long samples() { return samples; }
    public long successes() { return successes; }
    public long failures() { return failures; }
    public long recoveredSuccesses() { return recoveredSuccesses; }
    public long cycles() { return cycles; }
    public long successfulCycles() { return successfulCycles; }
    public long rollbacks() { return rollbacks; }
    public double ackEma() { return ackEma; }
    public double cycleQualityEma() { return cycleQualityEma; }
    public double bestCycleQuality() { return bestCycleQuality; }
    public double cycleAdjustment() { return cycleAdjustment; }
    public double leadAdjustment() { return leadAdjustment; }
    public double rotationAdjustment() { return rotationAdjustment; }
    public double cadenceAdjustment() { return cadenceAdjustment; }
    public long checkpointCycle() { return checkpointCycle; }
    public double checkpointQuality() { return checkpointQuality; }
    public double checkpointCycleAdjustment() { return checkpointCycleAdjustment; }
    public double checkpointLeadAdjustment() { return checkpointLeadAdjustment; }
    public double checkpointRotationAdjustment() { return checkpointRotationAdjustment; }
    public double checkpointCadenceAdjustment() { return checkpointCadenceAdjustment; }

    public double reliability() {
        long total = successes + failures;
        return total == 0L ? 1.0D : successes / (double) total;
    }

    public double cycleSuccessRate() {
        return cycles == 0L ? 1.0D : successfulCycles / (double) cycles;
    }

    public double confidence() {
        double placementConfidence = clamp(samples / 60.0D, 0.0D, 1.0D);
        double cycleConfidence = clamp(cycles / 24.0D, 0.0D, 1.0D);
        return placementConfidence * 0.55D + cycleConfidence * 0.45D;
    }

    public void observeSuccess(int confirmationAge, int confirmationBudget, boolean recovered) {
        samples++;
        successes++;
        if (recovered) recoveredSuccesses++;
        updateAck(confirmationAge);

        double budget = Math.max(1.0D, confirmationBudget);
        double ackRatio = confirmationAge / budget;
        if (recovered) {
            cadenceAdjustment -= 0.0025D;
            cycleAdjustment += 0.0018D;
            leadAdjustment -= 0.0010D;
        } else if (ackRatio <= 0.45D && reliability() >= 0.96D) {
            cadenceAdjustment += 0.0012D;
            cycleAdjustment -= 0.0008D;
        } else if (ackRatio >= 0.80D) {
            cadenceAdjustment -= 0.0018D;
            cycleAdjustment += 0.0012D;
        }
        clampAdjustments();
    }

    public void observeFailure(boolean complexRotation) {
        samples++;
        failures++;
        cadenceAdjustment -= 0.007D;
        cycleAdjustment += 0.005D;
        leadAdjustment -= 0.003D;
        if (complexRotation) rotationAdjustment += 0.002D;
        clampAdjustments();
    }

    public boolean observeCycle(BridgeCycleResult result, boolean complexRotation) {
        if (result == null) return false;
        cycles++;
        if (result.successful()) successfulCycles++;
        cycleQualityEma = cycles == 1L
            ? result.quality()
            : cycleQualityEma * 0.82D + result.quality() * 0.18D;
        bestCycleQuality = Math.max(bestCycleQuality, cycleQualityEma);

        double weight = result.networkCondition().learningWeight();
        if (result.successful()) {
            regressionStreak = Math.max(0, regressionStreak - 1);
            if (result.quality() >= 0.82D && result.networkCondition() == NetworkCondition.STABLE) {
                cadenceAdjustment += 0.0022D * weight;
                cycleAdjustment -= 0.0014D * weight;
                if (complexRotation) rotationAdjustment -= 0.0007D;
            }
        } else {
            regressionStreak++;
            cadenceAdjustment -= 0.0055D * weight;
            cycleAdjustment += 0.0040D * weight;
            leadAdjustment -= 0.0022D * weight;
            if (complexRotation) rotationAdjustment += 0.0015D * weight;
        }
        clampAdjustments();

        if (shouldCheckpoint()) saveCheckpoint();
        if (shouldRollback()) {
            rollbackToCheckpoint();
            return true;
        }
        return false;
    }

    private boolean shouldCheckpoint() {
        if (cycles < 6L || cycles - checkpointCycle < 6L) return false;
        return cycleQualityEma >= 0.72D && cycleQualityEma >= checkpointQuality - 0.015D;
    }

    private void saveCheckpoint() {
        checkpointCycle = cycles;
        checkpointQuality = cycleQualityEma;
        checkpointCycleAdjustment = cycleAdjustment;
        checkpointLeadAdjustment = leadAdjustment;
        checkpointRotationAdjustment = rotationAdjustment;
        checkpointCadenceAdjustment = cadenceAdjustment;
        regressionStreak = 0;
    }

    private boolean shouldRollback() {
        if (checkpointCycle <= 0L || cycles - checkpointCycle < 3L) return false;
        return regressionStreak >= 3 && cycleQualityEma < checkpointQuality - 0.10D;
    }

    private void rollbackToCheckpoint() {
        cycleAdjustment = checkpointCycleAdjustment;
        leadAdjustment = checkpointLeadAdjustment;
        rotationAdjustment = checkpointRotationAdjustment;
        cadenceAdjustment = checkpointCadenceAdjustment - 0.004D;
        rollbacks++;
        regressionStreak = 0;
        clampAdjustments();
    }

    private void updateAck(int confirmationAge) {
        double value = Math.max(0, confirmationAge);
        ackEma = successes <= 1 ? value : ackEma * 0.86D + value * 0.14D;
    }

    public TechniqueTuning applyTo(TechniqueTuning manual) {
        double weight = confidence();
        if (weight <= 0.0D) return manual;
        return new TechniqueTuning(
            manual.cycleScale() + cycleAdjustment * weight,
            manual.leadOffset() + leadAdjustment * weight,
            manual.rotationScale() + (float) (rotationAdjustment * weight),
            manual.cadenceBias() + cadenceAdjustment * weight
        );
    }

    public void restore(
        long samples,
        long successes,
        long failures,
        long recoveredSuccesses,
        double ackEma,
        double cycleAdjustment,
        double leadAdjustment,
        double rotationAdjustment,
        double cadenceAdjustment,
        long cycles,
        long successfulCycles,
        long rollbacks,
        double cycleQualityEma,
        double bestCycleQuality,
        long checkpointCycle,
        double checkpointQuality,
        double checkpointCycleAdjustment,
        double checkpointLeadAdjustment,
        double checkpointRotationAdjustment,
        double checkpointCadenceAdjustment
    ) {
        this.samples = Math.max(0L, samples);
        this.successes = Math.max(0L, successes);
        this.failures = Math.max(0L, failures);
        this.recoveredSuccesses = Math.max(0L, recoveredSuccesses);
        this.ackEma = Math.max(0.0D, ackEma);
        this.cycleAdjustment = cycleAdjustment;
        this.leadAdjustment = leadAdjustment;
        this.rotationAdjustment = rotationAdjustment;
        this.cadenceAdjustment = cadenceAdjustment;
        this.cycles = Math.max(0L, cycles);
        this.successfulCycles = Math.max(0L, successfulCycles);
        this.rollbacks = Math.max(0L, rollbacks);
        this.cycleQualityEma = clamp(cycleQualityEma, 0.0D, 1.0D);
        this.bestCycleQuality = clamp(bestCycleQuality, 0.0D, 1.0D);
        this.checkpointCycle = Math.max(0L, checkpointCycle);
        this.checkpointQuality = clamp(checkpointQuality, 0.0D, 1.0D);
        this.checkpointCycleAdjustment = checkpointCycleAdjustment;
        this.checkpointLeadAdjustment = checkpointLeadAdjustment;
        this.checkpointRotationAdjustment = checkpointRotationAdjustment;
        this.checkpointCadenceAdjustment = checkpointCadenceAdjustment;
        clampAdjustments();
    }

    private void clampAdjustments() {
        cycleAdjustment = clamp(cycleAdjustment, -0.20D, 0.30D);
        leadAdjustment = clamp(leadAdjustment, -0.12D, 0.16D);
        rotationAdjustment = clamp(rotationAdjustment, -0.20D, 0.25D);
        cadenceAdjustment = clamp(cadenceAdjustment, -0.15D, 0.12D);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
