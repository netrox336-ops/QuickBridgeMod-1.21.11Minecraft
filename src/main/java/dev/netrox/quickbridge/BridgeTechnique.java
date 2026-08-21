package dev.netrox.quickbridge;

public enum BridgeTechnique {
    NINJA("Ninja", MovementStyle.BACKWARD, 1, 0, 0, false),
    DIAGONAL_NINJA("Diagonal Ninja", MovementStyle.DIAGONAL_BACKWARD, 1, 0, 0, false),
    BREEZILY("Breezily", MovementStyle.BACKWARD_ALTERNATE, 1, 0, 0, false),
    WITCHLY("Witchly", MovementStyle.BACKWARD_ALTERNATE, 1, 0, 0, false),
    GOD_BRIDGE("God Bridge", MovementStyle.BACKWARD_RIGHT, 1, 8, 0, false),
    JUMP_GOD("Jump God", MovementStyle.BACKWARD_RIGHT, 1, 5, 0, true),
    TELLY("Telly", MovementStyle.FORWARD, 1, 7, 2, true),
    SPEED_TELLY("Speed Telly", MovementStyle.FORWARD, 1, 5, 2, true),
    SIDE_BRIDGE("Side Bridge", MovementStyle.SIDE_RIGHT, 1, 0, 0, false),
    TIME_BRIDGE("Time Bridge", MovementStyle.BACKWARD, 2, 0, 0, true),
    ANDROMEDA("Andromeda", MovementStyle.BACKWARD_RIGHT, 1, 4, 2, true),
    BLINK_BRIDGE("Blink Bridge", MovementStyle.BACKWARD, 1, 6, 2, true),
    SLOPE_BRIDGE("Slope Bridging", MovementStyle.BACKWARD, 1, 3, 0, false),
    MOONWALK("Moonwalk", MovementStyle.BACKWARD_ALTERNATE, 1, 0, 0, true);

    private final String displayName;
    private final MovementStyle movementStyle;
    private final int placeEveryTicks;
    private final int jumpEveryTicks;
    private final int extraPlacementAttempts;
    private final boolean experimental;

    BridgeTechnique(String displayName, MovementStyle movementStyle, int placeEveryTicks, int jumpEveryTicks,
                    int extraPlacementAttempts, boolean experimental) {
        this.displayName = displayName;
        this.movementStyle = movementStyle;
        this.placeEveryTicks = placeEveryTicks;
        this.jumpEveryTicks = jumpEveryTicks;
        this.extraPlacementAttempts = extraPlacementAttempts;
        this.experimental = experimental;
    }

    public String displayName() { return displayName; }
    public MovementStyle movementStyle() { return movementStyle; }
    public int placeEveryTicks() { return placeEveryTicks; }
    public int jumpEveryTicks() { return jumpEveryTicks; }
    public int extraPlacementAttempts() { return extraPlacementAttempts; }
    public boolean experimental() { return experimental; }

    public BridgeTechnique next() {
        BridgeTechnique[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
