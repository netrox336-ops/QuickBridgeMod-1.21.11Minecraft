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
        int top = Math.max(34, height / 2 - 108);

        addRenderableWidget(Button.builder(
            Component.literal("Техника: " + config.technique().displayName()),
            button -> {
                config.nextTechnique();
                minecraft.setScreen(new LearningStatsScreen(parent));
            }
        ).bounds(center - 154, top, 308, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Автообучение: " + state(config.autoLearning())),
            button -> {
                config.toggleAutoLearning();
                button.setMessage(Component.literal("Автообучение: " + state(config.autoLearning())));
            }
        ).bounds(center - 154, top + 28, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Тренировка: " + state(config.trainingMode())),
            button -> {
                config.toggleTrainingMode();
                button.setMessage(Component.literal("Тренировка: " + state(config.trainingMode())));
            }
        ).bounds(center + 4, top + 28, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Сбросить тренировку"),
            button -> TrainingSession.reset()
        ).bounds(center - 154, top + 142, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Сбросить технику"),
            button -> {
                LearningEngine.reset(serverId(), config.technique());
                minecraft.setScreen(new LearningStatsScreen(parent));
            }
        ).bounds(center + 4, top + 142, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Сбросить сервер"),
            button -> {
                LearningEngine.resetServer(serverId());
                minecraft.setScreen(new LearningStatsScreen(parent));
            }
        ).bounds(center - 154, top + 168, 150, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Готово"), button -> onClose())
            .bounds(center + 4, top + 168, 150, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        BridgeConfig config = BridgeConfig.get();
        LearningProfile profile = LearningEngine.profile(serverId(), config.technique());
        TechniqueTuning manual = config.tuning(config.technique());
        TechniqueTuning effective = config.autoLearning() ? profile.applyTo(manual) : manual;
        NetworkCondition condition = LearningEngine.networkCondition(serverId(), config.technique(), config.confirmationTicks());
        int center = width / 2;
        int top = Math.max(34, height / 2 - 108);

        graphics.drawCenteredString(font, title, center, 8, 0xFFFFFF);
        graphics.drawCenteredString(font,
            Component.literal("Сервер: " + ServerContext.shortLabel(ServerContext.label(Minecraft.getInstance()))),
            center, 20, 0xA0A0A0);

        String placementStats = String.format(Locale.ROOT,
            "Placements %d • Reliability %.1f%% • ACK %.2ft • Confidence %.0f%%",
            profile.samples(), profile.reliability() * 100.0D, profile.ackEma(), profile.confidence() * 100.0D);
        graphics.drawCenteredString(font, Component.literal(placementStats), center, top + 58, 0xE6E6E6);

        String cycleStats = String.format(Locale.ROOT,
            "Cycles %d • Success %.1f%% • Quality %.1f%% • Best %.1f%%",
            profile.cycles(), profile.cycleSuccessRate() * 100.0D,
            profile.cycleQualityEma() * 100.0D, profile.bestCycleQuality() * 100.0D);
        graphics.drawCenteredString(font, Component.literal(cycleStats), center, top + 74, 0xFFFFD28A);

        String network = "Network: " + condition.displayName() + " • Rollbacks: " + profile.rollbacks()
            + " • Checkpoint cycle: " + profile.checkpointCycle();
        graphics.drawCenteredString(font, Component.literal(network), center, top + 90, 0x9FD7FF);

        String learned = String.format(Locale.ROOT,
            "Learned: cycle %+.3f • lead %+.3f • rot %+.3f • cad %+.3f",
            profile.cycleAdjustment(), profile.leadAdjustment(),
            profile.rotationAdjustment(), profile.cadenceAdjustment());
        graphics.drawCenteredString(font, Component.literal(learned), center, top + 106, 0xB8E6B8);

        String effectiveText = String.format(Locale.ROOT,
            "Effective: cycle %.2f • lead %+.2f • rot %.2f • cad %+.2f",
            effective.cycleScale(), effective.leadOffset(), effective.rotationScale(), effective.cadenceBias());
        graphics.drawCenteredString(font, Component.literal(effectiveText), center, top + 122, 0xBBBBBB);

        if (config.trainingMode()) {
            String training = String.format(Locale.ROOT,
                "TRAINING: %d cycles • %.1f%% success • quality %.1f%% • %s",
                TrainingSession.cycles(), TrainingSession.successRate() * 100.0D,
                TrainingSession.qualityEma() * 100.0D, TrainingSession.lastCondition().displayName());
            graphics.drawCenteredString(font, Component.literal(training), center, top + 134, 0xFFFFD166);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
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
