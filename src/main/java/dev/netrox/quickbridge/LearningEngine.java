package dev.netrox.quickbridge;

public final class LearningEngine {
    private LearningEngine() {}

    public static TechniqueTuning effectiveTuning(
        BridgeConfig config,
        String serverId,
        BridgeTechnique technique,
        NetworkCondition activeCondition,
        NetworkCondition previousCondition,
        double transitionBlend
    ) {
        TechniqueTuning manual = config.tuning(technique);
        if (!config.autoLearning()) return applyNetworkGuard(manual, activeCondition, config.networkGuard());

        TechniqueTuning baseline = profile(serverId, technique).applyTo(manual, 0.60D);
        if (!config.networkProfiles()) {
            return applyNetworkGuard(baseline, activeCondition, config.networkGuard());
        }

        NetworkCondition active = activeCondition == null ? NetworkCondition.STABLE : activeCondition;
        NetworkCondition previous = previousCondition == null ? active : previousCondition;
        TechniqueTuning activeTuning = conditionProfile(serverId, technique, active).applyTo(baseline, 0.90D);

        TechniqueTuning conditioned;
        if (previous == active || transitionBlend >= 1.0D) {
            conditioned = activeTuning;
        } else {
            TechniqueTuning previousTuning = conditionProfile(serverId, technique, previous).applyTo(baseline, 0.90D);
            conditioned = blend(previousTuning, activeTuning, transitionBlend);
        }
        return applyNetworkGuard(conditioned, active, config.networkGuard());
    }

    public static LearningProfile profile(String serverId, BridgeTechnique technique) {
        return ServerLearningStore.get().profile(serverId, technique);
    }

    public static LearningProfile conditionProfile(
        String serverId,
        BridgeTechnique technique,
        NetworkCondition condition
    ) {
        return ServerLearningStore.get().conditionProfile(serverId, technique, condition);
    }

    public static void recordSuccess(
        String serverId,
        BridgeTechnique technique,
        NetworkCondition condition,
        int confirmationAge,
        int confirmationBudget,
        boolean recovered,
        boolean enabled
    ) {
        if (!enabled) return;
        LearningProfile global = profile(serverId, technique);
        global.observeSuccess(confirmationAge, confirmationBudget, recovered);
        LearningProfile conditioned = conditionProfile(serverId, technique, safe(condition));
        conditioned.observeSuccess(confirmationAge, confirmationBudget, recovered);
        ServerLearningStore.get().markDirty();
    }

    public static void recordFailure(
        String serverId,
        BridgeTechnique technique,
        NetworkCondition condition,
        boolean enabled
    ) {
        if (!enabled) return;
        boolean complex = isComplexRotation(technique);
        profile(serverId, technique).observeFailure(complex);
        conditionProfile(serverId, technique, safe(condition)).observeFailure(complex);
        ServerLearningStore.get().markDirty();
    }

    public static boolean recordCycle(
        String serverId,
        BridgeTechnique technique,
        BridgeCycleResult result,
        boolean enabled
    ) {
        if (!enabled || result == null) return false;
        boolean complex = isComplexRotation(technique);
        boolean globalRollback = profile(serverId, technique).observeCycle(result, complex);
        boolean conditionRollback = conditionProfile(serverId, technique, result.networkCondition())
            .observeCycle(result, complex);
        ServerLearningStore.get().markDirty();
        return globalRollback || conditionRollback;
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

    public static void resetCondition(
        String serverId,
        BridgeTechnique technique,
        NetworkCondition condition
    ) {
        ServerLearningStore.get().resetCondition(serverId, technique, safe(condition));
    }

    public static void resetServer(String serverId) {
        ServerLearningStore.get().resetServer(serverId);
    }

    private static TechniqueTuning applyNetworkGuard(
        TechniqueTuning tuning,
        NetworkCondition condition,
        boolean enabled
    ) {
        if (!enabled || condition == null) return tuning;
        return switch (condition) {
            case STABLE -> tuning;
            case DELAYED -> new TechniqueTuning(
                tuning.cycleScale() + 0.02D,
                tuning.leadOffset() - 0.01D,
                tuning.rotationScale(),
                tuning.cadenceBias() - 0.025D
            );
            case UNSTABLE -> new TechniqueTuning(
                tuning.cycleScale() + 0.05D,
                tuning.leadOffset() - 0.02D,
                tuning.rotationScale() + 0.03F,
                tuning.cadenceBias() - 0.060D
            );
        };
    }

    private static TechniqueTuning blend(TechniqueTuning from, TechniqueTuning to, double amount) {
        double t = clamp(amount, 0.0D, 1.0D);
        return new TechniqueTuning(
            lerp(from.cycleScale(), to.cycleScale(), t),
            lerp(from.leadOffset(), to.leadOffset(), t),
            (float) lerp(from.rotationScale(), to.rotationScale(), t),
            lerp(from.cadenceBias(), to.cadenceBias(), t)
        );
    }

    private static double lerp(double from, double to, double amount) {
        return from + (to - from) * amount;
    }

    private static NetworkCondition safe(NetworkCondition condition) {
        return condition == null ? NetworkCondition.STABLE : condition;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static boolean isComplexRotation(BridgeTechnique technique) {
        return switch (technique) {
            case TELLY, SPEED_TELLY, BLINK_BRIDGE, ANDROMEDA, WITCHLY, MOONWALK -> true;
            default -> false;
        };
    }
}
