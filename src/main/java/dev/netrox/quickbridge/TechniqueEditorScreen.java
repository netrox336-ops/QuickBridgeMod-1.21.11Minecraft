package dev.netrox.quickbridge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public final class TechniqueEditorScreen extends Screen {
    private final Screen parent;

    public TechniqueEditorScreen(Screen parent) {
        super(Component.literal("Калибровка QuickBridge"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        BridgeConfig config = BridgeConfig.get();
        int center = width / 2;
        int top = Math.max(28, height / 2 - 108);

        addRenderableWidget(Button.builder(
            Component.literal("Техника: " + config.technique().displayName()),
            button -> {
                config.nextTechnique();
                minecraft.setScreen(new TechniqueEditorScreen(parent));
            }
        ).bounds(center - 154, top, 308, 20).build());

        addPair(center, top + 30,
            () -> config.adjustCycleScale(config.technique(), -0.05D),
            () -> config.adjustCycleScale(config.technique(), 0.05D));
        addPair(center, top + 56,
            () -> config.adjustLeadOffset(config.technique(), -0.05D),
            () -> config.adjustLeadOffset(config.technique(), 0.05D));
        addPair(center, top + 82,
            () -> config.adjustRotationScale(config.technique(), -0.10F),
            () -> config.adjustRotationScale(config.technique(), 0.10F));
        addPair(center, top + 108,
            () -> config.adjustCadenceBias(config.technique(), -0.05D),
            () -> config.adjustCadenceBias(config.technique(), 0.05D));

        addRenderableWidget(Button.builder(
            Component.literal("Сбросить ручной профиль"),
            button -> config.resetTuning(config.technique())
        ).bounds(center - 154, top + 150, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Сбросить обучение"),
            button -> LearningEngine.reset(serverId(), config.technique())
        ).bounds(center + 4, top + 150, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Статистика обучения"),
            button -> minecraft.setScreen(new LearningStatsScreen(this))
        ).bounds(center - 154, top + 174, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Готово"),
            button -> onClose()
        ).bounds(center + 4, top + 174, 150, 20).build());
    }

    private void addPair(int center, int y, Runnable minus, Runnable plus) {
        addRenderableWidget(Button.builder(Component.literal("−"), button -> minus.run())
            .bounds(center - 154, y, 44, 20).build());
        addRenderableWidget(Button.builder(Component.literal("+"), button -> plus.run())
            .bounds(center + 110, y, 44, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        BridgeConfig config = BridgeConfig.get();
        TechniqueTuning tuning = config.tuning(config.technique());
        LearningProfile learned = LearningEngine.profile(serverId(), config.technique());
        TechniqueTuning effective = config.autoLearning() ? learned.applyTo(tuning) : tuning;
        TechniqueExecutionProfile execution = TechniqueExecutionProfile.forTechnique(config.technique());
        int center = width / 2;
        int top = Math.max(28, height / 2 - 108);

        graphics.drawCenteredString(font, title, center, 6, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.literal("Manual + learned + execution profile"), center, 18, 0xA0A0A0);
        graphics.drawCenteredString(font, Component.literal("Длина цикла: " + percent(tuning.cycleScale())), center, top + 36, 0xE6E6E6);
        graphics.drawCenteredString(font, Component.literal("Упреждение блока: " + signed(tuning.leadOffset())), center, top + 62, 0xE6E6E6);
        graphics.drawCenteredString(font, Component.literal("Скорость поворота: " + percent(tuning.rotationScale())), center, top + 88, 0xE6E6E6);
        graphics.drawCenteredString(font, Component.literal("Cadence bias: " + signed(tuning.cadenceBias())), center, top + 114, 0xE6E6E6);

        String learnedText = String.format(
            Locale.ROOT,
            "Learned: C %+.3f • L %+.3f • R %+.3f • Cad %+.3f • Conf %.0f%%",
            learned.cycleAdjustment(),
            learned.leadAdjustment(),
            learned.rotationAdjustment(),
            learned.cadenceAdjustment(),
            learned.confidence() * 100.0D
        );
        graphics.drawCenteredString(font, Component.literal(learnedText), center, top + 128, 0x9FD7FF);

        String executionText = String.format(
            Locale.ROOT,
            "Execution: window %.0f–%.0f%% • min speed %.3f • yaw ±%.0f° • pitch ±%.0f°",
            execution.placementStart() * 100.0D,
            execution.placementEnd() * 100.0D,
            execution.minBurstSpeed(),
            execution.yawTolerance(),
            execution.pitchTolerance()
        );
        graphics.drawCenteredString(font, Component.literal(executionText), center, top + 140,
            config.executionGuard() ? 0xFFFFD28A : 0xFF888888);

        String effectiveText = String.format(
            Locale.ROOT,
            "Effective: %.2f • %+.2f • %.2f • %+.2f",
            effective.cycleScale(),
            effective.leadOffset(),
            effective.rotationScale(),
            effective.cadenceBias()
        );
        graphics.drawCenteredString(font, Component.literal(effectiveText), center, top + 202, 0xB8E6B8);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private String serverId() {
        return ServerContext.id(Minecraft.getInstance());
    }

    private static String percent(double value) {
        return Math.round(value * 100.0D) + "%";
    }

    private static String signed(double value) {
        return String.format(Locale.ROOT, "%+.2f", value);
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
