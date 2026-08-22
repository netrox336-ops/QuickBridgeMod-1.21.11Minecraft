package dev.netrox.quickbridge;

import net.minecraft.client.player.LocalPlayer;

public final class TechniqueStateMachine {
    private double cycleProgress;
    private double placementAccumulator;
    private TechniquePhase previousPhase = TechniquePhase.IDLE;
    private Snapshot lastSnapshot = Snapshot.idle();

    public void reset() {
        cycleProgress = 0.0D;
        placementAccumulator = 0.0D;
        previousPhase = TechniquePhase.IDLE;
        lastSnapshot = Snapshot.idle();
        ExecutionDiagnostics.reset();
    }

    public Snapshot tick(
        LocalPlayer player,
        BridgeTechnique technique,
        TechniqueTuning tuning,
        int engineTick,
        int confirmedPlacements,
        int failedPlacements,
        double averageConfirmationTicks,
        int confirmationBudget
    ) {
        TechniqueExecutionProfile execution = TechniqueExecutionProfile.forTechnique(technique);

        if (ExecutionDiagnostics.consumePhaseResync(technique)) {
            cycleProgress = clampProgress(execution.recoveryProgress());
            placementAccumulator = 0.0D;
            previousPhase = phaseFor(technique, execution, cycleProgress);
        }

        AdaptiveCadence.Sample cadence = AdaptiveCadence.sample(
            player,
            technique,
            tuning,
            confirmedPlacements,
            failedPlacements,
            averageConfirmationTicks,
            confirmationBudget
        );

        double effectiveCycle = Math.max(2.0D, technique.cycleTicks() * tuning.cycleScale());
        TechniquePhase phaseBeforeAdvance = phaseFor(technique, execution, cycleProgress);
        double phaseRate = execution.progressRate(player, phaseBeforeAdvance);
        cycleProgress += (cadence.cadenceFactor() / effectiveCycle) * phaseRate;
        if (cycleProgress >= 1.0D) cycleProgress -= Math.floor(cycleProgress);

        TechniquePhase phase = phaseFor(technique, execution, cycleProgress);
        int strafeSign = strafeSign(phase, cycleProgress);
        boolean jumpPulse = shouldJump(technique, phase, previousPhase, engineTick);

        placementAccumulator += cadence.cadenceFactor();
        double interval = Math.max(1.0D, technique.placeEveryTicks());
        boolean placementPhase = isPlacementPhase(technique, phase);
        boolean windowOpen = placementPhase && execution.placementWindow(cycleProgress, phase);
        double motionScore = execution.motionScore(player, phase, cadence.speed());

        boolean guardEnabled = BridgeConfig.get().executionGuard();
        boolean rotationReady = !guardEnabled
            || !execution.rotationGate()
            || ExecutionDiagnostics.rotationAligned();
        boolean motionReady = !guardEnabled
            || !execution.motionGate()
            || motionScore >= 0.54D;
        boolean placementReady = windowOpen && rotationReady && motionReady;

        boolean placementDue = placementAccumulator >= interval;
        boolean placeNow = placementDue && placementReady;
        if (placementDue) {
            ExecutionDiagnostics.recordPlacementDecision(placeNow);
            if (placeNow) {
                placementAccumulator -= interval;
            } else {
                placementAccumulator = Math.min(placementAccumulator, interval * 1.35D);
            }
        }

        ExecutionDiagnostics.reportExecution(motionScore, windowOpen, placementReady);

        previousPhase = phase;
        lastSnapshot = new Snapshot(
            phase,
            placeNow,
            jumpPulse,
            strafeSign,
            cadence.speed(),
            cadence.cadenceFactor(),
            cadence.reliability(),
            cycleProgress
        );
        return lastSnapshot;
    }

    public Snapshot lastSnapshot() {
        return lastSnapshot;
    }

    private static TechniquePhase phaseFor(
        BridgeTechnique technique,
        TechniqueExecutionProfile execution,
        double progress
    ) {
        if (execution.complexPhases()) return execution.phase(progress);
        return switch (technique) {
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

    private static double clampProgress(double value) {
        if (value < 0.0D) return 0.0D;
        if (value >= 1.0D) return 0.999D;
        return value;
    }

    public record Snapshot(
        TechniquePhase phase,
        boolean placeNow,
        boolean jumpPulse,
        int strafeSign,
        double horizontalSpeed,
        double cadenceFactor,
        double reliability,
        double cycleProgress
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
                0.0D
            );
        }
    }
}
