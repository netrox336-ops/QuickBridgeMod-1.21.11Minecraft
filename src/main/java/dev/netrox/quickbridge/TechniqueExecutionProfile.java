package dev.netrox.quickbridge;

import net.minecraft.client.player.LocalPlayer;

public record TechniqueExecutionProfile(
    double runupEnd,
    double jumpEnd,
    double turnEnd,
    double burstEnd,
    double placementStart,
    double placementEnd,
    double minBurstSpeed,
    float yawTolerance,
    float pitchTolerance,
    double recoveryProgress,
    boolean rotationGate,
    boolean motionGate,
    boolean preferAirborneBurst
) {
    public static TechniqueExecutionProfile forTechnique(BridgeTechnique technique) {
        return switch (technique) {
            case TELLY -> new TechniqueExecutionProfile(
                0.18D, 0.32D, 0.52D, 0.90D,
                0.55D, 0.88D, 0.205D,
                18.0F, 14.0F, 0.46D,
                true, true, true
            );
            case SPEED_TELLY -> new TechniqueExecutionProfile(
                0.15D, 0.28D, 0.46D, 0.91D,
                0.49D, 0.89D, 0.225D,
                16.0F, 13.0F, 0.42D,
                true, true, true
            );
            case BLINK_BRIDGE -> new TechniqueExecutionProfile(
                0.14D, 0.27D, 0.42D, 0.84D,
                0.44D, 0.82D, 0.215D,
                17.0F, 13.0F, 0.39D,
                true, true, true
            );
            case ANDROMEDA -> new TechniqueExecutionProfile(
                0.18D, 0.30D, 0.46D, 0.92D,
                0.49D, 0.90D, 0.205D,
                18.0F, 15.0F, 0.43D,
                true, true, true
            );
            case WITCHLY -> new TechniqueExecutionProfile(
                0.0D, 0.0D, 0.0D, 1.0D,
                0.06D, 0.96D, 0.145D,
                24.0F, 18.0F, 0.12D,
                true, false, false
            );
            case BREEZILY, MOONWALK -> new TechniqueExecutionProfile(
                0.0D, 0.0D, 0.0D, 1.0D,
                0.04D, 0.97D, 0.135D,
                28.0F, 20.0F, 0.10D,
                false, false, false
            );
            default -> new TechniqueExecutionProfile(
                0.0D, 0.0D, 0.0D, 1.0D,
                0.0D, 1.0D, 0.0D,
                40.0F, 30.0F, 0.0D,
                false, false, false
            );
        };
    }

    public boolean complexPhases() {
        return runupEnd > 0.0D && jumpEnd > runupEnd && turnEnd > jumpEnd;
    }

    public TechniquePhase phase(double progress) {
        if (!complexPhases()) return TechniquePhase.CRUISE;
        if (progress < runupEnd) return TechniquePhase.RUNUP;
        if (progress < jumpEnd) return TechniquePhase.JUMP;
        if (progress < turnEnd) return TechniquePhase.TURN;
        if (progress < burstEnd) return TechniquePhase.BURST;
        return TechniquePhase.RESET;
    }

    public boolean placementWindow(double progress, TechniquePhase phase) {
        if (complexPhases() && phase != TechniquePhase.BURST) return false;
        return progress >= placementStart && progress <= placementEnd;
    }

    public double progressRate(LocalPlayer player, TechniquePhase phase) {
        if (player == null || !complexPhases()) return 1.0D;
        if (phase == TechniquePhase.JUMP && player.onGround()) return 0.72D;
        if (phase == TechniquePhase.TURN && preferAirborneBurst && player.onGround()) return 0.84D;
        return 1.0D;
    }

    public double motionScore(LocalPlayer player, TechniquePhase phase, double horizontalSpeed) {
        if (player == null || !motionGate) return 1.0D;
        double score = 1.0D;

        if (minBurstSpeed > 0.0D && horizontalSpeed < minBurstSpeed) {
            double ratio = horizontalSpeed / minBurstSpeed;
            score -= Math.max(0.0D, 1.0D - ratio) * 0.55D;
        }

        if (phase == TechniquePhase.JUMP && player.onGround()) {
            score -= 0.22D;
        }
        if (preferAirborneBurst && (phase == TechniquePhase.TURN || phase == TechniquePhase.BURST) && player.onGround()) {
            score -= 0.18D;
        }

        double vertical = player.getDeltaMovement().y;
        if (phase == TechniquePhase.BURST && vertical > 0.42D) {
            score -= 0.12D;
        }

        return clamp(score, 0.0D, 1.0D);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
