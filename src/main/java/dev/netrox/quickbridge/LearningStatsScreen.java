package dev.netrox.quickbridge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public final class LearningStatsScreen extends Screen {
    private final Screen parent;

    public LearningStatsScreen(Screen parent) {
        super(Component.literal("Обучение QuickBridge"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        BridgeConfig config = BridgeConfig.get();
        int center = width / 2;
        int top = Math.max(28, height / 2 - 116);

        addRenderableWidget(Button.builder(
            Component.literal("Техника: " + config.technique().displayName()),
            button -> {
                config.nextTechnique();
                minecraft.setScreen(new LearningStatsScreen(parent));
            }
        ).bounds(center - 154, top, 308, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Network Profiles: " + state(config.networkProfiles())),
            button -> {
                config.toggleNetworkProfiles();
                button.setMessage(Component.literal("Network Profiles: " + state(config.networkProfiles())));
            }
        ).bounds(center - 154, top + 28, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Network Guard: " + state(config.networkGuard())),
            button -> {
                config.toggleNetworkGuard();
                button.setMessage(Component.literal("Network Guard: " + state(config.networkGuard())));
            }
        ).bounds(center + 4, top + 28, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Сбросить active profile"),
            button -> {
                LearningEngine.resetCondition(serverId(), config.technique(), activeCondition(config));
                minecraft.setScreen(new LearningStatsScreen(parent));
            }
        ).bounds(center - 154, top + 166, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Сбросить технику"),
            button -> {
                LearningEngine.reset(serverId(), config.technique());
                minecraft.setScreen(new LearningStatsScreen(parent));
            }
        ).bounds(center + 4, top + 166, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Сбросить тренировку"),
            button -> TrainingSession.reset()
        ).bounds(center - 154, top + 192, 150, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Готово"), button -> onClose())
            .bounds(center + 4, top + 192, 150, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        BridgeConfig config = BridgeConfig.get();
        String server = serverId();
        NetworkCondition active = activeCondition(config);
        LearningProfile global = LearningEngine.profile(server, config.technique());
        LearningProfile bucket = LearningEngine.conditionProfile(server, config.technique(), active);
        TechniqueTuning effective = LearningEngine.effectiveTuning(
            config, server, config.technique(), active, active, 1.0D);
        int center = width / 2;
        int top = Math.max(28, height / 2 - 116);

        graphics.drawCenteredString(font, title, center, 7, 0xFFFFFF);
        graphics.drawCenteredString(font,
            Component.literal("Сервер: " + ServerContext.shortLabel(ServerContext.label(Minecraft.getInstance()))),
            center, 19, 0xA0A0A0);

        String globalStats = String.format(Locale.ROOT,
            "GLOBAL: %d samples • %.1f%% rel • %d cycles • Q %.1f%% • Conf %.0f%%",
            global.samples(), global.reliability() * 100.0D, global.cycles(),
            global.cycleQualityEma() * 100.0D, global.confidence() * 100.0D);
        graphics.drawCenteredString(font, Component.literal(globalStats), center, top + 58, 0xE6E6E6);

        String bucketStats = String.format(Locale.ROOT,
            "%s: %d samples • %.1f%% rel • %d cycles • Q %.1f%% • Conf %.0f%%",
            active.displayName(), bucket.samples(), bucket.reliability() * 100.0D, bucket.cycles(),
            bucket.cycleQualityEma() * 100.0D, bucket.confidence() * 100.0D);
        graphics.drawCenteredString(font, Component.literal(bucketStats), center, top + 74, 0xB9C7FF);

        String selector = BridgeEngine.active()
            ? "Selector: " + BridgeEngine.activeNetworkCondition().displayName()
                + " • candidate " + BridgeEngine.candidateNetworkCondition().displayName()
                + " x" + BridgeEngine.networkCandidateCycles()
                + " • switches " + BridgeEngine.networkSwitches()
                + (BridgeEngine.networkTransitioning()
                    ? String.format(Locale.ROOT, " • blend %.0f%%", BridgeEngine.networkBlend() * 100.0D)
                    : "")
            : "Selector: будет определён при следующем запуске техники";
        graphics.drawCenteredString(font, Component.literal(selector), center, top + 90, 0x9FD7FF);

        String corrections = String.format(Locale.ROOT,
            "Bucket correction: cycle %+.3f • lead %+.3f • rot %+.3f • cad %+.3f",
            bucket.cycleAdjustment(), bucket.leadAdjustment(),
            bucket.rotationAdjustment(), bucket.cadenceAdjustment());
        graphics.drawCenteredString(font, Component.literal(corrections), center, top + 106, 0xB8E6B8);

        String effectiveText = String.format(Locale.ROOT,
            "Effective: cycle %.2f • lead %+.2f • rot %.2f • cad %+.2f",
            effective.cycleScale(), effective.leadOffset(), effective.rotationScale(), effective.cadenceBias());
        graphics.drawCenteredString(font, Component.literal(effectiveText), center, top + 122, 0xBBBBBB);

        String guard = "Profiles " + state(config.networkProfiles()) + " • Guard " + state(config.networkGuard())
            + " • Global RB " + global.rollbacks() + " • Bucket RB " + bucket.rollbacks();
        graphics.drawCenteredString(font, Component.literal(guard), center, top + 138, 0xFFFFD28A);

        if (config.trainingMode()) {
            String training = String.format(Locale.ROOT,
                "TRAINING: %d cycles • %.1f%% success • Q %.1f%% • %s",
                TrainingSession.cycles(), TrainingSession.successRate() * 100.0D,
                TrainingSession.qualityEma() * 100.0D, TrainingSession.lastCondition().displayName());
            graphics.drawCenteredString(font, Component.literal(training), center, top + 152, 0xFFFFD166);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private NetworkCondition activeCondition(BridgeConfig config) {
        if (BridgeEngine.active()) return BridgeEngine.activeNetworkCondition();
        return LearningEngine.networkCondition(serverId(), config.technique(), config.confirmationTicks());
    }

    private String serverId() {
        return ServerContext.id(Minecraft.getInstance());
    }

    private static String state(boolean value) {
        return value ? "ВКЛ" : "ВЫКЛ";
    }

    @Override
    public void onClose() {
        BridgeConfig.get().save();
        LearningEngine.flush();
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
