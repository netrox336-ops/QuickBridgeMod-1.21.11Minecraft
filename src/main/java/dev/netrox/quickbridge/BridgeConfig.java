package dev.netrox.quickbridge;

import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class BridgeConfig {
    private static final BridgeConfig INSTANCE = new BridgeConfig();

    private boolean loaded;
    private BridgeTechnique technique = BridgeTechnique.NINJA;
    private ControlMode controlMode = ControlMode.HOLD;
    private boolean hudEnabled = true;
    private boolean diagnosticHud = true;
    private boolean autoSelectBlocks = true;
    private boolean stopOnFall = true;
    private boolean stopOnGui = true;
    private boolean sneakAssist = true;
    private boolean placementRecovery = true;
    private boolean stopOnPlacementFailures = true;
    private boolean restoreView = true;
    private int confirmationTicks = 5;
    private int maxRecoveryAttempts = 2;
    private int maxConsecutiveFailures = 3;
    private int hudX = 6;
    private int hudY = 6;

    private BridgeConfig() {}

    public static BridgeConfig get() {
        INSTANCE.ensureLoaded();
        return INSTANCE;
    }

    private void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        Path file = configFile();
        if (!Files.isRegularFile(file)) return;

        Properties p = new Properties();
        try (InputStream input = Files.newInputStream(file)) {
            p.load(input);
            try { technique = BridgeTechnique.valueOf(p.getProperty("technique", technique.name())); }
            catch (IllegalArgumentException ignored) {}
            try { controlMode = ControlMode.valueOf(p.getProperty("controlMode", controlMode.name())); }
            catch (IllegalArgumentException ignored) {}
            hudEnabled = Boolean.parseBoolean(p.getProperty("hudEnabled", "true"));
            diagnosticHud = Boolean.parseBoolean(p.getProperty("diagnosticHud", "true"));
            autoSelectBlocks = Boolean.parseBoolean(p.getProperty("autoSelectBlocks", "true"));
            stopOnFall = Boolean.parseBoolean(p.getProperty("stopOnFall", "true"));
            stopOnGui = Boolean.parseBoolean(p.getProperty("stopOnGui", "true"));
            sneakAssist = Boolean.parseBoolean(p.getProperty("sneakAssist", "true"));
            placementRecovery = Boolean.parseBoolean(p.getProperty("placementRecovery", "true"));
            stopOnPlacementFailures = Boolean.parseBoolean(p.getProperty("stopOnPlacementFailures", "true"));
            restoreView = Boolean.parseBoolean(p.getProperty("restoreView", "true"));
            confirmationTicks = clamp(parseInt(p.getProperty("confirmationTicks"), 5), 3, 10);
            maxRecoveryAttempts = clamp(parseInt(p.getProperty("maxRecoveryAttempts"), 2), 0, 4);
            maxConsecutiveFailures = clamp(parseInt(p.getProperty("maxConsecutiveFailures"), 3), 1, 8);
            hudX = Math.max(0, parseInt(p.getProperty("hudX"), 6));
            hudY = Math.max(0, parseInt(p.getProperty("hudY"), 6));
        } catch (IOException ignored) {}
    }

    public void save() {
        Properties p = new Properties();
        p.setProperty("technique", technique.name());
        p.setProperty("controlMode", controlMode.name());
        p.setProperty("hudEnabled", Boolean.toString(hudEnabled));
        p.setProperty("diagnosticHud", Boolean.toString(diagnosticHud));
        p.setProperty("autoSelectBlocks", Boolean.toString(autoSelectBlocks));
        p.setProperty("stopOnFall", Boolean.toString(stopOnFall));
        p.setProperty("stopOnGui", Boolean.toString(stopOnGui));
        p.setProperty("sneakAssist", Boolean.toString(sneakAssist));
        p.setProperty("placementRecovery", Boolean.toString(placementRecovery));
        p.setProperty("stopOnPlacementFailures", Boolean.toString(stopOnPlacementFailures));
        p.setProperty("restoreView", Boolean.toString(restoreView));
        p.setProperty("confirmationTicks", Integer.toString(confirmationTicks));
        p.setProperty("maxRecoveryAttempts", Integer.toString(maxRecoveryAttempts));
        p.setProperty("maxConsecutiveFailures", Integer.toString(maxConsecutiveFailures));
        p.setProperty("hudX", Integer.toString(hudX));
        p.setProperty("hudY", Integer.toString(hudY));

        Path file = configFile();
        try {
            Files.createDirectories(file.getParent());
            try (OutputStream output = Files.newOutputStream(file)) {
                p.store(output, "QuickBridge 0.2.0");
            }
        } catch (IOException ignored) {}
    }

    private Path configFile() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("quickbridge.properties");
    }

    private static int parseInt(String value, int fallback) {
        try { return value == null ? fallback : Integer.parseInt(value); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public BridgeTechnique technique() { return technique; }
    public void nextTechnique() { technique = technique.next(); save(); }
    public void setTechnique(BridgeTechnique technique) { this.technique = technique; save(); }
    public ControlMode controlMode() { return controlMode; }
    public void nextControlMode() { controlMode = controlMode.next(); save(); }
    public boolean hudEnabled() { return hudEnabled; }
    public void toggleHud() { hudEnabled = !hudEnabled; save(); }
    public boolean diagnosticHud() { return diagnosticHud; }
    public void toggleDiagnosticHud() { diagnosticHud = !diagnosticHud; save(); }
    public boolean autoSelectBlocks() { return autoSelectBlocks; }
    public void toggleAutoSelectBlocks() { autoSelectBlocks = !autoSelectBlocks; save(); }
    public boolean stopOnFall() { return stopOnFall; }
    public void toggleStopOnFall() { stopOnFall = !stopOnFall; save(); }
    public boolean stopOnGui() { return stopOnGui; }
    public void toggleStopOnGui() { stopOnGui = !stopOnGui; save(); }
    public boolean sneakAssist() { return sneakAssist; }
    public void toggleSneakAssist() { sneakAssist = !sneakAssist; save(); }
    public boolean placementRecovery() { return placementRecovery; }
    public void togglePlacementRecovery() { placementRecovery = !placementRecovery; save(); }
    public boolean stopOnPlacementFailures() { return stopOnPlacementFailures; }
    public void toggleStopOnPlacementFailures() { stopOnPlacementFailures = !stopOnPlacementFailures; save(); }
    public boolean restoreView() { return restoreView; }
    public void toggleRestoreView() { restoreView = !restoreView; save(); }
    public int confirmationTicks() { return confirmationTicks; }
    public void nextConfirmationTicks() { confirmationTicks = confirmationTicks >= 8 ? 3 : confirmationTicks + 1; save(); }
    public int maxRecoveryAttempts() { return maxRecoveryAttempts; }
    public void nextRecoveryAttempts() { maxRecoveryAttempts = maxRecoveryAttempts >= 3 ? 0 : maxRecoveryAttempts + 1; save(); }
    public int maxConsecutiveFailures() { return maxConsecutiveFailures; }
    public int hudX() { return hudX; }
    public int hudY() { return hudY; }
}
