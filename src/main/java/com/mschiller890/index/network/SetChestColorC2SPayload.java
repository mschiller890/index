package com.mschiller890.index.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SetChestColorC2SPayload(BlockPos pos, boolean colored) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SetChestColorC2SPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("index", "set_chest_color"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetChestColorC2SPayload> CODEC = StreamCodec.composite(
            net.minecraft.network.codec.ByteBufCodecs.fromCodec(BlockPos.CODEC), SetChestColorC2SPayload::pos,
            net.minecraft.network.codec.ByteBufCodecs.BOOL, SetChestColorC2SPayload::colored,
            SetChestColorC2SPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}