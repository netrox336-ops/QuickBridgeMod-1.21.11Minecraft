package dev.netrox.quickbridge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class PlacementHelper {
    private PlacementHelper() {}

    public static int countHotbarBlocks(LocalPlayer player) {
        int count = 0;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof BlockItem) count += stack.getCount();
        }
        return count;
    }

    public static boolean ensureBlockSelected(Minecraft minecraft, boolean autoSelect) {
        LocalPlayer player = minecraft.player;
        if (player == null) return false;

        int current = player.getInventory().getSelectedSlot();
        if (isBlock(player.getInventory().getItem(current))) return true;
        if (!autoSelect) return false;

        int bestSlot = -1;
        int bestCount = -1;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!isBlock(stack) || stack.getCount() <= bestCount) continue;
            bestSlot = slot;
            bestCount = stack.getCount();
        }
        if (bestSlot < 0) return false;

        player.getInventory().setSelectedSlot(bestSlot);
        return true;
    }

    public static PlacementAttempt tryPlace(Minecraft minecraft, BlockPos target) {
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || minecraft.gameMode == null) return null;
        if (!minecraft.level.getBlockState(target).isAir()) return null;

        PlacementAttempt best = null;
        double bestDistance = Double.MAX_VALUE;

        for (Direction face : Direction.values()) {
            BlockPos neighbor = target.relative(face.getOpposite());
            if (minecraft.level.getBlockState(neighbor).isAir()) continue;

            Vec3 hitLocation = new Vec3(
                neighbor.getX() + 0.5D + face.getStepX() * 0.5D,
                neighbor.getY() + 0.5D + face.getStepY() * 0.5D,
                neighbor.getZ() + 0.5D + face.getStepZ() * 0.5D
            );
            double distance = player.getEyePosition().distanceToSqr(hitLocation);
            if (distance > 25.0D || distance >= bestDistance) continue;

            bestDistance = distance;
            best = new PlacementAttempt(target, neighbor, face, hitLocation);
        }

        if (best == null) return null;

        minecraft.gameMode.useItemOn(
            player,
            InteractionHand.MAIN_HAND,
            new BlockHitResult(best.hitLocation(), best.face(), best.neighbor(), false)
        );
        player.swing(InteractionHand.MAIN_HAND);
        return best;
    }

    public static boolean isPlaced(Minecraft minecraft, BlockPos target) {
        return minecraft.level != null && !minecraft.level.getBlockState(target).isAir();
    }

    private static boolean isBlock(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof BlockItem;
    }

    public record PlacementAttempt(BlockPos target, BlockPos neighbor, Direction face, Vec3 hitLocation) {}
}
