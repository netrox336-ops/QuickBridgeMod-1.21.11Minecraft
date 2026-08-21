package dev.netrox.quickbridge;

import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(QuickBridge.MODID)
public final class QuickBridge {
    public static final String MODID = "quickbridge";

    public QuickBridge(FMLJavaModLoadingContext context) {
        RegisterKeyMappingsEvent.BUS.addListener(QuickBridgeClient::registerKeys);
        TickEvent.ClientTickEvent.Post.BUS.addListener(QuickBridgeClient::onClientTick);
        AddGuiOverlayLayersEvent.BUS.addListener(QuickBridgeClient::addHudLayer);
    }
}
