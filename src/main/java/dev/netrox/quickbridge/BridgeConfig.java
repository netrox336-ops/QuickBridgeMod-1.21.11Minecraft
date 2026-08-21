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
    private boolean autoSelectBlocks = true;
    private boolean stopOnFall = true;
    private boolean stopOnGui = true;
    private boolean sneakAssist = true;
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
            autoSelectBlocks = Boolean.parseBoolean(p.getProperty("autoSelectBlocks", "true"));
            stopOnFall = Boolean.parseBoolean(p.getProperty("stopOnFall", "true"));
            stopOnGui = Boolean.parseBoolean(p.getProperty("stopOnGui", "true"));
            sneakAssist = Boolean.parseBoolean(p.getProperty("sneakAssist", "true"));
            hudX = parseInt(p.getProperty("hudX"), 6);
            hudY = parseInt(p.getProperty("hudY"), 6);
        } catch (IOException ignored) {}
    }

    public void save() {
        Properties p = new Properties();
        p.setProperty("technique", technique.name());
        p.setProperty("controlMode", controlMode.name());
        p.setProperty("hudEnabled", Boolean.toString(hudEnabled));
        p.setProperty("autoSelectBlocks", Boolean.toString(autoSelectBlocks));
        p.setProperty("stopOnFall", Boolean.toString(stopOnFall));
        p.setProperty("stopOnGui", Boolean.toString(stopOnGui));
        p.setProperty("sneakAssist", Boolean.toString(sneakAssist));
        p.setProperty("hudX", Integer.toString(hudX));
        p.setProperty("hudY", Integer.toString(hudY));

        Path file = configFile();
        try {
            Files.createDirectories(file.getParent());
            try (OutputStream output = Files.newOutputStream(file)) {
                p.store(output, "QuickBridge 0.1.0");
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

    public BridgeTechnique technique() { return technique; }
    public void nextTechnique() { technique = technique.next(); save(); }
    public void setTechnique(BridgeTechnique technique) { this.technique = technique; save(); }
    public ControlMode controlMode() { return controlMode; }
    public void nextControlMode() { controlMode = controlMode.next(); save(); }
    public boolean hudEnabled() { return hudEnabled; }
    public void toggleHud() { hudEnabled = !hudEnabled; save(); }
    public boolean autoSelectBlocks() { return autoSelectBlocks; }
    public void toggleAutoSelectBlocks() { autoSelectBlocks = !autoSelectBlocks; save(); }
    public boolean stopOnFall() { return stopOnFall; }
    public void toggleStopOnFall() { stopOnFall = !stopOnFall; save(); }
    public boolean stopOnGui() { return stopOnGui; }
    public void toggleStopOnGui() { stopOnGui = !stopOnGui; save(); }
    public boolean sneakAssist() { return sneakAssist; }
    public void toggleSneakAssist() { sneakAssist = !sneakAssist; save(); }
    public int hudX() { return hudX; }
    public int hudY() { return hudY; }
}
