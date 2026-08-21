package dev.netrox.quickbridge;

public final class LearningProfile {
    private long samples;
    private long successes;
    private long failures;
    private long recoveredSuccesses;
    private double ackEma;
    private double cycleAdjustment;
    private double leadAdjustment;
    private double rotationAdjustment;
    private double cadenceAdjustment;

    public long samples() { return samples; }
    public long successes() { return successes; }
    public long failures() { return failures; }
    public long recoveredSuccesses() { return recoveredSuccesses; }
    public double ackEma() { return ackEma; }
    public double cycleAdjustment() { return cycleAdjustment; }
    public double leadAdjustment() { return leadAdjustment; }
    public double rotationAdjustment() { return rotationAdjustment; }
    public double cadenceAdjustment() { return cadenceAdjustment; }

    public double reliability() {
        long total = successes + failures;
        return total == 0L ? 1.0D : successes / (double) total;
    }

    public double confidence() {
        return clamp(samples / 60.0D, 0.0D, 1.0D);
    }

    public void observeSuccess(int confirmationAge, int confirmationBudget, boolean recovered) {
        samples++;
        successes++;
        if (recovered) recoveredSuccesses++;
        updateAck(confirmationAge);

        double budget = Math.max(1.0D, confirmationBudget);
        double ackRatio = confirmationAge / budget;

        if (recovered) {
            cadenceAdjustment -= 0.0035D;
            cycleAdjustment += 0.0025D;
            leadAdjustment -= 0.0015D;
        } else if (ackRatio <= 0.45D && reliability() >= 0.96D) {
            cadenceAdjustment += 0.0018D;
            cycleAdjustment -= 0.0010D;
        } else if (ackRatio >= 0.80D) {
            cadenceAdjustment -= 0.0025D;
            cycleAdjustment += 0.0018D;
        }
        clampAdjustments();
    }

    public void observeFailure(boolean complexRotation) {
        samples++;
        failures++;
        cadenceAdjustment -= 0.010D;
        cycleAdjustment += 0.008D;
        leadAdjustment -= 0.004D;
        if (complexRotation) rotationAdjustment += 0.003D;
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
        double cadenceAdjustment
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
