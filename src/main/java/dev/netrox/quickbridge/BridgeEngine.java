package dev.netrox.quickbridge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;

public final class BridgeEngine {
    private static boolean active;
    private static int ticks;
    private static int placementAttempts;
    private static int originalSlot = -1;
    private static float startYaw;

    private BridgeEngine() {}

    public static boolean active() { return active; }
    public static int placementAttempts() { return placementAttempts; }

    public static void start(Minecraft minecraft) {
        if (active || minecraft.player == null) return;
        active = true;
        ticks = 0;
        placementAttempts = 0;
        originalSlot = minecraft.player.getInventory().getSelectedSlot();
        startYaw = minecraft.player.getYRot();
    }

    public static void stop(Minecraft minecraft) {
        if (!active) return;
        active = false;
        releaseMovement(minecraft);
        if (minecraft.player != null && originalSlot >= 0 && originalSlot < 9) {
            minecraft.player.getInventory().setSelectedSlot(originalSlot);
        }
        originalSlot = -1;
    }

    public static void tick(Minecraft minecraft, BridgeConfig config) {
        if (!active) return;
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || minecraft.gameMode == null) {
            stop(minecraft);
            return;
        }
        if (config.stopOnGui() && minecraft.screen != null) {
            stop(minecraft);
            return;
        }
        if (config.stopOnFall() && !player.onGround() && player.getDeltaMovement().y < -0.65D) {
            stop(minecraft);
            return;
        }
        if (!PlacementHelper.ensureBlockSelected(minecraft, config.autoSelectBlocks())) {
            stop(minecraft);
            return;
        }

        ticks++;
        BridgeTechnique technique = config.technique();
        applyMovement(minecraft, technique, config);

        if (technique.placeEveryTicks() > 0 && ticks % technique.placeEveryTicks() == 0) {
            int attempts = 1 + technique.extraPlacementAttempts();
            for (int i = 0; i < attempts; i++) {
                if (placeNext(minecraft, technique, i)) placementAttempts++;
            }
        }
    }

    private static void applyMovement(Minecraft minecraft, BridgeTechnique technique, BridgeConfig config) {
        releaseMovement(minecraft);
        int phase = (ticks / 3) & 1;

        switch (technique.movementStyle()) {
            case BACKWARD -> minecraft.options.keyDown.setDown(true);
            case BACKWARD_RIGHT -> {
                minecraft.options.keyDown.setDown(true);
                minecraft.options.keyRight.setDown(true);
            }
            case BACKWARD_ALTERNATE -> {
                minecraft.options.keyDown.setDown(true);
                if (phase == 0) minecraft.options.keyLeft.setDown(true);
                else minecraft.options.keyRight.setDown(true);
            }
            case FORWARD -> {
                minecraft.options.keyUp.setDown(true);
                minecraft.options.keySprint.setDown(true);
            }
            case SIDE_RIGHT -> minecraft.options.keyRight.setDown(true);
            case SIDE_LEFT -> minecraft.options.keyLeft.setDown(true);
            case DIAGONAL_BACKWARD -> {
                minecraft.options.keyDown.setDown(true);
                minecraft.options.keyLeft.setDown(true);
            }
        }

        if (technique.jumpEveryTicks() > 0 && ticks % technique.jumpEveryTicks() == 0) {
            minecraft.options.keyJump.setDown(true);
        }

        if (config.sneakAssist() && shouldSneak(minecraft.player, technique)) {
            minecraft.options.keyShift.setDown(true);
        }
    }

    private static boolean shouldSneak(LocalPlayer player, BridgeTechnique technique) {
        if (technique == BridgeTechnique.NINJA || technique == BridgeTechnique.DIAGONAL_NINJA || technique == BridgeTechnique.SLOPE_BRIDGE) {
            double fx = player.getX() - Math.floor(player.getX());
            double fz = player.getZ() - Math.floor(player.getZ());
            double edge = Math.min(Math.min(fx, 1.0D - fx), Math.min(fz, 1.0D - fz));
            return edge < 0.18D;
        }
        return false;
    }

    private static boolean placeNext(Minecraft minecraft, BridgeTechnique technique, int extraIndex) {
        LocalPlayer player = minecraft.player;
        if (player == null) return false;

        double[] move = movementVector(technique.movementStyle(), startYaw, ticks);
        double distance = 0.35D + extraIndex * 0.55D;
        int y = (int)Math.floor(player.getY() - 1.0D);

        BlockPos[] candidates = new BlockPos[] {
            BlockPos.containing(player.getX() + move[0] * distance, y, player.getZ() + move[1] * distance),
            BlockPos.containing(player.getX() + move[0] * (distance + 0.45D), y, player.getZ() + move[1] * (distance + 0.45D)),
            BlockPos.containing(player.getX(), y, player.getZ())
        };

        for (BlockPos target : candidates) {
            if (PlacementHelper.tryPlace(minecraft, target)) return true;
        }
        return false;
    }

    private static double[] movementVector(MovementStyle style, float yaw, int tick) {
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
            case BACKWARD_ALTERNATE -> {
                double side = ((tick / 3) & 1) == 0 ? -0.6D : 0.6D;
                x = -forwardX + rightX * side;
                z = -forwardZ + rightZ * side;
            }
            case DIAGONAL_BACKWARD -> { x = -forwardX - rightX * 0.75D; z = -forwardZ - rightZ * 0.75D; }
            default -> { x = -forwardX; z = -forwardZ; }
        }
        double length = Math.sqrt(x * x + z * z);
        if (length < 0.0001D) return new double[] {0.0D, 0.0D};
        return new double[] {x / length, z / length};
    }

    private static void releaseMovement(Minecraft minecraft) {
        minecraft.options.keyUp.setDown(false);
        minecraft.options.keyDown.setDown(false);
        minecraft.options.keyLeft.setDown(false);
        minecraft.options.keyRight.setDown(false);
        minecraft.options.keyJump.setDown(false);
        minecraft.options.keyShift.setDown(false);
        minecraft.options.keySprint.setDown(false);
    }
}
