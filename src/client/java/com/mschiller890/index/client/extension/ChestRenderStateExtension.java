package com.mschiller890.index.client.extension;

import net.minecraft.core.BlockPos;

public interface ChestRenderStateExtension {

    void index$setBlockPos(BlockPos pos);

    BlockPos index$getBlockPos();
}