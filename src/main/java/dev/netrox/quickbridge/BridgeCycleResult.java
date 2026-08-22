package dev.netrox.quickbridge;

public record BridgeCycleResult(
    int confirmed,
    int failed,
    int recoveries,
    double quality,
    boolean successful,
    NetworkCondition networkCondition
) {
    public static BridgeCycleResult of(
        int confirmed,
        int failed,
        int recoveries,
        double ackTicks,
        int confirmationBudget,
        double reliability
    ) {
        int safeConfirmed = Math.max(0, confirmed);
        int safeFailed = Math.max(0, failed);
        int safeRecoveries = Math.max(0, recoveries);
        int total = safeConfirmed + safeFailed;
        double recoveryRate = safeConfirmed == 0 ? (safeRecoveries > 0 ? 1.0D : 0.0D)
            : safeRecoveries / (double) safeConfirmed;
        NetworkCondition condition = NetworkCondition.classify(
            ackTicks,
            confirmationBudget,
            reliability,
            recoveryRate
        );
        double denominator = Math.max(1.0D, safeConfirmed + safeFailed + safeRecoveries * 0.40D);
        double raw = safeConfirmed / denominator;
        double quality = Math.max(0.0D, Math.min(1.0D, raw * condition.learningWeight()));
        boolean successful = safeConfirmed > 0 && safeFailed == 0 && quality >= 0.62D;
        return new BridgeCycleResult(safeConfirmed, safeFailed, safeRecoveries, quality, successful, condition);
    }
}
