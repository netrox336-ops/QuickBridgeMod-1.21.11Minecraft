package dev.netrox.quickbridge;

public final class TrainingSession {
    private static int cycles;
    private static int successfulCycles;
    private static double qualityEma;
    private static NetworkCondition lastCondition = NetworkCondition.STABLE;

    private TrainingSession() {}

    public static void reset() {
        cycles = 0;
        successfulCycles = 0;
        qualityEma = 0.0D;
        lastCondition = NetworkCondition.STABLE;
    }

    public static void record(BridgeCycleResult result) {
        if (result == null) return;
        cycles++;
        if (result.successful()) successfulCycles++;
        qualityEma = cycles == 1 ? result.quality() : qualityEma * 0.78D + result.quality() * 0.22D;
        lastCondition = result.networkCondition();
    }

    public static int cycles() { return cycles; }
    public static int successfulCycles() { return successfulCycles; }
    public static double qualityEma() { return qualityEma; }
    public static NetworkCondition lastCondition() { return lastCondition; }

    public static double successRate() {
        return cycles == 0 ? 0.0D : successfulCycles / (double) cycles;
    }
}
