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
        int width = diagnostics ? 326 : Math.max(158, minecraft.font.width(technique.displayName()) + 52);
        int height = learning ? 126 : diagnostics ? 84 : 45;

        graphics.fill(x, y, x + width, y + height, 0xB0101010);
        graphics.fill(x, y, x + 3, y + height, BridgeEngine.active() ? 0xFF55FF55 : 0xFF777777);
        graphics.drawString(minecraft.font, "QUICKBRIDGE 0.7", x + 8, y + 6, 0xFFFFFFFF, true);
        graphics.drawString(minecraft.font,
            technique.displayName() + (technique.experimental() ? " [EXP]" : "") + (config.trainingMode() ? " [TRAIN]" : ""),
            x + 8, y + 18, config.trainingMode() ? 0xFFFFD166 : technique.experimental() ? 0xFFFFAA00 : 0xFFE6E6E6, true);

        String status = BridgeEngine.active() ? BridgeEngine.phase() : "OFF";
        graphics.drawString(minecraft.font,
            status + " • Blocks: " + blocks + " • " + config.controlMode().displayName(),
            x + 8, y + 30, BridgeEngine.active() ? 0xFF55FF55 : 0xFFAAAAAA, true);

        if (diagnostics) {
            String edge = BridgeEngine.edgeDistance() >= 0.89D ? "safe" : String.format(Locale.ROOT, "%.2f", BridgeEngine.edgeDistance());
            graphics.drawString(minecraft.font,
                "OK " + BridgeEngine.confirmedPlacements() + " • R " + BridgeEngine.recoveryAttempts()
                    + " • F " + BridgeEngine.failedPlacements() + " • Edge " + edge,
                x + 8, y + 42, 0xFFB8B8B8, true);

            String adaptive = String.format(Locale.ROOT,
                "Speed %.3f • Cad %.2fx • ACK %.1ft • Run %.0f%%",
                BridgeEngine.horizontalSpeed(), BridgeEngine.cadenceFactor(), BridgeEngine.averageConfirmationTicks(),
                BridgeEngine.placementReliability() * 100.0D);
            graphics.drawString(minecraft.font, adaptive, x + 8, y + 54, 0xFF9FD7FF, true);

            String execution = String.format(Locale.ROOT,
                "Exec %.0f%% • Yaw %.1f° • Pitch %.1f° • Window %s • %s",
                ExecutionDiagnostics.motionScore() * 100.0D,
                ExecutionDiagnostics.yawError(),
                ExecutionDiagnostics.pitchError(),
                ExecutionDiagnostics.placementWindowOpen() ? "OPEN" : "CLOSED",
                config.executionGuard() ? (ExecutionDiagnostics.placementReady() ? "READY" : "GUARD") : "BYPASS");
            graphics.drawString(minecraft.font, execution, x + 8, y + 68,
                ExecutionDiagnostics.placementReady() || !config.executionGuard() ? 0xFFB8E6B8 : 0xFFFFB86B, true);
        }

        if (learning) {
            BridgeCycleResult cycle = BridgeEngine.lastCycle();
            String cycleLine = String.format(Locale.ROOT, "Cycle %.0f%% • observed %s%s • blocked %d • resync %d",
                cycle.quality() * 100.0D, cycle.networkCondition().displayName(),
                BridgeEngine.lastCycleRollback() ? " • ROLLBACK" : "",
                ExecutionDiagnostics.blockedPlacements(),
                ExecutionDiagnostics.phaseResyncs());
            graphics.drawString(minecraft.font, cycleLine, x + 8, y + 82, 0xFFFFD28A, true);

            String profileLine;
            if (BridgeEngine.networkTransitioning()) {
                profileLine = String.format(Locale.ROOT, "Profile %s → %s • blend %.0f%% • switches %d",
                    BridgeEngine.previousNetworkCondition().displayName(),
                    BridgeEngine.activeNetworkCondition().displayName(),
                    BridgeEngine.networkBlend() * 100.0D,
                    BridgeEngine.networkSwitches());
            } else {
                profileLine = "Profile " + BridgeEngine.activeNetworkCondition().displayName()
                    + " • candidate " + BridgeEngine.candidateNetworkCondition().displayName()
                    + " (" + BridgeEngine.networkCandidateCycles() + ") • switches " + BridgeEngine.networkSwitches();
            }
            graphics.drawString(minecraft.font, profileLine, x + 8, y + 96,
                config.networkProfiles() ? 0xFFB9C7FF : 0xFF888888, true);

            if (config.trainingMode()) {
                String training = String.format(Locale.ROOT, "Training %d cycles • %.0f%% success • Q %.0f%%",
                    TrainingSession.cycles(), TrainingSession.successRate() * 100.0D, TrainingSession.qualityEma() * 100.0D);
                graphics.drawString(minecraft.font, training, x + 8, y + 110, 0xFFFFD166, true);
            } else {
                LearningProfile profile = BridgeEngine.conditionLearningProfile();
                String learned = String.format(Locale.ROOT, "Bucket %d/%d cycles • Q %.0f%% • Conf %.0f%% • RB %d",
                    profile.successfulCycles(), profile.cycles(), profile.cycleQualityEma() * 100.0D,
                    profile.confidence() * 100.0D, profile.rollbacks());
                graphics.drawString(minecraft.font, learned, x + 8, y + 110,
                    config.autoLearning() && config.networkProfiles() ? 0xFFB8E6B8 : 0xFF888888, true);
            }
        }
    }
}
