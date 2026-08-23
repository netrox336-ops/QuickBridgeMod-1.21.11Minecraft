package dev.netrox.quickbridge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;

public final class PathProbe {
    private static final double STEP = 0.20D;
    private static final double HORIZON = 2.40D;

    private PathProbe() {}

    public static Snapshot inspect(Minecraft minecraft, LocalPlayer player, double moveX, double moveZ) {
        if (minecraft.level == null || player == null) return Snapshot.empty();

        double length = Math.sqrt(moveX * moveX + moveZ * moveZ);
        if (length < 0.0001D) return Snapshot.empty();

        double dirX = moveX / length;
        double dirZ = moveZ / length;
        double supportY = player.getY() - 0.05D;
        double firstGap = HORIZON;
        double firstObstacle = HORIZON;
        int samples = 0;
        int supported = 0;
        int longestGap = 0;
        int currentGap = 0;

        for (double distance = STEP; distance <= HORIZON + 0.0001D; distance += STEP) {
            double x = player.getX() + dirX * distance;
            double z = player.getZ() + dirZ * distance;
            BlockPos support = BlockPos.containing(x, supportY, z);
            BlockPos feet = BlockPos.containing(x, player.getY() + 0.10D, z);
            BlockPos head = BlockPos.containing(x, player.getY() + 1.10D, z);

            samples++;
            if (minecraft.level.getBlockState(support).isAir()) {
                if (firstGap >= HORIZON) firstGap = distance;
                currentGap++;
                longestGap = Math.max(longestGap, currentGap);
            } else {
                supported++;
                currentGap = 0;
            }

            if (firstObstacle >= HORIZON
                && (!minecraft.level.getBlockState(feet).isAir() || !minecraft.level.getBlockState(head).isAir())) {
                firstObstacle = distance;
            }
        }

        double supportRatio = samples == 0 ? 1.0D : supported / (double) samples;
        double gapLength = longestGap * STEP;
        boolean obstacleAhead = firstObstacle < 0.85D;
        boolean dangerousGap = firstGap < 0.42D && gapLength > 0.75D;
        return new Snapshot(firstGap, firstObstacle, supportRatio, gapLength, obstacleAhead, dangerousGap);
    }

    public record Snapshot(
        double firstGapDistance,
        double firstObstacleDistance,
        double supportRatio,
        double longestGapLength,
        boolean obstacleAhead,
        boolean dangerousGap
    ) {
        private static Snapshot empty() {
            return new Snapshot(HORIZON, HORIZON, 1.0D, 0.0D, false, false);
        }
    }
}
