package com.mschiller890.index.client.mixin;

import com.mschiller890.index.client.extension.ChestRenderStateExtension;
import com.mschiller890.index.client.helpers.ChestColorManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChestRenderer.class)
public abstract class ChestRendererMixin<T extends BlockEntity & LidBlockEntity> {

    @Unique
    private ChestRenderState index$currentState;


    @Inject(
            method = "extractRenderState",
            at = @At("TAIL")
    )
    private void index$capturePosition(
            T blockEntity,
            ChestRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress,
            CallbackInfo ci
    ) {
        if (!(blockEntity instanceof EnderChestBlockEntity)) {
            ((ChestRenderStateExtension) state)
                    .index$setBlockPos(blockEntity.getBlockPos().immutable());
        }
    }


    @Inject(
            method = "submit",
            at = @At("HEAD")
    )
    private void index$captureState(
            ChestRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState camera,
            CallbackInfo ci
    ) {
        this.index$currentState = state;
    }
    
    @ModifyConstant(
            method = "submit",
            constant = @org.spongepowered.asm.mixin.injection.Constant(intValue = -1)
    )
    private int index$applyColor(int original) {

        if (index$currentState != null) {

            BlockPos pos =
                    ((ChestRenderStateExtension) index$currentState)
                            .index$getBlockPos();

            if (pos != null) {
                return ChestColorManager.getColor(
                        pos,
                        0xFFFFFFFF
                );
            }
        }

        return original;
    }
}