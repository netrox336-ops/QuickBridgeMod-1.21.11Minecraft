package dev.netrox.quickbridge;

public final class CycleEvaluator {
    private double lastProgress;
    private int baseConfirmed;
    private int baseFailed;
    private int baseRecoveries;
    private boolean primed;
    private BridgeCycleResult lastResult = BridgeCycleResult.of(0, 0, 0, 0.0D, 5, 1.0D);

    public void reset(int confirmed, int failed, int recoveries) {
        lastProgress = 0.0D;
        baseConfirmed = confirmed;
        baseFailed = failed;
        baseRecoveries = recoveries;
        primed = false;
        lastResult = BridgeCycleResult.of(0, 0, 0, 0.0D, 5, 1.0D);
    }

    public BridgeCycleResult tick(
        TechniqueStateMachine.Snapshot snapshot,
        int confirmed,
        int failed,
        int recoveries,
        double ackTicks,
        int confirmationBudget,
        double reliability
    ) {
        double progress = snapshot.cycleProgress();
        if (!primed) {
            primed = true;
            lastProgress = progress;
            baseConfirmed = confirmed;
            baseFailed = failed;
            baseRecoveries = recoveries;
            return null;
        }

        boolean wrapped = lastProgress > 0.72D && progress < 0.28D;
        lastProgress = progress;
        if (!wrapped) return null;

        BridgeCycleResult result = BridgeCycleResult.of(
            confirmed - baseConfirmed,
            failed - baseFailed,
            recoveries - baseRecoveries,
            ackTicks,
            confirmationBudget,
            reliability
        );
        baseConfirmed = confirmed;
        baseFailed = failed;
        baseRecoveries = recoveries;
        lastResult = result;
        return result;
    }

    public BridgeCycleResult lastResult() {
        return lastResult;
    }
}
