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
        int top = Math.max(42, height / 2 - 92);

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
        ).bounds(center - 154, top + 26, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("HUD: " + state(config.hudEnabled())),
            button -> {
                config.toggleHud();
                button.setMessage(Component.literal("HUD: " + state(config.hudEnabled())));
            }
        ).bounds(center + 4, top + 26, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Автовыбор блоков: " + state(config.autoSelectBlocks())),
            button -> {
                config.toggleAutoSelectBlocks();
                button.setMessage(Component.literal("Автовыбор блоков: " + state(config.autoSelectBlocks())));
            }
        ).bounds(center - 154, top + 52, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Sneak assist: " + state(config.sneakAssist())),
            button -> {
                config.toggleSneakAssist();
                button.setMessage(Component.literal("Sneak assist: " + state(config.sneakAssist())));
            }
        ).bounds(center + 4, top + 52, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Stop on fall: " + state(config.stopOnFall())),
            button -> {
                config.toggleStopOnFall();
                button.setMessage(Component.literal("Stop on fall: " + state(config.stopOnFall())));
            }
        ).bounds(center - 154, top + 78, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Stop on GUI: " + state(config.stopOnGui())),
            button -> {
                config.toggleStopOnGui();
                button.setMessage(Component.literal("Stop on GUI: " + state(config.stopOnGui())));
            }
        ).bounds(center + 4, top + 78, 150, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Готово"), button -> onClose())
            .bounds(center - 100, top + 112, 200, 20).build());
    }

    private static String state(boolean value) {
        return value ? "ВКЛ" : "ВЫКЛ";
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 16, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.literal("B — мост • N — техника • K — аварийный стоп • O — настройки"), width / 2, 29, 0xA0A0A0);
        super.render(graphics, mouseX, mouseY, partialTick);
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
