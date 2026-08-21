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
        int top = Math.max(42, height / 2 - 94);

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
            Component.literal("Learning HUD: " + state(config.learningHud())),
            button -> {
                config.toggleLearningHud();
                button.setMessage(Component.literal("Learning HUD: " + state(config.learningHud())));
            }
        ).bounds(center + 4, top + 28, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Сбросить технику"),
            button -> {
                LearningEngine.reset(serverId(), config.technique());
                minecraft.setScreen(new LearningStatsScreen(parent));
            }
        ).bounds(center - 154, top + 128, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Сбросить сервер"),
            button -> {
                LearningEngine.resetServer(serverId());
                minecraft.setScreen(new LearningStatsScreen(parent));
            }
        ).bounds(center + 4, top + 128, 150, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Готово"), button -> onClose())
            .bounds(center - 100, top + 156, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        BridgeConfig config = BridgeConfig.get();
        LearningProfile profile = LearningEngine.profile(serverId(), config.technique());
        TechniqueTuning manual = config.tuning(config.technique());
        TechniqueTuning effective = config.autoLearning() ? profile.applyTo(manual) : manual;
        int center = width / 2;
        int top = Math.max(42, height / 2 - 94);

        graphics.drawCenteredString(font, title, center, 10, 0xFFFFFF);
        graphics.drawCenteredString(
            font,
            Component.literal("Сервер: " + ServerContext.shortLabel(ServerContext.label(Minecraft.getInstance()))),
            center,
            22,
            0xA0A0A0
        );

        String stats = String.format(
            Locale.ROOT,
            "Samples %d • Success %.1f%% • ACK %.2ft • Confidence %.0f%%",
            profile.samples(),
            profile.reliability() * 100.0D,
            profile.ackEma(),
            profile.confidence() * 100.0D
        );
        graphics.drawCenteredString(font, Component.literal(stats), center, top + 58, 0xE6E6E6);

        String learned = String.format(
            Locale.ROOT,
            "Learned: cycle %+.3f • lead %+.3f • rot %+.3f • cad %+.3f",
            profile.cycleAdjustment(),
            profile.leadAdjustment(),
            profile.rotationAdjustment(),
            profile.cadenceAdjustment()
        );
        graphics.drawCenteredString(font, Component.literal(learned), center, top + 74, 0x9FD7FF);

        String effectiveText = String.format(
            Locale.ROOT,
            "Effective: cycle %.2f • lead %+.2f • rot %.2f • cad %+.2f",
            effective.cycleScale(),
            effective.leadOffset(),
            effective.rotationScale(),
            effective.cadenceBias()
        );
        graphics.drawCenteredString(font, Component.literal(effectiveText), center, top + 90, 0xB8E6B8);

        graphics.drawCenteredString(
            font,
            Component.literal("Recovery success: " + profile.recoveredSuccesses() + " • Failures: " + profile.failures()),
            center,
            top + 106,
            0xBBBBBB
        );
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
