package com.mschiller890.index.client.helpers;

import com.mschiller890.index.client.screens.SearchForItemScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ResultList extends ObjectSelectionList<ResultList.Entry> {

    private final SearchForItemScreen screen;

    public ResultList(
            SearchForItemScreen screen,
            int width,
            int height,
            int y,
            int itemHeight
    ) {
        super(Minecraft.getInstance(), width, height, y, itemHeight);
        this.screen = screen;
    }

    @Override
    public int getRowWidth() {
        return 200;
    }

    @Override
    protected int scrollBarX() {
        return this.getRowRight() + 2;
    }

    public void add(ItemStack stack, boolean checked) {
        this.addEntry(new Entry(stack, checked, this));
    }

    public void clearResults() {
        this.setSelected(null);
        this.clearEntries();
    }

    public ItemStack getSelectedStack() {
        Entry selected = this.getSelected();
        return selected != null ? selected.getStack() : null;
    }

    public static class Entry extends ObjectSelectionList.Entry<Entry> {

        private final ItemStack stack;
        private boolean checked;
        private final ResultList parent;

        public Entry(ItemStack stack, boolean checked, ResultList parent) {
            this.stack = stack;
            this.checked = checked;
            this.parent = parent;
        }

        @Override
        public void extractContent(
                GuiGraphicsExtractor graphics,
                int mouseX,
                int mouseY,
                boolean hovered,
                float delta
        ) {
            graphics.item(stack, this.getContentX(), this.getContentY());

            graphics.text(
                    Minecraft.getInstance().font,
                    stack.getHoverName(),
                    this.getContentX() + 20,
                    this.getContentYMiddle() - Minecraft.getInstance().font.lineHeight / 2,
                    0xFFFFFFFF
            );

            int boxSize = 9;
            int boxX = this.getContentRight() - boxSize;
            int boxY = this.getContentYMiddle() - boxSize / 2;

            graphics.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, 0xFFFFFFFF);
            graphics.fill(boxX + 1, boxY + 1, boxX + boxSize - 1, boxY + boxSize - 1, 0xFF000000);

            if (checked) {
                graphics.fill(boxX + 2, boxY + 2, boxX + boxSize - 2, boxY + boxSize - 2, 0xFF00AA00);
            }
        }

        @Override
        public boolean mouseClicked(
                net.minecraft.client.input.MouseButtonEvent event,
                boolean doubleClick
        ) {
            checked = !checked;
            return super.mouseClicked(event, doubleClick);
        }

        public ItemStack getStack() {
            return stack;
        }

        public boolean isChecked() {
            return checked;
        }

        @Override
        public Component getNarration() {
            return stack.getHoverName();
        }
    }
}