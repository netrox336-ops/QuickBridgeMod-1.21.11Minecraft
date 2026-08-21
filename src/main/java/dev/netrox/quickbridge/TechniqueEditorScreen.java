package dev.netrox.quickbridge;

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
        int top = Math.max(34, height / 2 - 104);

        addRenderableWidget(Button.builder(
            Component.literal("Техника: " + config.technique().displayName()),
            button -> {
                config.nextTechnique();
                minecraft.setScreen(new TechniqueEditorScreen(parent));
            }
        ).bounds(center - 154, top, 308, 20).build());

        addPair(center, top + 32,
            () -> config.adjustCycleScale(config.technique(), -0.05D),
            () -> config.adjustCycleScale(config.technique(), 0.05D));
        addPair(center, top + 60,
            () -> config.adjustLeadOffset(config.technique(), -0.05D),
            () -> config.adjustLeadOffset(config.technique(), 0.05D));
        addPair(center, top + 88,
            () -> config.adjustRotationScale(config.technique(), -0.10F),
            () -> config.adjustRotationScale(config.technique(), 0.10F));
        addPair(center, top + 116,
            () -> config.adjustCadenceBias(config.technique(), -0.05D),
            () -> config.adjustCadenceBias(config.technique(), 0.05D));

        addRenderableWidget(Button.builder(
            Component.literal("Сбросить профиль"),
            button -> config.resetTuning(config.technique())
        ).bounds(center - 154, top + 150, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Готово"),
            button -> onClose()
        ).bounds(center + 4, top + 150, 150, 20).build());
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
        int center = width / 2;
        int top = Math.max(34, height / 2 - 104);

        graphics.drawCenteredString(font, title, center, 10, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.literal("Настройки сохраняются отдельно для каждой техники"), center, 22, 0xA0A0A0);
        graphics.drawCenteredString(font, Component.literal("Длина цикла: " + percent(tuning.cycleScale())), center, top + 38, 0xE6E6E6);
        graphics.drawCenteredString(font, Component.literal("Упреждение блока: " + signed(tuning.leadOffset())), center, top + 66, 0xE6E6E6);
        graphics.drawCenteredString(font, Component.literal("Скорость поворота: " + percent(tuning.rotationScale())), center, top + 94, 0xE6E6E6);
        graphics.drawCenteredString(font, Component.literal("Cadence bias: " + signed(tuning.cadenceBias())), center, top + 122, 0xE6E6E6);
        super.render(graphics, mouseX, mouseY, partialTick);
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
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
