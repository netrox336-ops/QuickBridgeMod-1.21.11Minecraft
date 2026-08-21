package dev.netrox.quickbridge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class BridgeEngine {
    private static final List<PendingPlacement> PENDING = new ArrayList<>();

    private static boolean active;
    private static int ticks;
    private static int placementAttempts;
    private static int confirmedPlacements;
    private static int failedPlacements;
    private static int recoveryAttempts;
    private static int consecutiveFailures;
    private static int originalSlot = -1;
    private static float startYaw;
    private static float startPitch;
    private static double edgeDistance = 0.90D;
    private static String phase = "OFF";
    private static String lastStopReason = "-";

    private BridgeEngine() {}

    public static boolean active() { return active; }
    public static int placementAttempts() { return placementAttempts; }
    public static int confirmedPlacements() { return confirmedPlacements; }
    public static int failedPlacements() { return failedPlacements; }
    public static int recoveryAttempts() { return recoveryAttempts; }
    public static int pendingPlacements() { return PENDING.size(); }
    public static double edgeDistance() { return edgeDistance; }
    public static String phase() { return phase; }
    public static String lastStopReason() { return lastStopReason; }

    public static void start(Minecraft minecraft) {
        if (active || minecraft.player == null) return;
        LocalPlayer player = minecraft.player;
        active = true;
        ticks = 0;
        placementAttempts = 0;
        confirmedPlacements = 0;
        failedPlacements = 0;
        recoveryAttempts = 0;
        consecutiveFailures = 0;
        edgeDistance = 0.90D;
        PENDING.clear();
        originalSlot = player.getInventory().getSelectedSlot();
        startYaw = player.getYRot();
        startPitch = player.getXRot();
        phase = "RUN";
        lastStopReason = "-";
        RotationEngine.begin(player);
    }

    public static void stop(Minecraft minecraft) {
        stopInternal(minecraft, "MANUAL");
    }

    private static void stopInternal(Minecraft minecraft, String reason) {
        if (!active) return;
        active = false;
        releaseMovement(minecraft);

        BridgeConfig config = BridgeConfig.get();
        if (minecraft.player != null) {
            if (originalSlot >= 0 && originalSlot < 9) {
                minecraft.player.getInventory().setSelectedSlot(originalSlot);
            }
            if (config.restoreView()) RotationEngine.restore(minecraft.player);
        }

        RotationEngine.clear();
        PENDING.clear();
        originalSlot = -1;
        phase = "OFF";
        lastStopReason = reason;
    }

    public static void tick(Minecraft minecraft, BridgeConfig config) {
        if (!active) return;
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || minecraft.gameMode == null) {
            stopInternal(minecraft, "WORLD");
            return;
        }
        if (config.stopOnGui() && minecraft.screen != null) {
            stopInternal(minecraft, "GUI");
            return;
        }
        if (config.stopOnFall() && !player.onGround() && player.getDeltaMovement().y < -0.65D) {
            stopInternal(minecraft, "FALL");
            return;
        }
        if (!PlacementHelper.ensureBlockSelected(minecraft, config.autoSelectBlocks())) {
            stopInternal(minecraft, "NO BLOCKS");
            return;
        }

        boolean recovering = updatePending(minecraft, config);
        if (!active) return;

        ticks++;
        BridgeTechnique technique = config.technique();
        double[] move = movementVector(technique.movementStyle(), startYaw, ticks);

        RotationEngine.tick(player, technique, startYaw, startPitch, ticks);
        applyWorldMovement(minecraft, player, technique, move);

        EdgeDetector.EdgeState edge = EdgeDetector.inspect(
            minecraft,
            player,
            move[0],
            move[1],
            technique.edgeSneakThreshold()
        );
        edgeDistance = edge.distance();

        if (config.sneakAssist() && edge.shouldSneak()) {
            minecraft.options.keyShift.setDown(true);
        }

        if (technique.shouldJump(ticks)) {
            minecraft.options.keyJump.setDown(true);
        }

        if (recovering) phase = "RECOVERY";
        else if (edge.shouldSneak()) phase = "EDGE";
        else phase = technique.placementWindow(ticks) ? "PLACE" : "RUN";

        if (technique.shouldPlace(ticks)) {
            int attempts = 1 + technique.extraPlacementAttempts();
            for (int i = 0; i < attempts; i++) {
                PlacementHelper.PlacementAttempt attempt = placeNext(minecraft, technique, move, i);
                if (attempt == null) continue;
                placementAttempts++;
                trackPending(attempt.target());
            }
        }
    }

    private static boolean updatePending(Minecraft minecraft, BridgeConfig config) {
        boolean recoveredThisTick = false;
        Iterator<PendingPlacement> iterator = PENDING.iterator();

        while (iterator.hasNext()) {
            PendingPlacement pending = iterator.next();
            if (PlacementHelper.isPlaced(minecraft, pending.target)) {
                confirmedPlacements++;
                consecutiveFailures = 0;
                iterator.remove();
                continue;
            }

            pending.age++;
            if (pending.age < config.confirmationTicks()) continue;

            if (config.placementRecovery() && pending.retries < config.maxRecoveryAttempts()) {
                PlacementHelper.PlacementAttempt retry = PlacementHelper.tryPlace(minecraft, pending.target);
                pending.retries++;
                pending.age = 0;
                if (retry != null) {
                    placementAttempts++;
                    recoveryAttempts++;
                    recoveredThisTick = true;
                }
                continue;
            }

            failedPlacements++;
            consecutiveFailures++;
            iterator.remove();
        }

        if (config.stopOnPlacementFailures() && consecutiveFailures >= config.maxConsecutiveFailures()) {
            stopInternal(minecraft, "PLACEMENT FAIL");
        }
        return recoveredThisTick;
    }

    private static void trackPending(BlockPos target) {
        for (PendingPlacement pending : PENDING) {
            if (pending.target.equals(target)) return;
        }
        PENDING.add(new PendingPlacement(target));
    }

    private static PlacementHelper.PlacementAttempt placeNext(
        Minecraft minecraft,
        BridgeTechnique technique,
        double[] move,
        int extraIndex
    ) {
        LocalPlayer player = minecraft.player;
        if (player == null) return null;

        double distance = technique.placementLead() + extraIndex * 0.50D;
        int y = (int)Math.floor(player.getY() - 1.0D);

        BlockPos[] candidates = new BlockPos[] {
            BlockPos.containing(player.getX() + move[0] * distance, y, player.getZ() + move[1] * distance),
            BlockPos.containing(player.getX() + move[0] * (distance + 0.38D), y, player.getZ() + move[1] * (distance + 0.38D)),
            BlockPos.containing(player.getX(), y, player.getZ())
        };

        for (BlockPos target : candidates) {
            if (hasPending(target)) continue;
            PlacementHelper.PlacementAttempt attempt = PlacementHelper.tryPlace(minecraft, target);
            if (attempt != null) return attempt;
        }
        return null;
    }

    private static boolean hasPending(BlockPos target) {
        for (PendingPlacement pending : PENDING) {
            if (pending.target.equals(target)) return true;
        }
        return false;
    }

    private static void applyWorldMovement(
        Minecraft minecraft,
        LocalPlayer player,
        BridgeTechnique technique,
        double[] desiredMove
    ) {
        releaseMovement(minecraft);

        double rad = Math.toRadians(player.getYRot());
        double cameraForwardX = -Math.sin(rad);
        double cameraForwardZ = Math.cos(rad);
        double cameraRightX = cameraForwardZ;
        double cameraRightZ = -cameraForwardX;

        double forward = desiredMove[0] * cameraForwardX + desiredMove[1] * cameraForwardZ;
        double right = desiredMove[0] * cameraRightX + desiredMove[1] * cameraRightZ;
        double threshold = 0.24D;

        if (forward > threshold) minecraft.options.keyUp.setDown(true);
        else if (forward < -threshold) minecraft.options.keyDown.setDown(true);

        if (right > threshold) minecraft.options.keyRight.setDown(true);
        else if (right < -threshold) minecraft.options.keyLeft.setDown(true);

        if (technique.sprint() && forward > threshold) {
            minecraft.options.keySprint.setDown(true);
        }
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

    private static final class PendingPlacement {
        private final BlockPos target;
        private int age;
        private int retries;

        private PendingPlacement(BlockPos target) {
            this.target = target;
        }
    }
}
