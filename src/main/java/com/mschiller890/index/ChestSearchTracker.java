package com.mschiller890.index;

import net.minecraft.core.BlockPos;

import java.util.*;

public final class ChestSearchTracker {

    private static final Map<UUID, Set<BlockPos>> ACTIVE = new HashMap<>();

    private ChestSearchTracker() {}

    public static void setSearch(UUID playerId, Set<BlockPos> positions) {
        if (positions.isEmpty()) {
            ACTIVE.remove(playerId);
        } else {
            ACTIVE.put(playerId, new HashSet<>(positions));
        }
    }

    public static Set<BlockPos> getSearch(UUID playerId) {
        return ACTIVE.getOrDefault(playerId, Set.of());
    }

    public static void markOpened(UUID playerId, BlockPos pos) {
        Set<BlockPos> set = ACTIVE.get(playerId);

        if (set != null) {
            set.remove(pos);
            if (set.isEmpty()) {
                ACTIVE.remove(playerId);
            }
        }
    }

    public static void clear(UUID playerId) {
        ACTIVE.remove(playerId);
    }

}
