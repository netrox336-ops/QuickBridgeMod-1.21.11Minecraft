package dev.netrox.quickbridge;

public final class NetworkProfileSelector {
    private NetworkCondition active = NetworkCondition.STABLE;
    private NetworkCondition previous = NetworkCondition.STABLE;
    private NetworkCondition candidate = NetworkCondition.STABLE;
    private int candidateCycles;
    private int switches;
    private double blend = 1.0D;

    public void reset(NetworkCondition initial) {
        active = initial == null ? NetworkCondition.STABLE : initial;
        previous = active;
        candidate = active;
        candidateCycles = 0;
        switches = 0;
        blend = 1.0D;
    }

    public void observe(NetworkCondition observed) {
        if (observed == null) return;

        if (observed == active) {
            candidate = active;
            candidateCycles = 0;
            return;
        }

        if (observed != candidate) {
            candidate = observed;
            candidateCycles = 1;
        } else {
            candidateCycles++;
        }

        int threshold = severity(observed) > severity(active) ? 2 : 3;
        if (candidateCycles < threshold) return;

        previous = active;
        active = observed;
        candidate = active;
        candidateCycles = 0;
        blend = 0.0D;
        switches++;
    }

    public void tick() {
        if (blend < 1.0D) blend = Math.min(1.0D, blend + 0.05D);
    }

    public NetworkCondition active() { return active; }
    public NetworkCondition previous() { return previous; }
    public NetworkCondition candidate() { return candidate; }
    public int candidateCycles() { return candidateCycles; }
    public int switches() { return switches; }
    public double blend() { return blend; }
    public boolean transitioning() { return blend < 1.0D && previous != active; }

    private static int severity(NetworkCondition condition) {
        return switch (condition) {
            case STABLE -> 0;
            case DELAYED -> 1;
            case UNSTABLE -> 2;
        };
    }
}
