package com.mschiller890.index.mixin;

import com.mschiller890.index.ChestItemTracker;
import com.mschiller890.index.ColoredChestPositions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChestBlockEntity.class)
public abstract class ChestBlockEntityMixin {
    @Inject(method="setChanged", at = @At("TAIL"))
    private void index$onChanged(CallbackInfo ci) {
        ChestBlockEntity self = (ChestBlockEntity) (Object) this;
        if (self.getLevel() instanceof ServerLevel serverLevel) {
            ColoredChestPositions data = serverLevel.getDataStorage().computeIfAbsent(ColoredChestPositions.TYPE);
            if (data.isColored(self.getBlockPos())) {
                ChestItemTracker.refresh(serverLevel, self.getBlockPos());
            }
        }
    }
}
