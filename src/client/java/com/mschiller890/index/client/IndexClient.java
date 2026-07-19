package com.mschiller890.index.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mschiller890.index.client.screens.SearchForItemScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class IndexClient implements ClientModInitializer {

	public static KeyMapping openSearchKey;

	@Override
	public void onInitializeClient() {
		openSearchKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"[index] Open Search screen",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_SHIFT,
				KeyMapping.Category.MISC
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while(openSearchKey.consumeClick()) {
				if (client.player != null) {
					client.setScreenAndShow(new SearchForItemScreen(Component.literal("Search Index")));
				}
			}
		});
	}
}