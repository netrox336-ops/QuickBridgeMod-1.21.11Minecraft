package dev.netrox.quickbridge;

public record TechniqueTuning(
    double cycleScale,
    double leadOffset,
    float rotationScale,
    double cadenceBias
) {
    public static TechniqueTuning defaults() {
        return new TechniqueTuning(1.0D, 0.0D, 1.0F, 0.0D);
    }

    public TechniqueTuning {
        cycleScale = clamp(cycleScale, 0.70D, 1.40D);
        leadOffset = clamp(leadOffset, -0.25D, 0.35D);
        rotationScale = (float) clamp(rotationScale, 0.55D, 1.60D);
        cadenceBias = clamp(cadenceBias, -0.25D, 0.25D);
    }

    public TechniqueTuning withCycleScale(double value) {
        return new TechniqueTuning(value, leadOffset, rotationScale, cadenceBias);
    }

    public TechniqueTuning withLeadOffset(double value) {
        return new TechniqueTuning(cycleScale, value, rotationScale, cadenceBias);
    }

    public TechniqueTuning withRotationScale(float value) {
        return new TechniqueTuning(cycleScale, leadOffset, value, cadenceBias);
    }

    public TechniqueTuning withCadenceBias(double value) {
        return new TechniqueTuning(cycleScale, leadOffset, rotationScale, value);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
