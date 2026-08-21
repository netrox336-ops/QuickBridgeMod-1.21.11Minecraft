package dev.netrox.quickbridge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;

public final class EdgeDetector {
    private static final double MAX_SCAN = 0.90D;
    private static final double STEP = 0.05D;

    private EdgeDetector() {}

    public static EdgeState inspect(Minecraft minecraft, LocalPlayer player, double moveX, double moveZ, double sneakThreshold) {
        if (minecraft.level == null || player == null) return new EdgeState(MAX_SCAN, false, null);

        double length = Math.sqrt(moveX * moveX + moveZ * moveZ);
        if (length < 0.0001D) return new EdgeState(MAX_SCAN, false, null);

        double dirX = moveX / length;
        double dirZ = moveZ / length;
        double supportY = player.getY() - 0.05D;

        for (double distance = 0.0D; distance <= MAX_SCAN; distance += STEP) {
            BlockPos support = BlockPos.containing(
                player.getX() + dirX * distance,
                supportY,
                player.getZ() + dirZ * distance
            );
            if (minecraft.level.getBlockState(support).isAir()) {
                return new EdgeState(distance, distance <= sneakThreshold, support);
            }
        }

        return new EdgeState(MAX_SCAN, false, null);
    }

    public record EdgeState(double distance, boolean shouldSneak, BlockPos firstUnsupported) {}
}
