package com.mschiller890.index.client.mixin;

import com.mschiller890.index.client.extension.ChestRenderStateExtension;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ChestRenderState.class)
public class ChestRenderStateMixin implements ChestRenderStateExtension {

    @Unique
    private BlockPos index$blockPos;


    @Override
    public void index$setBlockPos(BlockPos pos) {
        this.index$blockPos = pos;
    }


    @Override
    public BlockPos index$getBlockPos() {
        return index$blockPos;
    }
}