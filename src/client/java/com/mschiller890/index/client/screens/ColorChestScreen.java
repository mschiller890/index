package com.mschiller890.index.client.screens;

import com.mschiller890.index.client.ChestColorPersistence;
import com.mschiller890.index.client.helpers.ChestColorManager;
import com.mschiller890.index.network.SetChestColorC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ColorChestScreen extends Screen {

    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int MARGIN = 6;
    private static final int LINE_HEIGHT = 10;

    private BlockPos targetPos;

    private ColorSlider redSlider;
    private ColorSlider greenSlider;
    private ColorSlider blueSlider;

    public ColorChestScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();

        HitResult hitResult = Minecraft.getInstance().hitResult;
        if (hitResult instanceof BlockHitResult blockHit && hitResult.getType() == HitResult.Type.BLOCK) {
            this.targetPos = blockHit.getBlockPos();
        }

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int startingRed = 255;
        int startingGreen = 255;
        int startingBlue = 255;
        if (targetPos != null) {
            int existing = ChestColorManager.getColor(targetPos, 0xFFFFFFFF);
            startingRed = (existing >> 16) & 0xFF;
            startingGreen = (existing >> 8) & 0xFF;
            startingBlue = (existing & 0xFF);
        }

        this.redSlider = new ColorSlider(centerX - 100, centerY - 45, "Red", startingRed);
        this.greenSlider = new ColorSlider(centerX - 100, centerY -20, "Green", startingGreen);
        this.blueSlider = new ColorSlider(centerX - 100, centerY + 5, "Blue", startingBlue);

        this.addRenderableWidget(redSlider);
        this.addRenderableWidget(greenSlider);
        this.addRenderableWidget(blueSlider);

        /*
         * APPLY BUTTON
         */
        Button applyButton = Button.builder(
                Component.literal("Apply color"),
                ignored -> {
                    if (targetPos != null) {
                        int color = currentColor();
                        System.out.println(
                                "Setting chest color at " + targetPos + " -> " + Integer.toHexString(color)
                        );

                        ChestColorManager.setColor(targetPos, currentColor());
                        ChestColorPersistence.saveActiveWorld();
                        ClientPlayNetworking.send(new SetChestColorC2SPayload(targetPos, true));                    }
                    Minecraft.getInstance().gui.setScreen(null);
                }
        )
                .bounds(centerX-50, centerY + 60, 100,20)
                .build();
        applyButton.active = targetPos != null;
        this.addRenderableWidget(applyButton);

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
    }

    private int currentColor() {
        return 0xFF000000
                | (redSlider.getColorValue() << 16)
                | (greenSlider.getColorValue() << 8)
                | blueSlider.getColorValue();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        Minecraft minecraft = Minecraft.getInstance();
        HitResult hitResult = minecraft.hitResult;

        int y = MARGIN;

        if (hitResult instanceof BlockHitResult blockHit && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = blockHit.getBlockPos();
            Level level = minecraft.level;

            Component blockName;
            if (level != null) {
                BlockState state = level.getBlockState(pos);
                blockName = state.getBlock().getName();
            } else {
                blockName = Component.literal("Unknown block");
            }

            graphics.text(this.font, blockName, MARGIN, y, TEXT_COLOR);
            y += LINE_HEIGHT;

            Component coords = Component.literal(pos.getX() + "," + pos.getY() + "," + pos.getZ());
            graphics.text(this.font, coords, MARGIN, y, TEXT_COLOR);
        } else {
            graphics.text(this.font, Component.literal("Not looking at a block!"), MARGIN, y, TEXT_COLOR);
        }

        if (redSlider != null) {
            int previewColor = currentColor();
            int centerX = this.width / 2;
            int centerY = this.height / 2;

            int x = centerX - 100;
            int yx = centerY + 30;
            int width = 200;
            int height = 16;

            graphics.fill(x,yx-2,x+width,yx+height, 0xFF000000);
            graphics.fill(x+1, yx-1,x+width-1,yx+height-1,previewColor);
        }
    }

    @Override
    public boolean isPauseScreen(){
        return true;
    }

    private static class ColorSlider extends AbstractSliderButton {

        private final String label;

        ColorSlider(int x, int y, String label, int stringValue) {
           super(x,y,200,18,Component.empty(),stringValue/255.0);
           this.label = label;
           updateMessage();
        }

        int getColorValue() {
            return (int) Math.round(this.value * 255.0);
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal(label + ": " + getColorValue()));
        }

        @Override
        protected void applyValue() {

        }
    }
}
