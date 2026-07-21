package com.mschiller890.index.client;

import com.mschiller890.index.client.helpers.ChestColorManager;
import com.mschiller890.index.client.helpers.ChestColorStorage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.storage.LevelResource;

public final class ChestColorPersistence {

    private static String activeWorldId;

    private ChestColorPersistence() {}

    public static void register() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            activeWorldId = resolveWorldId(client);
            ChestColorStorage.load(activeWorldId);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (activeWorldId != null) {
                ChestColorStorage.save(activeWorldId);
            }
            ChestColorManager.clearAll();
            activeWorldId = null;
        });
    }

    public static void saveActiveWorld() {
        if (activeWorldId != null) {
            ChestColorStorage.save(activeWorldId);
        }
    }

    private static String resolveWorldId(Minecraft client) {
        IntegratedServer server = client.getSingleplayerServer();
        if (server != null) {
            return "sp_" + sanitize(server.getWorldPath(LevelResource.ROOT).getFileName().toString());
        }

        ServerData serverData = client.getCurrentServer();
        if (serverData != null) {
            return "mp_" + sanitize(serverData.ip);
        }

        return "unknown_world";
    }

    private static String sanitize(String input) {
        return input.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
