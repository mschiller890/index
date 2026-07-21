package com.mschiller890.index;

import com.mschiller890.index.network.SetChestColorC2SPayload;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;

import net.minecraft.server.level.ServerLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Index implements ModInitializer {
	public static final String MOD_ID = "index";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");
		LOGGER.info("index is running!");

		LOGGER.info("Registering payload...");
		PayloadTypeRegistry.serverboundPlay().register(SetChestColorC2SPayload.TYPE, SetChestColorC2SPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(SetChestColorC2SPayload.TYPE, (payload, context) -> {
			context.server().execute(() -> {
				ServerLevel level = context.player().level();
				ColoredChestPositions data = level.getDataStorage().computeIfAbsent(ColoredChestPositions.TYPE);

				if (payload.colored()) {
					data.add(payload.pos());
					ChestItemTracker.refresh(level, payload.pos());
				} else {
					data.remove(payload.pos());
					ChestItemTracker.untrack(level, payload.pos());
				}
			});
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			for (ServerLevel level : server.getAllLevels()) {
				ChestItemTracker.untrackLevel(level);
			}
		});

		ServerLevelEvents.LOAD.register((server, level) -> {
			ColoredChestPositions data = level.getDataStorage().computeIfAbsent(ColoredChestPositions.TYPE);
			ChestItemTracker.trackAll(level, data.all());
		});
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
