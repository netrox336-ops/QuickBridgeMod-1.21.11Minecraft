package dev.netrox.quickbridge;

import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

public final class BridgeConfig {
    private static final BridgeConfig INSTANCE = new BridgeConfig();

    private final Map<BridgeTechnique, TechniqueTuning> tuning = new EnumMap<>(BridgeTechnique.class);
    private boolean loaded;
    private BridgeTechnique technique = BridgeTechnique.NINJA;
    private ControlMode controlMode = ControlMode.HOLD;
    private boolean hudEnabled = true;
    private boolean diagnosticHud = true;
    private boolean autoLearning = true;
    private boolean learningHud = true;
    private boolean trainingMode;
    private boolean networkProfiles = true;
    private boolean networkGuard = true;
    private boolean executionGuard = true;
    private boolean smartPlacement = true;
    private boolean pathGuard = true;
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

    private BridgeConfig() {
        for (BridgeTechnique value : BridgeTechnique.values()) tuning.put(value, TechniqueTuning.defaults());
    }

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
            autoLearning = Boolean.parseBoolean(p.getProperty("autoLearning", "true"));
            learningHud = Boolean.parseBoolean(p.getProperty("learningHud", "true"));
            trainingMode = Boolean.parseBoolean(p.getProperty("trainingMode", "false"));
            networkProfiles = Boolean.parseBoolean(p.getProperty("networkProfiles", "true"));
            networkGuard = Boolean.parseBoolean(p.getProperty("networkGuard", "true"));
            executionGuard = Boolean.parseBoolean(p.getProperty("executionGuard", "true"));
            smartPlacement = Boolean.parseBoolean(p.getProperty("smartPlacement", "true"));
            pathGuard = Boolean.parseBoolean(p.getProperty("pathGuard", "true"));
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

            for (BridgeTechnique value : BridgeTechnique.values()) {
                TechniqueTuning defaults = TechniqueTuning.defaults();
                String prefix = "tuning." + value.name() + ".";
                tuning.put(value, new TechniqueTuning(
                    parseDouble(p.getProperty(prefix + "cycleScale"), defaults.cycleScale()),
                    parseDouble(p.getProperty(prefix + "leadOffset"), defaults.leadOffset()),
                    parseFloat(p.getProperty(prefix + "rotationScale"), defaults.rotationScale()),
                    parseDouble(p.getProperty(prefix + "cadenceBias"), defaults.cadenceBias())
                ));
            }
        } catch (IOException ignored) {}
    }

    public void save() {
        Properties p = new Properties();
        p.setProperty("technique", technique.name());
        p.setProperty("controlMode", controlMode.name());
        p.setProperty("hudEnabled", Boolean.toString(hudEnabled));
        p.setProperty("diagnosticHud", Boolean.toString(diagnosticHud));
        p.setProperty("autoLearning", Boolean.toString(autoLearning));
        p.setProperty("learningHud", Boolean.toString(learningHud));
        p.setProperty("trainingMode", Boolean.toString(trainingMode));
        p.setProperty("networkProfiles", Boolean.toString(networkProfiles));
        p.setProperty("networkGuard", Boolean.toString(networkGuard));
        p.setProperty("executionGuard", Boolean.toString(executionGuard));
        p.setProperty("smartPlacement", Boolean.toString(smartPlacement));
        p.setProperty("pathGuard", Boolean.toString(pathGuard));
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

        for (BridgeTechnique value : BridgeTechnique.values()) {
            TechniqueTuning valueTuning = tuning(value);
            String prefix = "tuning." + value.name() + ".";
            p.setProperty(prefix + "cycleScale", Double.toString(valueTuning.cycleScale()));
            p.setProperty(prefix + "leadOffset", Double.toString(valueTuning.leadOffset()));
            p.setProperty(prefix + "rotationScale", Float.toString(valueTuning.rotationScale()));
            p.setProperty(prefix + "cadenceBias", Double.toString(valueTuning.cadenceBias()));
        }

        Path file = configFile();
        try {
            Files.createDirectories(file.getParent());
            try (OutputStream output = Files.newOutputStream(file)) {
                p.store(output, "QuickBridge 0.8.0");
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

    private static double parseDouble(String value, double fallback) {
        try { return value == null ? fallback : Double.parseDouble(value); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private static float parseFloat(String value, float fallback) {
        try { return value == null ? fallback : Float.parseFloat(value); }
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
    public boolean autoLearning() { return autoLearning; }
    public void toggleAutoLearning() { autoLearning = !autoLearning; save(); }
    public boolean learningHud() { return learningHud; }
    public void toggleLearningHud() { learningHud = !learningHud; save(); }
    public boolean trainingMode() { return trainingMode; }
    public void toggleTrainingMode() { trainingMode = !trainingMode; TrainingSession.reset(); save(); }
    public boolean networkProfiles() { return networkProfiles; }
    public void toggleNetworkProfiles() { networkProfiles = !networkProfiles; save(); }
    public boolean networkGuard() { return networkGuard; }
    public void toggleNetworkGuard() { networkGuard = !networkGuard; save(); }
    public boolean executionGuard() { return executionGuard; }
    public void toggleExecutionGuard() { executionGuard = !executionGuard; save(); }
    public boolean smartPlacement() { return smartPlacement; }
    public void toggleSmartPlacement() { smartPlacement = !smartPlacement; save(); }
    public boolean pathGuard() { return pathGuard; }
    public void togglePathGuard() { pathGuard = !pathGuard; save(); }
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

    public TechniqueTuning tuning(BridgeTechnique value) {
        return tuning.getOrDefault(value, TechniqueTuning.defaults());
    }

    public void adjustCycleScale(BridgeTechnique value, double delta) {
        TechniqueTuning current = tuning(value);
        tuning.put(value, current.withCycleScale(round(current.cycleScale() + delta, 2)));
        save();
    }

    public void adjustLeadOffset(BridgeTechnique value, double delta) {
        TechniqueTuning current = tuning(value);
        tuning.put(value, current.withLeadOffset(round(current.leadOffset() + delta, 2)));
        save();
    }

    public void adjustRotationScale(BridgeTechnique value, float delta) {
        TechniqueTuning current = tuning(value);
        tuning.put(value, current.withRotationScale((float) round(current.rotationScale() + delta, 2)));
        save();
    }

    public void adjustCadenceBias(BridgeTechnique value, double delta) {
        TechniqueTuning current = tuning(value);
        tuning.put(value, current.withCadenceBias(round(current.cadenceBias() + delta, 2)));
        save();
    }

    public void resetTuning(BridgeTechnique value) {
        tuning.put(value, TechniqueTuning.defaults());
        save();
    }

    private static double round(double value, int digits) {
        double scale = Math.pow(10.0D, digits);
        return Math.round(value * scale) / scale;
    }
}
