package dev.netrox.quickbridge;

import net.minecraft.core.BlockPos;

public final class BridgePathDiagnostics {
    private static int scannedCandidates;
    private static int viableCandidates;
    private static int chosenRank = -1;
    private static double chosenScore;
    private static BlockPos chosenTarget;
    private static long planMisses;
    private static double firstGapDistance = 2.40D;
    private static double firstObstacleDistance = 2.40D;
    private static double supportRatio = 1.0D;
    private static double longestGapLength;
    private static boolean obstacleAhead;
    private static boolean dangerousGap;

    private BridgePathDiagnostics() {}

    public static void reset() {
        scannedCandidates = 0;
        viableCandidates = 0;
        chosenRank = -1;
        chosenScore = 0.0D;
        chosenTarget = null;
        planMisses = 0L;
        firstGapDistance = 2.40D;
        firstObstacleDistance = 2.40D;
        supportRatio = 1.0D;
        longestGapLength = 0.0D;
        obstacleAhead = false;
        dangerousGap = false;
    }

    public static void reportPlan(int scanned, int viable) {
        scannedCandidates = Math.max(0, scanned);
        viableCandidates = Math.max(0, viable);
        chosenRank = -1;
        chosenScore = 0.0D;
        chosenTarget = null;
    }

    public static void reportChosen(int rank, double score, BlockPos target) {
        chosenRank = rank;
        chosenScore = score;
        chosenTarget = target;
    }

    public static void reportMiss() {
        planMisses++;
        chosenRank = -1;
        chosenTarget = null;
    }

    public static void reportProbe(PathProbe.Snapshot snapshot) {
        if (snapshot == null) return;
        firstGapDistance = snapshot.firstGapDistance();
        firstObstacleDistance = snapshot.firstObstacleDistance();
        supportRatio = snapshot.supportRatio();
        longestGapLength = snapshot.longestGapLength();
        obstacleAhead = snapshot.obstacleAhead();
        dangerousGap = snapshot.dangerousGap();
    }

    public static int scannedCandidates() { return scannedCandidates; }
    public static int viableCandidates() { return viableCandidates; }
    public static int chosenRank() { return chosenRank; }
    public static double chosenScore() { return chosenScore; }
    public static BlockPos chosenTarget() { return chosenTarget; }
    public static long planMisses() { return planMisses; }
    public static double firstGapDistance() { return firstGapDistance; }
    public static double firstObstacleDistance() { return firstObstacleDistance; }
    public static double supportRatio() { return supportRatio; }
    public static double longestGapLength() { return longestGapLength; }
    public static boolean obstacleAhead() { return obstacleAhead; }
    public static boolean dangerousGap() { return dangerousGap; }
}
