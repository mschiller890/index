package com.mschiller890.index;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ColoredChestPositions extends SavedData {

    private final Set<BlockPos> positions = new HashSet<>();

    private static final Codec<ColoredChestPositions> CODEC = BlockPos.CODEC.listOf().xmap(
            list -> {
                ColoredChestPositions data = new ColoredChestPositions();
                data.positions.addAll(list);
                return data;
            },
            data -> new ArrayList<>(data.positions)
    );

    public static final SavedDataType<ColoredChestPositions> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("index", "colored_chests"),
            ColoredChestPositions::new,
            CODEC,
            null
    );

    public boolean add(BlockPos pos) { boolean c = positions.add(pos.immutable()); if (c) setDirty(); return c; }
    public boolean remove(BlockPos pos) { boolean c = positions.remove(pos); if (c) setDirty(); return c; }
    public boolean isColored(BlockPos pos) { return positions.contains(pos); }
    public Set<BlockPos> all() { return positions; }
}