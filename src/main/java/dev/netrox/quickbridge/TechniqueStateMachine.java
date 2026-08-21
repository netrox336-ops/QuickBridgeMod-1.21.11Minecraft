package dev.netrox.quickbridge;

import net.minecraft.client.player.LocalPlayer;

public final class TechniqueStateMachine {
    private double cycleProgress;
    private double placementAccumulator;
    private int recoveryHoldTicks;
    private TechniquePhase previousPhase = TechniquePhase.IDLE;
    private Snapshot lastSnapshot = Snapshot.idle();

    public void reset() {
        cycleProgress = 0.0D;
        placementAccumulator = 0.0D;
        recoveryHoldTicks = 0;
        previousPhase = TechniquePhase.IDLE;
        lastSnapshot = Snapshot.idle();
    }

    public Snapshot tick(
        LocalPlayer player,
        BridgeTechnique technique,
        TechniqueTuning tuning,
        int engineTick,
        int confirmedPlacements,
        int failedPlacements,
        double averageConfirmationTicks,
        int confirmationBudget,
        boolean recoveryTriggered
    ) {
        AdaptiveCadence.Sample cadence = AdaptiveCadence.sample(
            player,
            technique,
            tuning,
            confirmedPlacements,
            failedPlacements,
            averageConfirmationTicks,
            confirmationBudget
        );

        if (recoveryTriggered) recoveryHoldTicks = Math.max(recoveryHoldTicks, 2);
        if (recoveryHoldTicks > 0) {
            recoveryHoldTicks--;
            previousPhase = TechniquePhase.RECOVERY;
            lastSnapshot = new Snapshot(
                TechniquePhase.RECOVERY,
                false,
                false,
                0,
                cadence.speed(),
                cadence.cadenceFactor(),
                cadence.reliability(),
                cycleProgress,
                true
            );
            return lastSnapshot;
        }

        double effectiveCycle = Math.max(2.0D, technique.cycleTicks() * tuning.cycleScale());
        cycleProgress += cadence.cadenceFactor() / effectiveCycle;
        if (cycleProgress >= 1.0D) cycleProgress -= Math.floor(cycleProgress);

        TechniquePhase phase = phaseFor(technique, cycleProgress);
        int strafeSign = strafeSign(phase, cycleProgress);
        boolean jumpPulse = shouldJump(technique, phase, previousPhase, engineTick);

        placementAccumulator += cadence.cadenceFactor();
        double interval = Math.max(1.0D, technique.placeEveryTicks());
        boolean placementPhase = isPlacementPhase(technique, phase);
        boolean placeNow = placementPhase && placementAccumulator >= interval;
        if (placeNow) placementAccumulator -= interval;

        previousPhase = phase;
        lastSnapshot = new Snapshot(
            phase,
            placeNow,
            jumpPulse,
            strafeSign,
            cadence.speed(),
            cadence.cadenceFactor(),
            cadence.reliability(),
            cycleProgress,
            false
        );
        return lastSnapshot;
    }

    public Snapshot lastSnapshot() {
        return lastSnapshot;
    }

    private static TechniquePhase phaseFor(BridgeTechnique technique, double progress) {
        return switch (technique) {
            case TELLY, SPEED_TELLY -> {
                if (progress < 0.18D) yield TechniquePhase.RUNUP;
                if (progress < 0.32D) yield TechniquePhase.JUMP;
                if (progress < 0.52D) yield TechniquePhase.TURN;
                if (progress < 0.90D) yield TechniquePhase.BURST;
                yield TechniquePhase.RESET;
            }
            case BLINK_BRIDGE -> {
                if (progress < 0.14D) yield TechniquePhase.RUNUP;
                if (progress < 0.27D) yield TechniquePhase.JUMP;
                if (progress < 0.42D) yield TechniquePhase.TURN;
                if (progress < 0.84D) yield TechniquePhase.BURST;
                yield TechniquePhase.RESET;
            }
            case ANDROMEDA -> {
                if (progress < 0.18D) yield TechniquePhase.RUNUP;
                if (progress < 0.30D) yield TechniquePhase.JUMP;
                if (progress < 0.46D) yield TechniquePhase.TURN;
                if (progress < 0.92D) yield TechniquePhase.BURST;
                yield TechniquePhase.RESET;
            }
            case BREEZILY, WITCHLY, MOONWALK -> progress < 0.50D
                ? TechniquePhase.STRAFE_A
                : TechniquePhase.STRAFE_B;
            default -> TechniquePhase.CRUISE;
        };
    }

    private static boolean isPlacementPhase(BridgeTechnique technique, TechniquePhase phase) {
        return switch (technique) {
            case TELLY, SPEED_TELLY, BLINK_BRIDGE, ANDROMEDA -> phase == TechniquePhase.BURST;
            default -> phase != TechniquePhase.RESET && phase != TechniquePhase.JUMP && phase != TechniquePhase.TURN;
        };
    }

    private static boolean shouldJump(
        BridgeTechnique technique,
        TechniquePhase phase,
        TechniquePhase previous,
        int engineTick
    ) {
        if (technique == BridgeTechnique.TELLY
            || technique == BridgeTechnique.SPEED_TELLY
            || technique == BridgeTechnique.BLINK_BRIDGE
            || technique == BridgeTechnique.ANDROMEDA) {
            return phase == TechniquePhase.JUMP && previous != TechniquePhase.JUMP;
        }
        return technique.jumpEveryTicks() > 0 && engineTick % technique.jumpEveryTicks() == 0;
    }

    private static int strafeSign(TechniquePhase phase, double progress) {
        if (phase == TechniquePhase.STRAFE_A) return -1;
        if (phase == TechniquePhase.STRAFE_B) return 1;
        return progress < 0.5D ? -1 : 1;
    }

    public record Snapshot(
        TechniquePhase phase,
        boolean placeNow,
        boolean jumpPulse,
        int strafeSign,
        double horizontalSpeed,
        double cadenceFactor,
        double reliability,
        double cycleProgress,
        boolean pauseMovement
    ) {
        private static Snapshot idle() {
            return new Snapshot(
                TechniquePhase.IDLE,
                false,
                false,
                0,
                0.0D,
                1.0D,
                1.0D,
                0.0D,
                false
            );
        }
    }
}
