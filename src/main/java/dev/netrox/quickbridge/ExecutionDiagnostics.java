package dev.netrox.quickbridge;

public final class ExecutionDiagnostics {
    private static double motionScore = 1.0D;
    private static double yawError;
    private static double pitchError;
    private static boolean rotationAligned = true;
    private static boolean placementWindowOpen = true;
    private static boolean placementReady = true;
    private static long blockedPlacements;
    private static long allowedPlacements;
    private static long phaseResyncs;
    private static BridgeTechnique resyncTechnique;

    private ExecutionDiagnostics() {}

    public static void reset() {
        motionScore = 1.0D;
        yawError = 0.0D;
        pitchError = 0.0D;
        rotationAligned = true;
        placementWindowOpen = true;
        placementReady = true;
        blockedPlacements = 0L;
        allowedPlacements = 0L;
        phaseResyncs = 0L;
        resyncTechnique = null;
    }

    public static void reportRotation(
        double yawErrorDegrees,
        double pitchErrorDegrees,
        float yawTolerance,
        float pitchTolerance
    ) {
        yawError = Math.max(0.0D, yawErrorDegrees);
        pitchError = Math.max(0.0D, pitchErrorDegrees);
        rotationAligned = yawError <= Math.max(1.0F, yawTolerance)
            && pitchError <= Math.max(1.0F, pitchTolerance);
    }

    public static void reportExecution(double score, boolean windowOpen, boolean ready) {
        motionScore = clamp(score, 0.0D, 1.0D);
        placementWindowOpen = windowOpen;
        placementReady = ready;
    }

    public static void recordPlacementDecision(boolean allowed) {
        if (allowed) allowedPlacements++;
        else blockedPlacements++;
    }

    public static void requestPhaseResync(BridgeTechnique technique) {
        if (technique == null) return;
        resyncTechnique = technique;
    }

    public static boolean consumePhaseResync(BridgeTechnique technique) {
        if (technique == null || resyncTechnique != technique) return false;
        resyncTechnique = null;
        phaseResyncs++;
        return true;
    }

    public static double motionScore() { return motionScore; }
    public static double yawError() { return yawError; }
    public static double pitchError() { return pitchError; }
    public static boolean rotationAligned() { return rotationAligned; }
    public static boolean placementWindowOpen() { return placementWindowOpen; }
    public static boolean placementReady() { return placementReady; }
    public static long blockedPlacements() { return blockedPlacements; }
    public static long allowedPlacements() { return allowedPlacements; }
    public static long phaseResyncs() { return phaseResyncs; }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
