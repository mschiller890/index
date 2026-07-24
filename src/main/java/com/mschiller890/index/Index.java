package com.mschiller890.index;

import com.mschiller890.index.network.SearchItemsC2SPayload;
import com.mschiller890.index.network.SetChestColorC2SPayload;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.injection.struct.InjectorGroupInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

		PayloadTypeRegistry.serverboundPlay().register(SetChestColorC2SPayload.TYPE, SetChestColorC2SPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(SearchItemsC2SPayload.TYPE, SearchItemsC2SPayload.CODEC);

		LOGGER.info("Registered network payloads");

		ServerPlayNetworking.registerGlobalReceiver(SearchItemsC2SPayload.TYPE, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayer player = context.player();
				ServerLevel level = player.level();

				LOGGER.info("{} search for {} item(s)", player.getGameProfile().name(), payload.itemIds().size());

				Map<BlockPos, Item> matches = new HashMap<>();
				for (Identifier id : payload.itemIds()) {
					Item item = BuiltInRegistries.ITEM.getValue(id);
					if (item == null || item == Items.AIR) {
						LOGGER.warn("Ignored invalid item '{}'", id);
						continue;
					}

					LOGGER.debug("Searching for '{}", id);

					for (Map.Entry<BlockPos, Integer> entry : ChestItemTracker.findLocations(level, item)) {
						matches.putIfAbsent(entry.getKey(), item);
					}
				}

				LOGGER.info("Found {} matching chest(s) for {}", matches.size(), player.getGameProfile().name());

				ChestSearchTracker.setSearch(player.getUUID(), matches.keySet());
				ChestSearchMarkers.showMarkers(level, player, matches);
			});
		});

		int[] tickCounter = {0};
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			tickCounter[0]++;
			if (tickCounter[0] % 10 != 0) return;

			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				for (BlockPos pos : ChestSearchTracker.getSearch(player.getUUID())) {
					ChestSearchParticles.spawnAt(player, pos);
				}
			}
		});

		UseBlockCallback.EVENT.register((player,world,hand,hitResult) -> {
			if (world.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
				return InteractionResult.PASS;
			}

			BlockPos pos = hitResult.getBlockPos();
			ChestSearchTracker.markOpened(serverPlayer.getUUID(), pos);
			ChestSearchMarkers.removeMarkerAt(serverPlayer.level(), serverPlayer, pos);
			return InteractionResult.PASS;
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
		{
			ChestSearchTracker.clear(handler.player.getUUID());
			ChestSearchMarkers.clearMarkers(handler.player.level(), handler.player);
		});

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

			LOGGER.debug("Tracked {} colored chest(s) on level load for {}", data.all().size(), level.dimension().identifier().toString());
		});

		ServerLevelEvents.UNLOAD.register((server, level) -> {
			ChestItemTracker.untrackLevel(level);
			LOGGER.debug("Untracked chest index for unloaded level {}", level.dimension().identifier().toString());
		});
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
