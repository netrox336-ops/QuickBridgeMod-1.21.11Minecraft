package dev.netrox.quickbridge;

public enum RouteCorrectionMode {
    GENTLE("Мягко", 0.18D, 0.72D, 0.46D, 0.24D, 0.48D),
    NORMAL("Нормально", 0.14D, 0.62D, 0.62D, 0.34D, 0.66D),
    STRONG("Сильно", 0.10D, 0.54D, 0.80D, 0.44D, 0.84D);

    private final String displayName;
    private final double softLimit;
    private final double hardLimit;
    private final double gain;
    private final double maxCorrection;
    private final double plannerPenalty;

    RouteCorrectionMode(
        String displayName,
        double softLimit,
        double hardLimit,
        double gain,
        double maxCorrection,
        double plannerPenalty
    ) {
        this.displayName = displayName;
        this.softLimit = softLimit;
        this.hardLimit = hardLimit;
        this.gain = gain;
        this.maxCorrection = maxCorrection;
        this.plannerPenalty = plannerPenalty;
    }

    public String displayName() { return displayName; }
    public double softLimit() { return softLimit; }
    public double hardLimit() { return hardLimit; }
    public double gain() { return gain; }
    public double maxCorrection() { return maxCorrection; }
    public double plannerPenalty() { return plannerPenalty; }

    public RouteCorrectionMode next() {
        RouteCorrectionMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
