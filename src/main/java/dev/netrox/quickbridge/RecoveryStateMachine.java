package dev.netrox.quickbridge;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public final class RecoveryStateMachine {
    private Stage stage = Stage.IDLE;
    private BlockPos target;
    private BridgeTechnique technique;
    private int stageTicks;

    public boolean active() {
        return stage != Stage.IDLE && target != null;
    }

    public boolean activeFor(BlockPos position) {
        return active() && target.equals(position);
    }

    public BlockPos target() {
        return target;
    }

    public boolean begin(BlockPos position) {
        return begin(position, BridgeConfig.get().technique());
    }

    public boolean begin(BlockPos position, BridgeTechnique bridgeTechnique) {
        if (position == null || active()) return false;
        target = position.immutable();
        technique = bridgeTechnique;
        stage = Stage.HOLD;
        stageTicks = 0;
        return true;
    }

    public Snapshot tick(LocalPlayer player) {
        if (!active()) return Snapshot.idle();

        stageTicks++;
        return switch (stage) {
            case HOLD -> {
                if (stageTicks >= holdTicks()) advance(Stage.REALIGN);
                yield new Snapshot("RECOVERY HOLD", true, true, false, target);
            }
            case REALIGN -> {
                if (player != null) alignView(player, target, complexTechnique());
                if (stageTicks >= alignTicks()) advance(Stage.RETRY);
                yield new Snapshot("RECOVERY ALIGN", true, true, false, target);
            }
            case RETRY -> {
                BlockPos retryTarget = target;
                advance(Stage.RESUME);
                yield new Snapshot("RECOVERY RETRY", true, true, true, retryTarget);
            }
            case RESUME -> {
                BlockPos resumeTarget = target;
                if (stageTicks >= resumeTicks()) finishResume();
                yield new Snapshot("RECOVERY RESUME", true, true, false, resumeTarget);
            }
            case IDLE -> Snapshot.idle();
        };
    }

    public void confirmed(BlockPos position) {
        if (!activeFor(position)) return;
        stage = Stage.RESUME;
        stageTicks = 0;
    }

    public void cancel(BlockPos position) {
        if (position == null || activeFor(position)) clear();
    }

    public void clear() {
        stage = Stage.IDLE;
        target = null;
        technique = null;
        stageTicks = 0;
    }

    private void finishResume() {
        BridgeTechnique resumeTechnique = technique;
        clear();
        if (resumeTechnique == null) return;
        TechniqueExecutionProfile profile = TechniqueExecutionProfile.forTechnique(resumeTechnique);
        if (profile.recoveryProgress() > 0.0D) {
            ExecutionDiagnostics.requestPhaseResync(resumeTechnique);
        }
    }

    private int holdTicks() {
        return complexTechnique() ? 2 : 1;
    }

    private int alignTicks() {
        return complexTechnique() ? 3 : 1;
    }

    private int resumeTicks() {
        return complexTechnique() ? 2 : 1;
    }

    private boolean complexTechnique() {
        return technique == BridgeTechnique.TELLY
            || technique == BridgeTechnique.SPEED_TELLY
            || technique == BridgeTechnique.ANDROMEDA
            || technique == BridgeTechnique.BLINK_BRIDGE;
    }

    private void advance(Stage next) {
        stage = next;
        stageTicks = 0;
    }

    private static void alignView(LocalPlayer player, BlockPos target, boolean complex) {
        Vec3 eye = player.getEyePosition();
        double dx = target.getX() + 0.5D - eye.x;
        double dy = target.getY() + 0.5D - eye.y;
        double dz = target.getZ() + 0.5D - eye.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float targetPitch = (float) -Math.toDegrees(Math.atan2(dy, Math.max(0.001D, horizontal)));
        float yawStep = complex ? 38.0F : 28.0F;
        float pitchStep = complex ? 26.0F : 18.0F;
        player.setYRot(approachAngle(player.getYRot(), targetYaw, yawStep));
        player.setXRot(approachLinear(player.getXRot(), targetPitch, pitchStep));
    }

    private static float approachAngle(float current, float target, float maxStep) {
        float delta = wrapDegrees(target - current);
        if (Math.abs(delta) <= maxStep) return current + delta;
        return current + Math.copySign(maxStep, delta);
    }

    private static float approachLinear(float current, float target, float maxStep) {
        float clampedTarget = Math.max(-90.0F, Math.min(90.0F, target));
        float delta = clampedTarget - current;
        if (Math.abs(delta) <= maxStep) return clampedTarget;
        return current + Math.copySign(maxStep, delta);
    }

    private static float wrapDegrees(float value) {
        float wrapped = value % 360.0F;
        if (wrapped >= 180.0F) wrapped -= 360.0F;
        if (wrapped < -180.0F) wrapped += 360.0F;
        return wrapped;
    }

    private enum Stage {
        IDLE,
        HOLD,
        REALIGN,
        RETRY,
        RESUME
    }

    public record Snapshot(
        String phase,
        boolean pauseMovement,
        boolean forceSneak,
        boolean retryNow,
        BlockPos target
    ) {
        private static Snapshot idle() {
            return new Snapshot("", false, false, false, null);
        }
    }
}
