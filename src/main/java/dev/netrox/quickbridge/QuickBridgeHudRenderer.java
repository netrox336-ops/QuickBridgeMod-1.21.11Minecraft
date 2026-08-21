package dev.netrox.quickbridge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Locale;

public final class QuickBridgeHudRenderer {
    private QuickBridgeHudRenderer() {}

    public static void render(GuiGraphics graphics, BridgeConfig config) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !config.hudEnabled()) return;

        int x = config.hudX();
        int y = config.hudY();
        int blocks = PlacementHelper.countHotbarBlocks(minecraft.player);
        BridgeTechnique technique = config.technique();
        int width = config.diagnosticHud() ? 206 : Math.max(154, minecraft.font.width(technique.displayName()) + 50);
        int height = config.diagnosticHud() ? 58 : 45;

        graphics.fill(x, y, x + width, y + height, 0xB0101010);
        graphics.fill(x, y, x + 3, y + height, BridgeEngine.active() ? 0xFF55FF55 : 0xFF777777);
        graphics.drawString(minecraft.font, "QUICKBRIDGE 0.2", x + 8, y + 6, 0xFFFFFFFF, true);
        graphics.drawString(
            minecraft.font,
            technique.displayName() + (technique.experimental() ? " [EXP]" : ""),
            x + 8,
            y + 18,
            technique.experimental() ? 0xFFFFAA00 : 0xFFE6E6E6,
            true
        );

        String status = BridgeEngine.active() ? BridgeEngine.phase() : "OFF";
        graphics.drawString(
            minecraft.font,
            status + " • Blocks: " + blocks + " • " + config.controlMode().displayName(),
            x + 8,
            y + 30,
            BridgeEngine.active() ? 0xFF55FF55 : 0xFFAAAAAA,
            true
        );

        if (config.diagnosticHud()) {
            String edge = BridgeEngine.edgeDistance() >= 0.89D
                ? "safe"
                : String.format(Locale.ROOT, "%.2f", BridgeEngine.edgeDistance());
            String diagnostics = "OK " + BridgeEngine.confirmedPlacements()
                + " • R " + BridgeEngine.recoveryAttempts()
                + " • F " + BridgeEngine.failedPlacements()
                + " • Edge " + edge;
            graphics.drawString(minecraft.font, diagnostics, x + 8, y + 42, 0xFFB8B8B8, true);
        }
    }
}
