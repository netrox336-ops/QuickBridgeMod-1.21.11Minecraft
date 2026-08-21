package dev.netrox.quickbridge;

public enum BridgeTechnique {
    NINJA("Ninja", MovementStyle.BACKWARD, 1, 0, 0, false, RotationMode.NONE, 6, 0, 0.38D, 72.0F, 12.0F, 0.20D, 0.0F),
    DIAGONAL_NINJA("Diagonal Ninja", MovementStyle.DIAGONAL_BACKWARD, 1, 0, 0, false, RotationMode.NONE, 6, 0, 0.42D, 73.0F, 12.0F, 0.18D, 0.0F),
    BREEZILY("Breezily", MovementStyle.BACKWARD_ALTERNATE, 1, 0, 0, false, RotationMode.OSCILLATING_BACKWARD, 6, 0, 0.40D, 76.0F, 14.0F, 0.13D, 12.0F),
    WITCHLY("Witchly", MovementStyle.BACKWARD_ALTERNATE, 1, 0, 0, false, RotationMode.OSCILLATING_BACKWARD, 6, 0, 0.44D, 78.0F, 18.0F, 0.12D, 26.0F),
    GOD_BRIDGE("God Bridge", MovementStyle.BACKWARD_RIGHT, 1, 8, 0, false, RotationMode.BACKWARD, 8, 0, 0.45D, 78.0F, 25.0F, 0.10D, 0.0F),
    JUMP_GOD("Jump God", MovementStyle.BACKWARD_RIGHT, 1, 5, 0, true, RotationMode.BACKWARD, 5, 0, 0.48D, 79.0F, 32.0F, 0.09D, 0.0F),
    TELLY("Telly", MovementStyle.FORWARD, 1, 7, 2, true, RotationMode.TELLY, 10, 5, 0.58D, 80.0F, 42.0F, 0.08D, 0.0F),
    SPEED_TELLY("Speed Telly", MovementStyle.FORWARD, 1, 5, 2, true, RotationMode.TELLY, 8, 4, 0.62D, 81.0F, 52.0F, 0.07D, 0.0F),
    SIDE_BRIDGE("Side Bridge", MovementStyle.SIDE_RIGHT, 1, 0, 0, false, RotationMode.SIDE_RIGHT, 6, 0, 0.40D, 76.0F, 18.0F, 0.14D, 0.0F),
    TIME_BRIDGE("Time Bridge", MovementStyle.BACKWARD, 2, 0, 0, true, RotationMode.BACKWARD, 8, 0, 0.46D, 78.0F, 24.0F, 0.12D, 0.0F),
    ANDROMEDA("Andromeda", MovementStyle.BACKWARD_RIGHT, 1, 4, 2, true, RotationMode.OSCILLATING_BACKWARD, 6, 0, 0.64D, 82.0F, 55.0F, 0.06D, 16.0F),
    BLINK_BRIDGE("Blink Bridge", MovementStyle.FORWARD, 1, 6, 2, true, RotationMode.TELLY, 8, 3, 0.66D, 82.0F, 60.0F, 0.06D, 0.0F),
    SLOPE_BRIDGE("Slope Bridging", MovementStyle.BACKWARD, 1, 3, 0, false, RotationMode.BACKWARD, 6, 0, 0.42D, 68.0F, 24.0F, 0.16D, 0.0F),
    MOONWALK("Moonwalk", MovementStyle.BACKWARD_ALTERNATE, 1, 0, 0, true, RotationMode.OSCILLATING_BACKWARD, 6, 0, 0.43D, 78.0F, 20.0F, 0.11D, 20.0F);

    private final String displayName;
    private final MovementStyle movementStyle;
    private final int placeEveryTicks;
    private final int jumpEveryTicks;
    private final int extraPlacementAttempts;
    private final boolean experimental;
    private final RotationMode rotationMode;
    private final int cycleTicks;
    private final int placementStartTick;
    private final double placementLead;
    private final float placementPitch;
    private final float rotationStep;
    private final double edgeSneakThreshold;
    private final float oscillationDegrees;

    BridgeTechnique(
        String displayName,
        MovementStyle movementStyle,
        int placeEveryTicks,
        int jumpEveryTicks,
        int extraPlacementAttempts,
        boolean experimental,
        RotationMode rotationMode,
        int cycleTicks,
        int placementStartTick,
        double placementLead,
        float placementPitch,
        float rotationStep,
        double edgeSneakThreshold,
        float oscillationDegrees
    ) {
        this.displayName = displayName;
        this.movementStyle = movementStyle;
        this.placeEveryTicks = placeEveryTicks;
        this.jumpEveryTicks = jumpEveryTicks;
        this.extraPlacementAttempts = extraPlacementAttempts;
        this.experimental = experimental;
        this.rotationMode = rotationMode;
        this.cycleTicks = Math.max(1, cycleTicks);
        this.placementStartTick = Math.max(0, placementStartTick);
        this.placementLead = placementLead;
        this.placementPitch = placementPitch;
        this.rotationStep = rotationStep;
        this.edgeSneakThreshold = edgeSneakThreshold;
        this.oscillationDegrees = oscillationDegrees;
    }

    public String displayName() { return displayName; }
    public MovementStyle movementStyle() { return movementStyle; }
    public int placeEveryTicks() { return placeEveryTicks; }
    public int jumpEveryTicks() { return jumpEveryTicks; }
    public int extraPlacementAttempts() { return extraPlacementAttempts; }
    public boolean experimental() { return experimental; }
    public RotationMode rotationMode() { return rotationMode; }
    public int cycleTicks() { return cycleTicks; }
    public double placementLead() { return placementLead; }
    public float placementPitch() { return placementPitch; }
    public float rotationStep() { return rotationStep; }
    public double edgeSneakThreshold() { return edgeSneakThreshold; }
    public float oscillationDegrees() { return oscillationDegrees; }

    public int phaseTick(int tick) {
        return Math.floorMod(tick, cycleTicks);
    }

    public boolean placementWindow(int tick) {
        if (rotationMode != RotationMode.TELLY) return true;
        return phaseTick(tick) >= Math.min(placementStartTick, cycleTicks - 1);
    }

    public boolean shouldPlace(int tick) {
        return placeEveryTicks > 0 && tick % placeEveryTicks == 0 && placementWindow(tick);
    }

    public boolean shouldJump(int tick) {
        if (jumpEveryTicks <= 0) return false;
        if (rotationMode == RotationMode.TELLY) return phaseTick(tick) == Math.min(2, cycleTicks - 1);
        return tick % jumpEveryTicks == 0;
    }

    public boolean sprint() {
        return movementStyle == MovementStyle.FORWARD;
    }

    public BridgeTechnique next() {
        BridgeTechnique[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
