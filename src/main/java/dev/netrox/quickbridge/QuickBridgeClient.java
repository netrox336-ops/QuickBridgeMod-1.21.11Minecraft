package dev.netrox.quickbridge;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import org.lwjgl.glfw.GLFW;

public final class QuickBridgeClient {
    private static final KeyMapping BRIDGE = new KeyMapping(
        "key.quickbridge.bridge", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, KeyMapping.Category.MISC
    );
    private static final KeyMapping NEXT_TECHNIQUE = new KeyMapping(
        "key.quickbridge.next_technique", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, KeyMapping.Category.MISC
    );
    private static final KeyMapping OPEN_SETTINGS = new KeyMapping(
        "key.quickbridge.settings", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, KeyMapping.Category.MISC
    );
    private static final KeyMapping EMERGENCY_STOP = new KeyMapping(
        "key.quickbridge.emergency_stop", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, KeyMapping.Category.MISC
    );

    private static boolean toggleState;

    private QuickBridgeClient() {}

    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(BRIDGE);
        event.register(NEXT_TECHNIQUE);
        event.register(OPEN_SETTINGS);
        event.register(EMERGENCY_STOP);
    }

    public static void onClientTick(TickEvent.ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        BridgeConfig config = BridgeConfig.get();

        while (OPEN_SETTINGS.consumeClick()) {
            BridgeEngine.stop(minecraft);
            toggleState = false;
            minecraft.setScreen(new QuickBridgeScreen(minecraft.screen));
        }

        while (NEXT_TECHNIQUE.consumeClick()) {
            BridgeEngine.stop(minecraft);
            toggleState = false;
            config.nextTechnique();
            if (minecraft.player != null) {
                minecraft.player.displayClientMessage(
                    Component.literal("QuickBridge: " + config.technique().displayName()), true
                );
            }
        }

        while (EMERGENCY_STOP.consumeClick()) {
            toggleState = false;
            BridgeEngine.stop(minecraft);
            if (minecraft.player != null) {
                minecraft.player.displayClientMessage(Component.literal("QuickBridge: STOP"), true);
            }
        }

        if (config.controlMode() == ControlMode.TOGGLE) {
            while (BRIDGE.consumeClick()) {
                toggleState = !toggleState;
                if (toggleState) BridgeEngine.start(minecraft);
                else BridgeEngine.stop(minecraft);
            }
        } else {
            boolean held = BRIDGE.isDown();
            if (held && !BridgeEngine.active()) BridgeEngine.start(minecraft);
            if (!held && BridgeEngine.active()) BridgeEngine.stop(minecraft);
        }

        BridgeEngine.tick(minecraft, config);
    }

    public static void addHudLayer(AddGuiOverlayLayersEvent event) {
        event.getLayeredDraw().add(
            Identifier.fromNamespaceAndPath(QuickBridge.MODID, "quickbridge_hud"),
            QuickBridgeClient::renderHud
        );
    }

    private static void renderHud(GuiGraphics graphics, DeltaTracker tracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) return;
        QuickBridgeHudRenderer.render(graphics, BridgeConfig.get());
    }
}
