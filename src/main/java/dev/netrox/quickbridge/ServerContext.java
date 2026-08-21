package dev.netrox.quickbridge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

import java.util.Locale;

public final class ServerContext {
    private ServerContext() {}

    public static String id(Minecraft minecraft) {
        if (minecraft == null) return "unknown";
        ServerData server = minecraft.getCurrentServer();
        if (server != null && server.ip != null && !server.ip.isBlank()) {
            return server.ip.trim().toLowerCase(Locale.ROOT);
        }
        if (minecraft.isLocalServer()) return "singleplayer";
        return "unknown";
    }

    public static String label(Minecraft minecraft) {
        if (minecraft == null) return "Unknown";
        ServerData server = minecraft.getCurrentServer();
        if (server != null) {
            if (server.name != null && !server.name.isBlank()) return server.name.trim();
            if (server.ip != null && !server.ip.isBlank()) return server.ip.trim();
        }
        if (minecraft.isLocalServer()) return "Singleplayer";
        return "Unknown";
    }

    public static String shortLabel(String value) {
        if (value == null || value.isBlank()) return "Unknown";
        return value.length() <= 24 ? value : value.substring(0, 21) + "...";
    }
}
