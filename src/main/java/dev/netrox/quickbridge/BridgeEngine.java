package dev.netrox.quickbridge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class BridgeEngine {
    private static final List<PendingPlacement> PENDING = new ArrayList<>();
    private static final TechniqueStateMachine STATE_MACHINE = new TechniqueStateMachine();
    private static final RecoveryStateMachine RECOVERY = new RecoveryStateMachine();

    private static boolean active;
    private static int ticks;
    private static int placementAttempts;
    private static int confirmedPlacements;
    private static int failedPlacements;
    private static int recoveryAttempts;
    private static int consecutiveFailures;
    private static long confirmationAgeTotal;
    private static int confirmationAgeSamples;
    private static int originalSlot = -1;
    private static float startYaw;
    private static float startPitch;
    private static double edgeDistance = 0.90D;
    private static String phase = "OFF";
    private static String lastStopReason = "-";
    private static String currentServerId = "unknown";
    private static String currentServerLabel = "Unknown";

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
    public static double horizontalSpeed() { return STATE_MACHINE.lastSnapshot().horizontalSpeed(); }
    public static double cadenceFactor() { return STATE_MACHINE.lastSnapshot().cadenceFactor(); }
    public static double placementReliability() { return STATE_MACHINE.lastSnapshot().reliability(); }
    public static double cycleProgress() { return STATE_MACHINE.lastSnapshot().cycleProgress(); }
    public static String serverLabel() { return currentServerLabel; }

    public static double averageConfirmationTicks() {
        return confirmationAgeSamples == 0 ? 0.0D : confirmationAgeTotal / (double) confirmationAgeSamples;
    }

    public static LearningProfile learningProfile() {
        return LearningEngine.profile(currentServerId, BridgeConfig.get().technique());
    }

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
        confirmationAgeTotal = 0L;
        confirmationAgeSamples = 0;
        edgeDistance = 0.90D;
        PENDING.clear();
        STATE_MACHINE.reset();
        RECOVERY.clear();
        originalSlot = player.getInventory().getSelectedSlot();
        startYaw = player.getYRot();
        startPitch = player.getXRot();
        currentServerId = ServerContext.id(minecraft);
        currentServerLabel = ServerContext.label(minecraft);
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
        STATE_MACHINE.reset();
        RECOVERY.clear();
        PENDING.clear();
        LearningEngine.flush();
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

        BridgeTechnique technique = config.technique();
        updatePending(minecraft, config, technique);
        if (!active) return;

        RecoveryStateMachine.Snapshot recovery = RECOVERY.tick(player);
        if (recovery.retryNow() && recovery.target() != null) {
            PlacementHelper.PlacementAttempt retry = PlacementHelper.tryPlace(minecraft, recovery.target());
            resetPendingAge(recovery.target());
            if (retry != null) {
                placementAttempts++;
                recoveryAttempts++;
            }
        }
        if (recovery.pauseMovement()) {
            releaseMovement(minecraft);
            if (recovery.forceSneak()) minecraft.options.keyShift.setDown(true);
            phase = recovery.phase();
            return;
        }

        ticks++;
        TechniqueTuning tuning = LearningEngine.effectiveTuning(config, currentServerId, technique);
        TechniqueStateMachine.Snapshot state = STATE_MACHINE.tick(
            player,
            technique,
            tuning,
            ticks,
            confirmedPlacements,
            failedPlacements,
            averageConfirmationTicks(),
            config.confirmationTicks()
        );

        double[] move = movementVector(technique.movementStyle(), startYaw, state.strafeSign());
        RotationEngine.tick(player, technique, tuning, state, startYaw, startPitch);
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

        if (state.jumpPulse()) {
            minecraft.options.keyJump.setDown(true);
        }

        phase = edge.shouldSneak() && state.phase() == TechniquePhase.CRUISE
            ? "EDGE"
            : state.phase().displayName();

        if (state.placeNow()) {
            int attempts = 1 + technique.extraPlacementAttempts();
            for (int i = 0; i < attempts; i++) {
                PlacementHelper.PlacementAttempt attempt = placeNext(minecraft, technique, tuning, state, move, i);
                if (attempt == null) continue;
                placementAttempts++;
                trackPending(attempt.target());
            }
        }
    }

    private static void updatePending(Minecraft minecraft, BridgeConfig config, BridgeTechnique technique) {
        Iterator<PendingPlacement> iterator = PENDING.iterator();

        while (iterator.hasNext()) {
            PendingPlacement pending = iterator.next();
            if (PlacementHelper.isPlaced(minecraft, pending.target)) {
                confirmedPlacements++;
                confirmationAgeTotal += pending.age;
                confirmationAgeSamples++;
                consecutiveFailures = 0;
                boolean recovered = pending.retries > 0;
                LearningEngine.recordSuccess(
                    currentServerId,
                    technique,
                    pending.age,
                    config.confirmationTicks(),
                    recovered,
                    config.autoLearning()
                );
                RECOVERY.confirmed(pending.target);
                iterator.remove();
                continue;
            }

            pending.age++;
            if (pending.age < config.confirmationTicks()) continue;

            if (RECOVERY.activeFor(pending.target)) continue;

            if (config.placementRecovery() && pending.retries < config.maxRecoveryAttempts()) {
                if (!RECOVERY.active()) {
                    pending.retries++;
                    pending.age = 0;
                    RECOVERY.begin(pending.target);
                }
                continue;
            }

            if (RECOVERY.active()) continue;

            failedPlacements++;
            consecutiveFailures++;
            LearningEngine.recordFailure(currentServerId, technique, config.autoLearning());
            RECOVERY.cancel(pending.target);
            iterator.remove();
        }

        if (config.stopOnPlacementFailures() && consecutiveFailures >= config.maxConsecutiveFailures()) {
            stopInternal(minecraft, "PLACEMENT FAIL");
        }
    }

    private static void resetPendingAge(BlockPos target) {
        for (PendingPlacement pending : PENDING) {
            if (pending.target.equals(target)) {
                pending.age = 0;
                return;
            }
        }
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
        TechniqueTuning tuning,
        TechniqueStateMachine.Snapshot state,
        double[] move,
        int extraIndex
    ) {
        LocalPlayer player = minecraft.player;
        if (player == null) return null;

        double adaptiveLead = Math.min(0.14D, state.horizontalSpeed() * 0.35D);
        double distance = Math.max(
            0.18D,
            technique.placementLead() + tuning.leadOffset() + adaptiveLead + extraIndex * 0.50D
        );
        int y = (int) Math.floor(player.getY() - 1.0D);

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

    private static double[] movementVector(MovementStyle style, float yaw, int strafeSign) {
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
                double side = strafeSign * 0.6D;
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
