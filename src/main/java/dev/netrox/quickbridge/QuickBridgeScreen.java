package dev.netrox.quickbridge;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class QuickBridgeScreen extends Screen {
    private final Screen parent;

    public QuickBridgeScreen(Screen parent) {
        super(Component.literal("QuickBridge"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        BridgeConfig config = BridgeConfig.get();
        int center = width / 2;
        int top = Math.max(12, height / 2 - 128);
        int step = 20;

        addRenderableWidget(Button.builder(
            Component.literal("Техника: " + config.technique().displayName()),
            button -> {
                config.nextTechnique();
                button.setMessage(Component.literal("Техника: " + config.technique().displayName()));
            }
        ).bounds(center - 154, top, 308, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Режим: " + config.controlMode().displayName()),
            button -> {
                config.nextControlMode();
                button.setMessage(Component.literal("Режим: " + config.controlMode().displayName()));
            }
        ).bounds(center - 154, top + step, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("HUD: " + state(config.hudEnabled())),
            button -> {
                config.toggleHud();
                button.setMessage(Component.literal("HUD: " + state(config.hudEnabled())));
            }
        ).bounds(center + 4, top + step, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Автообучение: " + state(config.autoLearning())),
            button -> {
                config.toggleAutoLearning();
                button.setMessage(Component.literal("Автообучение: " + state(config.autoLearning())));
            }
        ).bounds(center - 154, top + step * 2, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Тренировка: " + state(config.trainingMode())),
            button -> {
                config.toggleTrainingMode();
                button.setMessage(Component.literal("Тренировка: " + state(config.trainingMode())));
            }
        ).bounds(center + 4, top + step * 2, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Network Profiles: " + state(config.networkProfiles())),
            button -> {
                config.toggleNetworkProfiles();
                button.setMessage(Component.literal("Network Profiles: " + state(config.networkProfiles())));
            }
        ).bounds(center - 154, top + step * 3, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Network Guard: " + state(config.networkGuard())),
            button -> {
                config.toggleNetworkGuard();
                button.setMessage(Component.literal("Network Guard: " + state(config.networkGuard())));
            }
        ).bounds(center + 4, top + step * 3, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Статистика обучения"),
            button -> minecraft.setScreen(new LearningStatsScreen(this))
        ).bounds(center - 154, top + step * 4, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Learning HUD: " + state(config.learningHud())),
            button -> {
                config.toggleLearningHud();
                button.setMessage(Component.literal("Learning HUD: " + state(config.learningHud())));
            }
        ).bounds(center + 4, top + step * 4, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Автовыбор: " + state(config.autoSelectBlocks())),
            button -> {
                config.toggleAutoSelectBlocks();
                button.setMessage(Component.literal("Автовыбор: " + state(config.autoSelectBlocks())));
            }
        ).bounds(center - 154, top + step * 5, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Sneak assist: " + state(config.sneakAssist())),
            button -> {
                config.toggleSneakAssist();
                button.setMessage(Component.literal("Sneak assist: " + state(config.sneakAssist())));
            }
        ).bounds(center + 4, top + step * 5, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Recovery: " + state(config.placementRecovery())),
            button -> {
                config.togglePlacementRecovery();
                button.setMessage(Component.literal("Recovery: " + state(config.placementRecovery())));
            }
        ).bounds(center - 154, top + step * 6, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Вернуть камеру: " + state(config.restoreView())),
            button -> {
                config.toggleRestoreView();
                button.setMessage(Component.literal("Вернуть камеру: " + state(config.restoreView())));
            }
        ).bounds(center + 4, top + step * 6, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Fail-stop: " + state(config.stopOnPlacementFailures())),
            button -> {
                config.toggleStopOnPlacementFailures();
                button.setMessage(Component.literal("Fail-stop: " + state(config.stopOnPlacementFailures())));
            }
        ).bounds(center - 154, top + step * 7, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Диагностика HUD: " + state(config.diagnosticHud())),
            button -> {
                config.toggleDiagnosticHud();
                button.setMessage(Component.literal("Диагностика HUD: " + state(config.diagnosticHud())));
            }
        ).bounds(center + 4, top + step * 7, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Повторы: " + config.maxRecoveryAttempts()),
            button -> {
                config.nextRecoveryAttempts();
                button.setMessage(Component.literal("Повторы: " + config.maxRecoveryAttempts()));
            }
        ).bounds(center - 154, top + step * 8, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Подтверждение: " + config.confirmationTicks() + "t"),
            button -> {
                config.nextConfirmationTicks();
                button.setMessage(Component.literal("Подтверждение: " + config.confirmationTicks() + "t"));
            }
        ).bounds(center + 4, top + step * 8, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Ручная калибровка"),
            button -> minecraft.setScreen(new TechniqueEditorScreen(this))
        ).bounds(center - 154, top + step * 9, 308, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Готово"), button -> onClose())
            .bounds(center - 100, top + step * 10, 200, 20).build());
    }

    private static String state(boolean value) {
        return value ? "ВКЛ" : "ВЫКЛ";
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 2, 0xFFFFFF);
        graphics.drawCenteredString(font,
            Component.literal("v0.6 • conditioned learning • hysteresis • network guard"),
            width / 2, 13, 0xA0A0A0);
        super.render(graphics, mouseX, mouseY, partialTick);
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
