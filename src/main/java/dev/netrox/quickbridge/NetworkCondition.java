package dev.netrox.quickbridge;

public enum NetworkCondition {
    STABLE("STABLE", 1.00D),
    DELAYED("DELAYED", 0.82D),
    UNSTABLE("UNSTABLE", 0.64D);

    private final String displayName;
    private final double learningWeight;

    NetworkCondition(String displayName, double learningWeight) {
        this.displayName = displayName;
        this.learningWeight = learningWeight;
    }

    public String displayName() {
        return displayName;
    }

    public double learningWeight() {
        return learningWeight;
    }

    public static NetworkCondition classify(double ackTicks, int confirmationBudget, double reliability, double recoveryRate) {
        double budget = Math.max(1.0D, confirmationBudget);
        double ratio = ackTicks <= 0.0D ? 0.0D : ackTicks / budget;
        if (reliability < 0.82D || recoveryRate > 0.28D || ratio > 1.0D) return UNSTABLE;
        if (reliability < 0.94D || recoveryRate > 0.12D || ratio > 0.62D) return DELAYED;
        return STABLE;
    }
}
