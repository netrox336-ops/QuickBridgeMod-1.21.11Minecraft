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

        for (int slot = 0; slot < 9; slot++) {
            if (!isBlock(player.getInventory().getItem(slot))) continue;
            player.getInventory().setSelectedSlot(slot);
            return true;
        }
        return false;
    }

    public static boolean tryPlace(Minecraft minecraft, BlockPos target) {
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || minecraft.gameMode == null) return false;
        if (!minecraft.level.getBlockState(target).isAir()) return false;

        for (Direction face : Direction.values()) {
            BlockPos neighbor = target.relative(face.getOpposite());
            if (minecraft.level.getBlockState(neighbor).isAir()) continue;

            Vec3 hitLocation = new Vec3(
                neighbor.getX() + 0.5D + face.getStepX() * 0.5D,
                neighbor.getY() + 0.5D + face.getStepY() * 0.5D,
                neighbor.getZ() + 0.5D + face.getStepZ() * 0.5D
            );
            if (player.getEyePosition().distanceToSqr(hitLocation) > 25.0D) continue;

            minecraft.gameMode.useItemOn(
                player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(hitLocation, face, neighbor, false)
            );
            return true;
        }
        return false;
    }

    private static boolean isBlock(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof BlockItem;
    }
}
