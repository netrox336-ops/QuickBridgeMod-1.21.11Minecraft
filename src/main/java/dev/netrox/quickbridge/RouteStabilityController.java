package dev.netrox.quickbridge;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;

public final class RouteStabilityController {
    private boolean initialized;
    private BridgeTechnique technique;
    private double originX;
    private double originZ;
    private double dirX;
    private double dirZ;
    private double sideX;
    private double sideZ;
    private double smoothedDrift;
    private double correction;
    private double progress;
    private double lastPlacementDrift;
    private double placementSpreadEma;
    private int confirmedSamples;
    private int reanchors;
    private Snapshot lastSnapshot = Snapshot.idle();

    public void reset() {
        initialized = false;
        technique = null;
        originX = 0.0D;
        originZ = 0.0D;
        dirX = 0.0D;
        dirZ = 1.0D;
        sideX = 1.0D;
        sideZ = 0.0D;
        smoothedDrift = 0.0D;
        correction = 0.0D;
        progress = 0.0D;
        lastPlacementDrift = 0.0D;
        placementSpreadEma = 0.0D;
        confirmedSamples = 0;
        reanchors = 0;
        lastSnapshot = Snapshot.idle();
    }

    public void begin(LocalPlayer player, BridgeTechnique bridgeTechnique, float startYaw) {
        if (player == null || bridgeTechnique == null) return;
        double[] axis = axisFor(bridgeTechnique.movementStyle(), startYaw);
        originX = player.getX();
        originZ = player.getZ();
        dirX = axis[0];
        dirZ = axis[1];
        sideX = dirZ;
        sideZ = -dirX;
        technique = bridgeTechnique;
        smoothedDrift = 0.0D;
        correction = 0.0D;
        progress = 0.0D;
        lastPlacementDrift = 0.0D;
        placementSpreadEma = 0.0D;
        confirmedSamples = 0;
        initialized = true;
        lastSnapshot = new Snapshot(
            dirX, dirZ, originX, originZ,
            0.0D, 0.0D, 0.0D, 0.0D,
            false, 0.0D, 0.0D, reanchors
        );
    }

    public Snapshot tick(
        LocalPlayer player,
        BridgeTechnique bridgeTechnique,
        float startYaw,
        double[] desiredMove,
        RouteCorrectionMode mode,
        boolean enabled
    ) {
        if (player == null || bridgeTechnique == null || desiredMove == null || desiredMove.length < 2) {
            lastSnapshot = Snapshot.idle();
            return lastSnapshot;
        }
        if (!initialized || technique != bridgeTechnique) {
            if (initialized) reanchors++;
            begin(player, bridgeTechnique, startYaw);
        }

        double dx = player.getX() - originX;
        double dz = player.getZ() - originZ;
        double rawDrift = dx * sideX + dz * sideZ;
        progress = dx * dirX + dz * dirZ;

        if (Math.abs(rawDrift) > 2.25D || progress < -1.75D) {
            reanchors++;
            begin(player, bridgeTechnique, startYaw);
            rawDrift = 0.0D;
            progress = 0.0D;
        }

        smoothedDrift = smoothedDrift * 0.78D + rawDrift * 0.22D;
        double alternatingScale = bridgeTechnique.movementStyle() == MovementStyle.BACKWARD_ALTERNATE ? 0.58D : 1.0D;
        double desiredCorrection = 0.0D;
        if (enabled && Math.abs(smoothedDrift) > mode.softLimit()) {
            double overflow = Math.abs(smoothedDrift) - mode.softLimit();
            desiredCorrection = -Math.copySign(
                Math.min(mode.maxCorrection(), overflow * mode.gain()) * alternatingScale,
                smoothedDrift
            );
        }
        correction = correction * 0.68D + desiredCorrection * 0.32D;

        double correctedX = desiredMove[0] + sideX * correction;
        double correctedZ = desiredMove[1] + sideZ * correction;
        double length = Math.sqrt(correctedX * correctedX + correctedZ * correctedZ);
        if (length > 0.0001D) {
            correctedX /= length;
            correctedZ /= length;
        }

        boolean hardDrift = enabled && Math.abs(smoothedDrift) >= mode.hardLimit();
        lastSnapshot = new Snapshot(
            correctedX,
            correctedZ,
            originX,
            originZ,
            rawDrift,
            smoothedDrift,
            correction,
            progress,
            hardDrift,
            lastPlacementDrift,
            placementSpreadEma,
            reanchors
        );
        return lastSnapshot;
    }

    public void observeConfirmed(BlockPos target) {
        if (!initialized || target == null) return;
        double centerX = target.getX() + 0.5D - originX;
        double centerZ = target.getZ() + 0.5D - originZ;
        double drift = centerX * sideX + centerZ * sideZ;
        lastPlacementDrift = drift;
        double absolute = Math.abs(drift);
        placementSpreadEma = confirmedSamples == 0
            ? absolute
            : placementSpreadEma * 0.84D + absolute * 0.16D;
        confirmedSamples++;
    }

    public Snapshot lastSnapshot() {
        return lastSnapshot;
    }

    private static double[] axisFor(MovementStyle style, float yaw) {
        double rad = Math.toRadians(yaw);
        double forwardX = -Math.sin(rad);
        double forwardZ = Math.cos(rad);
        double rightX = forwardZ;
        double rightZ = -forwardX;

        double x;
        double z;
        switch (style) {
            case FORWARD -> { x = forwardX; z = forwardZ; }
            case SIDE_RIGHT -> { x = rightX; z = rightZ; }
            case SIDE_LEFT -> { x = -rightX; z = -rightZ; }
            case BACKWARD_RIGHT -> { x = -forwardX + rightX * 0.65D; z = -forwardZ + rightZ * 0.65D; }
            case DIAGONAL_BACKWARD -> { x = -forwardX - rightX * 0.75D; z = -forwardZ - rightZ * 0.75D; }
            case BACKWARD_ALTERNATE, BACKWARD -> { x = -forwardX; z = -forwardZ; }
            default -> { x = -forwardX; z = -forwardZ; }
        }
        double length = Math.sqrt(x * x + z * z);
        if (length < 0.0001D) return new double[] {0.0D, 1.0D};
        return new double[] {x / length, z / length};
    }

    public record Snapshot(
        double moveX,
        double moveZ,
        double originX,
        double originZ,
        double rawDrift,
        double drift,
        double correction,
        double progress,
        boolean hardDrift,
        double lastPlacementDrift,
        double placementSpreadEma,
        int reanchors
    ) {
        public double routeError(BlockPos target) {
            if (target == null) return 0.0D;
            double dx = target.getX() + 0.5D - originX;
            double dz = target.getZ() + 0.5D - originZ;
            double length = Math.sqrt(moveX * moveX + moveZ * moveZ);
            if (length < 0.0001D) return 0.0D;
            double axisX = moveX / length;
            double axisZ = moveZ / length;
            double laneX = axisZ;
            double laneZ = -axisX;
            return Math.abs(dx * laneX + dz * laneZ);
        }

        private static Snapshot idle() {
            return new Snapshot(
                0.0D, 1.0D, 0.0D, 0.0D,
                0.0D, 0.0D, 0.0D, 0.0D,
                false, 0.0D, 0.0D, 0
            );
        }
    }
}
