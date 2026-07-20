package com.mschiller890.index.client.helpers;

import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class ChestColorManager {
    private static final Map<BlockPos, Integer> COLORS = new HashMap<>();

    private ChestColorManager() {
    }

    public static int getColor(BlockPos pos, int fallback) {
        return COLORS.getOrDefault(pos, fallback);
    }

    public static void setColor(BlockPos pos, int argbColor) {
        COLORS.put(pos.immutable(), argbColor);
    }

    public static void clearColor(BlockPos pos) {
        COLORS.remove(pos);
    }

    public static boolean hasColor(BlockPos pos) {
        return COLORS.containsKey(pos);
    }
}
