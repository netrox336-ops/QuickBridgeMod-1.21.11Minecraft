package dev.netrox.quickbridge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PlacementPlanner {
    private PlacementPlanner() {}

    public static Plan plan(
        Minecraft minecraft,
        LocalPlayer player,
        BridgeTechnique technique,
        TechniqueTuning tuning,
        TechniqueStateMachine.Snapshot state,
        double[] move,
        int extraIndex
    ) {
        if (minecraft.level == null || player == null || move == null || move.length < 2) {
            return new Plan(List.of(), 0, 0);
        }

        double moveLength = Math.sqrt(move[0] * move[0] + move[1] * move[1]);
        if (moveLength < 0.0001D) return new Plan(List.of(), 0, 0);
        double dirX = move[0] / moveLength;
        double dirZ = move[1] / moveLength;
        double sideX = dirZ;
        double sideZ = -dirX;

        double adaptiveLead = Math.min(0.16D, state.horizontalSpeed() * 0.38D);
        double baseDistance = Math.max(
            0.18D,
            technique.placementLead() + tuning.leadOffset() + adaptiveLead + extraIndex * 0.46D
        );
        double ackShift = Math.min(0.12D, Math.max(0.0D, 1.0D - state.reliability()) * 0.30D);
        baseDistance = Math.max(0.18D, baseDistance - ackShift);

        double[] forwardOffsets = new double[] {-0.24D, 0.0D, 0.30D, 0.62D, 0.92D};
        double[] lateralOffsets = technique.movementStyle() == MovementStyle.FORWARD
            ? new double[] {0.0D, -0.26D, 0.26D}
            : new double[] {0.0D, -0.22D, 0.22D};

        int y = (int) Math.floor(player.getY() - 1.0D);
        Set<BlockPos> seen = new HashSet<>();
        List<Candidate> viable = new ArrayList<>();
        int scanned = 0;

        for (double forwardOffset : forwardOffsets) {
            for (double lateralOffset : lateralOffsets) {
                double distance = Math.max(0.05D, baseDistance + forwardOffset);
                double x = player.getX() + dirX * distance + sideX * lateralOffset;
                double z = player.getZ() + dirZ * distance + sideZ * lateralOffset;
                BlockPos target = BlockPos.containing(x, y, z);
                if (!seen.add(target)) continue;
                scanned++;
                Candidate candidate = evaluate(minecraft, player, target, dirX, dirZ, baseDistance, lateralOffset);
                if (candidate != null) viable.add(candidate);
            }
        }

        BlockPos underPlayer = BlockPos.containing(player.getX(), y, player.getZ());
        if (seen.add(underPlayer)) {
            scanned++;
            Candidate fallback = evaluate(minecraft, player, underPlayer, dirX, dirZ, 0.0D, 0.0D);
            if (fallback != null) viable.add(new Candidate(fallback.target(), fallback.score() - 0.55D, fallback.attachments()));
        }

        viable.sort(Comparator.comparingDouble(Candidate::score).reversed());
        return new Plan(List.copyOf(viable), scanned, viable.size());
    }

    private static Candidate evaluate(
        Minecraft minecraft,
        LocalPlayer player,
        BlockPos target,
        double dirX,
        double dirZ,
        double idealDistance,
        double lateralOffset
    ) {
        if (!minecraft.level.getBlockState(target).isAir()) return null;

        int attachments = 0;
        double nearestAttach = Double.MAX_VALUE;
        Vec3 eye = player.getEyePosition();
        for (Direction face : Direction.values()) {
            BlockPos neighbor = target.relative(face.getOpposite());
            if (minecraft.level.getBlockState(neighbor).isAir()) continue;
            attachments++;
            Vec3 hit = new Vec3(
                neighbor.getX() + 0.5D + face.getStepX() * 0.5D,
                neighbor.getY() + 0.5D + face.getStepY() * 0.5D,
                neighbor.getZ() + 0.5D + face.getStepZ() * 0.5D
            );
            nearestAttach = Math.min(nearestAttach, eye.distanceToSqr(hit));
        }
        if (attachments == 0 || nearestAttach > 25.0D) return null;

        double centerX = target.getX() + 0.5D - player.getX();
        double centerZ = target.getZ() + 0.5D - player.getZ();
        double projected = centerX * dirX + centerZ * dirZ;
        double forwardError = Math.abs(projected - idealDistance);
        double lateralPenalty = Math.abs(lateralOffset);
        double reachPenalty = Math.max(0.0D, nearestAttach - 7.0D) * 0.035D;
        double supportBonus = Math.min(3, attachments) * 0.34D;
        double forwardBonus = Math.max(0.0D, projected) * 0.08D;
        double score = 2.25D + supportBonus + forwardBonus
            - forwardError * 0.72D
            - lateralPenalty * 0.46D
            - reachPenalty;
        return new Candidate(target, score, attachments);
    }

    public record Candidate(BlockPos target, double score, int attachments) {}

    public record Plan(List<Candidate> candidates, int scanned, int viable) {}
}
