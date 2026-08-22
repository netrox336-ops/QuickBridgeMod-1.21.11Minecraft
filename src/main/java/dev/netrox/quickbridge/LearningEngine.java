package dev.netrox.quickbridge;

public final class LearningEngine {
    private LearningEngine() {}

    public static TechniqueTuning effectiveTuning(
        BridgeConfig config,
        String serverId,
        BridgeTechnique technique
    ) {
        TechniqueTuning manual = config.tuning(technique);
        if (!config.autoLearning()) return manual;
        return profile(serverId, technique).applyTo(manual);
    }

    public static LearningProfile profile(String serverId, BridgeTechnique technique) {
        return ServerLearningStore.get().profile(serverId, technique);
    }

    public static void recordSuccess(
        String serverId,
        BridgeTechnique technique,
        int confirmationAge,
        int confirmationBudget,
        boolean recovered,
        boolean enabled
    ) {
        if (!enabled) return;
        LearningProfile profile = profile(serverId, technique);
        profile.observeSuccess(confirmationAge, confirmationBudget, recovered);
        ServerLearningStore.get().markDirty();
    }

    public static void recordFailure(
        String serverId,
        BridgeTechnique technique,
        boolean enabled
    ) {
        if (!enabled) return;
        LearningProfile profile = profile(serverId, technique);
        profile.observeFailure(isComplexRotation(technique));
        ServerLearningStore.get().markDirty();
    }

    public static boolean recordCycle(
        String serverId,
        BridgeTechnique technique,
        BridgeCycleResult result,
        boolean enabled
    ) {
        if (!enabled || result == null) return false;
        LearningProfile profile = profile(serverId, technique);
        boolean rolledBack = profile.observeCycle(result, isComplexRotation(technique));
        ServerLearningStore.get().markDirty();
        return rolledBack;
    }

    public static NetworkCondition networkCondition(
        String serverId,
        BridgeTechnique technique,
        int confirmationBudget
    ) {
        LearningProfile profile = profile(serverId, technique);
        double recoveryRate = profile.successes() == 0L
            ? 0.0D
            : profile.recoveredSuccesses() / (double) profile.successes();
        return NetworkCondition.classify(
            profile.ackEma(),
            confirmationBudget,
            profile.reliability(),
            recoveryRate
        );
    }

    public static void flush() {
        ServerLearningStore.get().flush();
    }

    public static void reset(String serverId, BridgeTechnique technique) {
        ServerLearningStore.get().reset(serverId, technique);
    }

    public static void resetServer(String serverId) {
        ServerLearningStore.get().resetServer(serverId);
    }

    private static boolean isComplexRotation(BridgeTechnique technique) {
        return switch (technique) {
            case TELLY, SPEED_TELLY, BLINK_BRIDGE, ANDROMEDA, WITCHLY, MOONWALK -> true;
            default -> false;
        };
    }
}
