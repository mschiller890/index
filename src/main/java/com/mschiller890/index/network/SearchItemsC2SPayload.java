package com.mschiller890.index.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public record SearchItemsC2SPayload(List<Identifier> itemIds) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SearchItemsC2SPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("index","search_items"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SearchItemsC2SPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, Identifier.STREAM_CODEC), SearchItemsC2SPayload::itemIds,
            SearchItemsC2SPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
