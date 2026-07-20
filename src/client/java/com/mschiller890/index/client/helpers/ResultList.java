package com.mschiller890.index.client.helpers;

import com.mschiller890.index.client.screens.SearchForItemScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;

public class ResultList extends ObjectSelectionList<ResultList.Entry> {

    private static final int REMOVE_COLOR = 0xFF5555;

    private final Screen screen;
    private final EntryStyle entryStyle;
    private final CheckedChangeListener checkedChangeListener;

    public ResultList(
            Screen screen,
            int width,
            int height,
            int y,
            int itemHeight
    ) {
        this(screen, width, height, y, itemHeight, EntryStyle.CHECKBOX, null);
    }

    public ResultList(
            Screen screen,
            int width,
            int height,
            int y,
            int itemHeight,
            CheckedChangeListener checkedChangeListener
    ) {
        this(screen, width, height, y, itemHeight, EntryStyle.CHECKBOX, checkedChangeListener);
    }

    public ResultList(
            Screen screen,
            int width,
            int height,
            int y,
            int itemHeight,
            EntryStyle entryStyle,
            CheckedChangeListener checkedChangeListener
    ) {
        super(Minecraft.getInstance(), width, height, y, itemHeight);
        this.screen = screen;
        this.entryStyle = entryStyle;
        this.checkedChangeListener = checkedChangeListener;
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

    public enum EntryStyle {
        CHECKBOX,
        REMOVE_BUTTON
    }

    public interface CheckedChangeListener {
        void onCheckedChanged(ItemStack stack, boolean checked);
    }

    public static class Entry extends ObjectSelectionList.Entry<Entry> {

        private static final int WIDGET_SIZE = 14;

        private final ItemStack stack;
        private final ResultList parent;
        private final AbstractWidget widget;
        private boolean checked;

        public Entry(ItemStack stack, boolean checked, ResultList parent) {
            this.stack = stack;
            this.parent = parent;
            this.checked = checked;

            if (parent.entryStyle == EntryStyle.REMOVE_BUTTON) {
                Component label = Component.literal("X")
                        .withStyle(style -> style.withColor(TextColor.fromRgb(REMOVE_COLOR)));
                this.widget = Button.builder(label, button -> remove())
                        .bounds(0, 0, WIDGET_SIZE, WIDGET_SIZE)
                        .build();
            } else {
                this.widget = Checkbox.builder(Component.empty(), Minecraft.getInstance().font)
                        .selected(checked)
                        .onValueChange((box, value) -> onCheckboxChanged(value))
                        .build();
            }
        }

        private void onCheckboxChanged(boolean value) {
            this.checked = value;
            if (parent.checkedChangeListener != null) {
                parent.checkedChangeListener.onCheckedChanged(stack, value);
            }
        }

        private void remove() {
            this.checked = false;
            if (parent.checkedChangeListener != null) {
                parent.checkedChangeListener.onCheckedChanged(stack, false);
            }
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

            int boxX = this.getContentRight() - widget.getWidth();
            int boxY = this.getContentYMiddle() - widget.getHeight() / 2;

            widget.setX(boxX);
            widget.setY(boxY);
            widget.extractRenderState(graphics, mouseX, mouseY, delta);
        }

        @Override
        public boolean mouseClicked(
                net.minecraft.client.input.MouseButtonEvent event,
                boolean doubleClick
        ) {
            if (widget.mouseClicked(event, doubleClick)) {
                return true;
            }
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