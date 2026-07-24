package com.mschiller890.index.client.screens;

import com.mschiller890.index.client.helpers.ResultList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SearchForItemScreen extends Screen {

    private static final int MAX_RESULTS = 200;

    private EditBox searchBox;
    private ResultList results;

    private final Map<Item, ItemStack> savedItems = new LinkedHashMap<>();

    private int titleY;
    private int centerX;

    public SearchForItemScreen(Component title) {
        super(title);
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
                Component.literal("Search for Item")
        );
        this.searchBox.setHint(Component.literal("Type item name..."));
        this.searchBox.setResponder(value -> performSearch());
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
                (stack, checked) -> {
                    if (checked) {
                        savedItems.put(stack.getItem(), stack.copy());
                    } else {
                        savedItems.remove(stack.getItem());
                    }
                }
        );
        this.addRenderableWidget(results);

        /*
         * SEARCH LIST BUTTON
         */
        Button searchListButton = Button.builder(
                        Component.literal("Search list"),
                        ignored -> Minecraft.getInstance().gui.setScreen(new SearchListScreen(Component.literal("Search List"), this, savedItems))
                )
                .bounds(10, this.height - 30, 80, 20)
                .build();
        this.addRenderableWidget(searchListButton);

        /*
         * CANCEL BUTTON
         */
        Button cancelButton = Button.builder(
                        Component.literal("Cancel"),
                        ignored -> Minecraft.getInstance().gui.setScreen(null)
                )
                .bounds(this.width - 70, this.height - 30, 60, 20)
                .build();
        this.addRenderableWidget(cancelButton);

        performSearch();
    }


    private void performSearch() {
        String query = searchBox.getValue().trim().toLowerCase(Locale.ROOT);
        results.clearResults();

        List<ItemStack> matches = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) continue;

            String path = BuiltInRegistries.ITEM.getKey(item).getPath();
            if (query.isEmpty() || path.toLowerCase(Locale.ROOT).contains(query)) {
                ItemStack candidate = new ItemStack(item, 1);
                if (candidate.isEmpty()) continue;

                matches.add(candidate);
                if (matches.size() >= MAX_RESULTS) break;
            }
        }

        for (ItemStack stack : matches) {
            results.add(stack, savedItems.containsKey(stack.getItem()));
        }
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        graphics.text(
                this.font,
                this.title,
                centerX - (this.font.width(this.title) / 2),
                titleY,
                0xFFFFFFFF
        );
    }
}