package dev.netrox.quickbridge;

import net.minecraft.client.player.LocalPlayer;

public final class RotationEngine {
    private static float originalYaw;
    private static float originalPitch;
    private static boolean captured;

    private RotationEngine() {}

    public static void begin(LocalPlayer player) {
        if (player == null) return;
        originalYaw = player.getYRot();
        originalPitch = player.getXRot();
        captured = true;
    }

    public static void tick(
        LocalPlayer player,
        BridgeTechnique technique,
        TechniqueTuning tuning,
        TechniqueStateMachine.Snapshot state,
        float startYaw,
        float startPitch
    ) {
        if (player == null) return;

        TechniqueExecutionProfile execution = TechniqueExecutionProfile.forTechnique(technique);
        if (technique.rotationMode() == RotationMode.NONE) {
            ExecutionDiagnostics.reportRotation(0.0D, 0.0D, execution.yawTolerance(), execution.pitchTolerance());
            return;
        }

        float desiredYaw = startYaw;
        float desiredPitch = startPitch;
        TechniquePhase phase = state.phase();

        switch (technique.rotationMode()) {
            case BACKWARD -> {
                desiredYaw = startYaw + 180.0F;
                desiredPitch = technique.placementPitch();
            }
            case OSCILLATING_BACKWARD -> {
                desiredYaw = startYaw + 180.0F + state.strafeSign() * technique.oscillationDegrees();
                desiredPitch = technique.placementPitch();
            }
            case TELLY -> {
                if (phase == TechniquePhase.TURN
                    || phase == TechniquePhase.BURST
                    || phase == TechniquePhase.RECOVERY) {
                    desiredYaw = startYaw + 180.0F;
                    desiredPitch = phase == TechniquePhase.RECOVERY
                        ? Math.max(84.0F, technique.placementPitch())
                        : technique.placementPitch();
                } else {
                    desiredYaw = startYaw;
                    desiredPitch = Math.min(25.0F, Math.max(-10.0F, startPitch));
                }
            }
            case SIDE_RIGHT -> {
                desiredYaw = startYaw + 90.0F;
                desiredPitch = technique.placementPitch();
            }
            case NONE -> {
                ExecutionDiagnostics.reportRotation(0.0D, 0.0D, execution.yawTolerance(), execution.pitchTolerance());
                return;
            }
        }

        float rotationStep = Math.max(4.0F, technique.rotationStep() * tuning.rotationScale());
        float nextYaw = approachAngle(player.getYRot(), desiredYaw, rotationStep);
        float nextPitch = approachLinear(player.getXRot(), desiredPitch, Math.max(4.0F, rotationStep * 0.72F));
        player.setYRot(nextYaw);
        player.setXRot(nextPitch);

        double yawError = Math.abs(wrapDegrees(desiredYaw - nextYaw));
        double pitchError = Math.abs(desiredPitch - nextPitch);
        ExecutionDiagnostics.reportRotation(
            yawError,
            pitchError,
            execution.yawTolerance(),
            execution.pitchTolerance()
        );
    }

    public static void restore(LocalPlayer player) {
        if (!captured || player == null) return;
        player.setYRot(originalYaw);
        player.setXRot(originalPitch);
    }

    public static void clear() {
        captured = false;
    }

    private static float approachAngle(float current, float target, float maxStep) {
        float delta = wrapDegrees(target - current);
        if (Math.abs(delta) <= maxStep) return current + delta;
        return current + Math.copySign(maxStep, delta);
    }

    private static float approachLinear(float current, float target, float maxStep) {
        float delta = target - current;
        if (Math.abs(delta) <= maxStep) return clampPitch(target);
        return clampPitch(current + Math.copySign(maxStep, delta));
    }

    private static float wrapDegrees(float value) {
        float wrapped = value % 360.0F;
        if (wrapped >= 180.0F) wrapped -= 360.0F;
        if (wrapped < -180.0F) wrapped += 360.0F;
        return wrapped;
    }

    private static float clampPitch(float value) {
        return Math.max(-90.0F, Math.min(90.0F, value));
    }
}
