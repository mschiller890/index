package com.mschiller890.index.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mschiller890.index.client.screens.ColorChestScreen;
import com.mschiller890.index.client.screens.SearchForItemScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.lwjgl.glfw.GLFW;

public class IndexClient implements ClientModInitializer {

	public static KeyMapping openSearchKey;
	private boolean wasPressed = false;

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

		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
//			Level level = Minecraft.getInstance().level;

			if (!level.isClientSide()) {
				return InteractionResult.PASS;
			}

			Minecraft client = Minecraft.getInstance();
			long handle = client.getWindow().handle();

			boolean ctrl =
					GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS;
			boolean shift =
					GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;

//			boolean pressed = ctrl && shift && client.options.keyUse.isDown();
//
//			if (pressed && !wasPressed) {
//				client.setScreenAndShow(new ColorChestScreen(Component.literal("Color Chest")));
//			}
//
//			wasPressed = pressed;

			if (!ctrl || !shift) {
				return InteractionResult.PASS;
			}

			if (level.getBlockEntity(hitResult.getBlockPos()) instanceof ChestBlockEntity) {
				client.setScreenAndShow(new ColorChestScreen(Component.literal("Color Chest")));
				return InteractionResult.FAIL;
			}

			return InteractionResult.PASS;
		});
	}
}