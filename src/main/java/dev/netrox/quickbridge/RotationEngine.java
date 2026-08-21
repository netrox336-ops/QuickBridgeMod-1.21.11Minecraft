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

    public static void tick(LocalPlayer player, BridgeTechnique technique, float startYaw, float startPitch, int ticks) {
        if (player == null || technique.rotationMode() == RotationMode.NONE) return;

        float desiredYaw = startYaw;
        float desiredPitch = startPitch;
        int phase = technique.phaseTick(ticks);

        switch (technique.rotationMode()) {
            case BACKWARD -> {
                desiredYaw = startYaw + 180.0F;
                desiredPitch = technique.placementPitch();
            }
            case OSCILLATING_BACKWARD -> {
                float side = phase < Math.max(1, technique.cycleTicks() / 2) ? -1.0F : 1.0F;
                desiredYaw = startYaw + 180.0F + side * technique.oscillationDegrees();
                desiredPitch = technique.placementPitch();
            }
            case TELLY -> {
                if (technique.placementWindow(ticks)) {
                    desiredYaw = startYaw + 180.0F;
                    desiredPitch = technique.placementPitch();
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
                return;
            }
        }

        player.setYRot(approachAngle(player.getYRot(), desiredYaw, technique.rotationStep()));
        player.setXRot(approachLinear(player.getXRot(), desiredPitch, Math.max(4.0F, technique.rotationStep() * 0.72F)));
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
