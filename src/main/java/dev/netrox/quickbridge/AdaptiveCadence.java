package dev.netrox.quickbridge;

import net.minecraft.client.player.LocalPlayer;

public final class AdaptiveCadence {
    private AdaptiveCadence() {}

    public static Sample sample(
        LocalPlayer player,
        BridgeTechnique technique,
        TechniqueTuning tuning,
        int confirmed,
        int failed,
        double averageConfirmationTicks,
        int confirmationBudget
    ) {
        double speed = horizontalSpeed(player);
        double reference = referenceSpeed(technique);
        double speedRatio = clamp(speed / Math.max(0.05D, reference), 0.55D, 1.55D);

        int total = confirmed + failed;
        double reliability = total < 3 ? 1.0D : confirmed / (double) total;
        double latencyRatio = averageConfirmationTicks <= 0.0D
            ? 0.0D
            : clamp(averageConfirmationTicks / Math.max(1.0D, confirmationBudget), 0.0D, 1.6D);

        double factor = 0.78D + speedRatio * 0.22D;
        if (reliability < 0.92D) factor -= (0.92D - reliability) * 0.55D;
        if (latencyRatio > 0.65D) factor -= (latencyRatio - 0.65D) * 0.18D;
        factor += tuning.cadenceBias();
        factor = clamp(factor, 0.62D, 1.35D);

        return new Sample(speed, factor, reliability, latencyRatio);
    }

    private static double horizontalSpeed(LocalPlayer player) {
        if (player == null) return 0.0D;
        double x = player.getDeltaMovement().x;
        double z = player.getDeltaMovement().z;
        return Math.sqrt(x * x + z * z);
    }

    private static double referenceSpeed(BridgeTechnique technique) {
        return switch (technique) {
            case TELLY, SPEED_TELLY, BLINK_BRIDGE -> 0.28D;
            case ANDROMEDA, GOD_BRIDGE, JUMP_GOD -> 0.23D;
            case BREEZILY, WITCHLY, MOONWALK -> 0.20D;
            case SIDE_BRIDGE, DIAGONAL_NINJA -> 0.18D;
            default -> 0.16D;
        };
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public record Sample(double speed, double cadenceFactor, double reliability, double latencyRatio) {}
}
