package dev.netrox.quickbridge;

import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public final class ServerLearningStore {
    private static final ServerLearningStore INSTANCE = new ServerLearningStore();

    private final Map<String, LearningProfile> profiles = new HashMap<>();
    private final Properties persisted = new Properties();
    private boolean loaded;
    private boolean dirty;

    private ServerLearningStore() {}

    public static ServerLearningStore get() {
        INSTANCE.ensureLoaded();
        return INSTANCE;
    }

    public LearningProfile profile(String serverId, BridgeTechnique technique) {
        ensureLoaded();
        String key = globalKey(serverId, technique);
        LearningProfile cached = profiles.get(key);
        if (cached != null) return cached;
        LearningProfile profile = loadProfile(globalPrefix(serverId, technique));
        profiles.put(key, profile);
        return profile;
    }

    public LearningProfile conditionProfile(
        String serverId,
        BridgeTechnique technique,
        NetworkCondition condition
    ) {
        ensureLoaded();
        NetworkCondition safeCondition = condition == null ? NetworkCondition.STABLE : condition;
        String key = conditionKey(serverId, technique, safeCondition);
        LearningProfile cached = profiles.get(key);
        if (cached != null) return cached;
        LearningProfile profile = loadProfile(conditionPrefix(serverId, technique, safeCondition));
        profiles.put(key, profile);
        return profile;
    }

    private LearningProfile loadProfile(String prefix) {
        LearningProfile profile = new LearningProfile();
        profile.restore(
            parseLong(persisted.getProperty(prefix + "samples"), 0L),
            parseLong(persisted.getProperty(prefix + "successes"), 0L),
            parseLong(persisted.getProperty(prefix + "failures"), 0L),
            parseLong(persisted.getProperty(prefix + "recovered"), 0L),
            parseDouble(persisted.getProperty(prefix + "ackEma"), 0.0D),
            parseDouble(persisted.getProperty(prefix + "cycleAdjustment"), 0.0D),
            parseDouble(persisted.getProperty(prefix + "leadAdjustment"), 0.0D),
            parseDouble(persisted.getProperty(prefix + "rotationAdjustment"), 0.0D),
            parseDouble(persisted.getProperty(prefix + "cadenceAdjustment"), 0.0D),
            parseLong(persisted.getProperty(prefix + "cycles"), 0L),
            parseLong(persisted.getProperty(prefix + "successfulCycles"), 0L),
            parseLong(persisted.getProperty(prefix + "rollbacks"), 0L),
            parseDouble(persisted.getProperty(prefix + "cycleQualityEma"), 0.0D),
            parseDouble(persisted.getProperty(prefix + "bestCycleQuality"), 0.0D),
            parseLong(persisted.getProperty(prefix + "checkpointCycle"), 0L),
            parseDouble(persisted.getProperty(prefix + "checkpointQuality"), 0.0D),
            parseDouble(persisted.getProperty(prefix + "checkpointCycleAdjustment"), 0.0D),
            parseDouble(persisted.getProperty(prefix + "checkpointLeadAdjustment"), 0.0D),
            parseDouble(persisted.getProperty(prefix + "checkpointRotationAdjustment"), 0.0D),
            parseDouble(persisted.getProperty(prefix + "checkpointCadenceAdjustment"), 0.0D)
        );
        return profile;
    }

    public void markDirty() {
        dirty = true;
    }

    public void reset(String serverId, BridgeTechnique technique) {
        ensureLoaded();
        String encoded = encoded(serverId);
        String techniqueName = technique.name();
        profiles.keySet().removeIf(key -> key.startsWith(encoded + "|" + techniqueName));
        removePrefix(globalPrefix(serverId, technique));
        removePrefix("condition." + encoded + "." + techniqueName + ".");
        dirty = true;
        flush();
    }

    public void resetCondition(
        String serverId,
        BridgeTechnique technique,
        NetworkCondition condition
    ) {
        ensureLoaded();
        NetworkCondition safeCondition = condition == null ? NetworkCondition.STABLE : condition;
        profiles.remove(conditionKey(serverId, technique, safeCondition));
        removePrefix(conditionPrefix(serverId, technique, safeCondition));
        dirty = true;
        flush();
    }

    public void resetServer(String serverId) {
        ensureLoaded();
        String encoded = encoded(serverId);
        profiles.keySet().removeIf(key -> key.startsWith(encoded + "|"));
        removePrefix("profile." + encoded + ".");
        removePrefix("condition." + encoded + ".");
        dirty = true;
        flush();
    }

    public void flush() {
        ensureLoaded();
        if (!dirty) return;

        for (Map.Entry<String, LearningProfile> entry : profiles.entrySet()) {
            String[] parts = entry.getKey().split("\\|");
            if (parts.length == 2) {
                writeProfile("profile." + parts[0] + "." + parts[1] + ".", entry.getValue());
            } else if (parts.length == 3) {
                writeProfile("condition." + parts[0] + "." + parts[1] + "." + parts[2] + ".", entry.getValue());
            }
        }

        Path file = file();
        try {
            Files.createDirectories(file.getParent());
            try (OutputStream output = Files.newOutputStream(file)) {
                persisted.store(output, "QuickBridge server learning 0.6.0");
            }
            dirty = false;
        } catch (IOException ignored) {
        }
    }

    private void writeProfile(String prefix, LearningProfile profile) {
        persisted.setProperty(prefix + "samples", Long.toString(profile.samples()));
        persisted.setProperty(prefix + "successes", Long.toString(profile.successes()));
        persisted.setProperty(prefix + "failures", Long.toString(profile.failures()));
        persisted.setProperty(prefix + "recovered", Long.toString(profile.recoveredSuccesses()));
        persisted.setProperty(prefix + "ackEma", Double.toString(profile.ackEma()));
        persisted.setProperty(prefix + "cycleAdjustment", Double.toString(profile.cycleAdjustment()));
        persisted.setProperty(prefix + "leadAdjustment", Double.toString(profile.leadAdjustment()));
        persisted.setProperty(prefix + "rotationAdjustment", Double.toString(profile.rotationAdjustment()));
        persisted.setProperty(prefix + "cadenceAdjustment", Double.toString(profile.cadenceAdjustment()));
        persisted.setProperty(prefix + "cycles", Long.toString(profile.cycles()));
        persisted.setProperty(prefix + "successfulCycles", Long.toString(profile.successfulCycles()));
        persisted.setProperty(prefix + "rollbacks", Long.toString(profile.rollbacks()));
        persisted.setProperty(prefix + "cycleQualityEma", Double.toString(profile.cycleQualityEma()));
        persisted.setProperty(prefix + "bestCycleQuality", Double.toString(profile.bestCycleQuality()));
        persisted.setProperty(prefix + "checkpointCycle", Long.toString(profile.checkpointCycle()));
        persisted.setProperty(prefix + "checkpointQuality", Double.toString(profile.checkpointQuality()));
        persisted.setProperty(prefix + "checkpointCycleAdjustment", Double.toString(profile.checkpointCycleAdjustment()));
        persisted.setProperty(prefix + "checkpointLeadAdjustment", Double.toString(profile.checkpointLeadAdjustment()));
        persisted.setProperty(prefix + "checkpointRotationAdjustment", Double.toString(profile.checkpointRotationAdjustment()));
        persisted.setProperty(prefix + "checkpointCadenceAdjustment", Double.toString(profile.checkpointCadenceAdjustment()));
    }

    private void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        Path file = file();
        if (!Files.isRegularFile(file)) return;
        try (InputStream input = Files.newInputStream(file)) {
            persisted.load(input);
        } catch (IOException ignored) {
        }
    }

    private void removePrefix(String prefix) {
        List<String> remove = new ArrayList<>();
        for (String name : persisted.stringPropertyNames()) {
            if (name.startsWith(prefix)) remove.add(name);
        }
        for (String name : remove) persisted.remove(name);
    }

    private Path file() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("quickbridge-learning.properties");
    }

    private static String globalPrefix(String serverId, BridgeTechnique technique) {
        return "profile." + encoded(serverId) + "." + technique.name() + ".";
    }

    private static String conditionPrefix(
        String serverId,
        BridgeTechnique technique,
        NetworkCondition condition
    ) {
        return "condition." + encoded(serverId) + "." + technique.name() + "." + condition.name() + ".";
    }

    private static String globalKey(String serverId, BridgeTechnique technique) {
        return encoded(serverId) + "|" + technique.name();
    }

    private static String conditionKey(
        String serverId,
        BridgeTechnique technique,
        NetworkCondition condition
    ) {
        return encoded(serverId) + "|" + technique.name() + "|" + condition.name();
    }

    private static String encoded(String serverId) {
        String value = serverId == null || serverId.isBlank() ? "unknown" : serverId;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static long parseLong(String value, long fallback) {
        try { return value == null ? fallback : Long.parseLong(value); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private static double parseDouble(String value, double fallback) {
        try { return value == null ? fallback : Double.parseDouble(value); }
        catch (NumberFormatException ignored) { return fallback; }
    }
}
