package com.mschiller890.index.client.screens;

import com.mschiller890.index.client.helpers.ResultList;
import com.mschiller890.index.network.SearchItemsC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SearchListScreen extends Screen {

    private EditBox searchBox;
    private ResultList results;
    private Button searchButton;

    private final Map<Item, ItemStack> savedItems;

    private final Screen parentScreen;

    private int titleY;
    private int centerX;

    protected SearchListScreen(Component title, Screen parentScreen, Map<Item, ItemStack> savedItems) {
        super(title);
        this.parentScreen = parentScreen;
        this.savedItems = savedItems;
    }

    @Override
    protected void init() {
        super.init();

        centerX = this.width / 2;
        int centerY = this.height / 2;

        titleY = centerY - 100;

        /*
         * SEARCH BOX
         */
        this.searchBox = new EditBox(
                this.font,
                centerX - 100,
                centerY - 85,
                200,
                20,
                Component.literal("Search list")
        );
        this.searchBox.setHint(Component.literal("Filter saved items..."));
        this.searchBox.setResponder(value -> populateResults());
        this.addRenderableWidget(searchBox);
        this.setInitialFocus(searchBox);

        /*
         * RESULTS LIST
         */
        int listTop = centerY - 50;
        int listBottom = this.height - 60;
        int listHeight = Math.max(20, listBottom - listTop);

        this.results = new ResultList(
                this,
                this.width,
                listHeight,
                listTop,
                20,
                ResultList.EntryStyle.REMOVE_BUTTON,
                (stack, checked) -> {
                    savedItems.remove(stack.getItem());
                    Minecraft.getInstance().execute(() -> {
                        populateResults();
                        updateSearchButtonState();
                    });
                }
        );
        this.addRenderableWidget(results);

        populateResults();

        /*
         * SEARCH BUTTON
         */
        Button searchButton = Button.builder(
                        Component.literal("Search for items in chests"),
                        ignored -> {
                            List<Identifier> ids = new ArrayList<>();
                            for (Item item : savedItems.keySet()) {
                                ids.add(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item));
                            }

                            ClientPlayNetworking.send(
                                    new SearchItemsC2SPayload(ids)
                            );
                        }
                )
                .bounds(centerX - 90, centerY + 80, 180, 20)
                .build();
        this.searchButton = searchButton;
        updateSearchButtonState();
        this.addRenderableWidget(searchButton);

        /*
         * CLEAR LIST BUTTON
         */
        Button clearListButton = Button.builder(
                        Component.literal("Clear list"),
                        ignored -> Minecraft.getInstance().gui.setScreen(
                                new ConfirmScreen(
                                        confirmed -> {
                                            if (confirmed) {
                                                savedItems.clear();
                                                results.clearResults();
                                                updateSearchButtonState();
                                            }
                                            Minecraft.getInstance().gui.setScreen(this);
                                        },
                                        Component.literal("Clear List"),
                                        Component.literal("Are you sure you want to clear the list of selected items?")
                                )
                        )
                )
                .bounds(10, this.height - 30, 80, 20)
                .build();
        this.addRenderableWidget(clearListButton);

        /*
         * BACK BUTTON
         */
        Button backButton = Button.builder(
                        Component.literal("Back"),
                        ignored -> Minecraft.getInstance().gui.setScreen(parentScreen)
                )
                .bounds(this.width - 70, this.height - 30, 60, 20)
                .build();
        this.addRenderableWidget(backButton);
    }

    private void populateResults() {
        String query = searchBox.getValue().trim().toLowerCase(Locale.ROOT);
        results.clearResults();

        for (ItemStack stack : savedItems.values()) {
            String name = stack.getHoverName().getString().toLowerCase(Locale.ROOT);

            if (query.isEmpty() || name.contains(query)) {
                results.add(stack, true);
            }
        }
    }

    private void updateSearchButtonState() {
        if (searchButton != null) {
            searchButton.active = !savedItems.isEmpty();
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);

        graphics.text(
                this.font,
                this.title,
                centerX - (this.font.width(this.title) / 2),
                titleY,
                0xFFFFFFFF
        );
    }
}