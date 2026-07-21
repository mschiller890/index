package com.mschiller890.index;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.*;

public final class ChestItemTracker {
    private static final Map<ServerLevel, Map<BlockPos, Map<Item, Integer>>> INDEX = new HashMap<>();

    private ChestItemTracker() {}

    public static void refresh(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof Container container)) {
            untrack(level, pos);
            return;
        }

        Map<Item, Integer> counts = new HashMap<>();
        for (int i = 0; i<container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                counts.merge(stack.getItem(), stack.getCount(), Integer::sum);
            }
        }

        INDEX.computeIfAbsent(level, l -> new HashMap<>()).put(pos.immutable(), counts);
    }

    public static void untrack(ServerLevel level, BlockPos pos) {
        Map<BlockPos, Map<Item, Integer>> levelMap = INDEX.get(level);
        if (levelMap != null) {
            levelMap.remove(pos);
        }
    }

    public static void untrackLevel(ServerLevel level) {
        INDEX.remove(level);
    }

    public static List<Map.Entry<BlockPos, Integer>> findLocations(ServerLevel level, Item item) {
        Map<BlockPos, Map<Item, Integer>> levelmap = INDEX.get(level);
        if (levelmap == null) return List.of();

        List<Map.Entry<BlockPos, Integer>> results = new ArrayList<>();
        for (Map.Entry<BlockPos, Map<Item, Integer>> e : levelmap.entrySet()) {
            Integer count = e.getValue().get(item);
            if (count != null && count > 0) {
                results.add(Map.entry(e.getKey(), count));
            }
        }
        return results;
    }

    public static void trackAll(ServerLevel level, Collection<BlockPos> positions) {
        for (BlockPos pos : positions) {
            refresh(level, pos);
        }
    }
}
