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
        boolean diagnostics = config.diagnosticHud();
        boolean learning = diagnostics && config.learningHud();
        int width = diagnostics ? 272 : Math.max(158, minecraft.font.width(technique.displayName()) + 52);
        int height = learning ? 84 : diagnostics ? 70 : 45;

        graphics.fill(x, y, x + width, y + height, 0xB0101010);
        graphics.fill(x, y, x + 3, y + height, BridgeEngine.active() ? 0xFF55FF55 : 0xFF777777);
        graphics.drawString(minecraft.font, "QUICKBRIDGE 0.4", x + 8, y + 6, 0xFFFFFFFF, true);
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

        if (diagnostics) {
            String edge = BridgeEngine.edgeDistance() >= 0.89D
                ? "safe"
                : String.format(Locale.ROOT, "%.2f", BridgeEngine.edgeDistance());
            String placement = "OK " + BridgeEngine.confirmedPlacements()
                + " • R " + BridgeEngine.recoveryAttempts()
                + " • F " + BridgeEngine.failedPlacements()
                + " • Edge " + edge;
            graphics.drawString(minecraft.font, placement, x + 8, y + 42, 0xFFB8B8B8, true);

            String adaptive = String.format(
                Locale.ROOT,
                "Speed %.3f • Cad %.2fx • ACK %.1ft • Run %.0f%%",
                BridgeEngine.horizontalSpeed(),
                BridgeEngine.cadenceFactor(),
                BridgeEngine.averageConfirmationTicks(),
                BridgeEngine.placementReliability() * 100.0D
            );
            graphics.drawString(minecraft.font, adaptive, x + 8, y + 54, 0xFF9FD7FF, true);
        }

        if (learning) {
            LearningProfile profile = BridgeEngine.learningProfile();
            String learned = String.format(
                Locale.ROOT,
                "Learn %d • %.0f%% • Conf %.0f%% • %s",
                profile.samples(),
                profile.reliability() * 100.0D,
                profile.confidence() * 100.0D,
                ServerContext.shortLabel(BridgeEngine.serverLabel())
            );
            graphics.drawString(
                minecraft.font,
                learned,
                x + 8,
                y + 68,
                config.autoLearning() ? 0xFFB8E6B8 : 0xFF888888,
                true
            );
        }
    }
}
