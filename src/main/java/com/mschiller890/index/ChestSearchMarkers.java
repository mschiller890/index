package com.mschiller890.index;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ChestSearchMarkers {
    private static final String MARKER_TAG = "index_marker";
    private static final double HEIGHT_OFFSET = 1;

    private static final Map<UUID, Map<BlockPos, Integer>> ACTIVE_MARKERS = new HashMap<>();

    private ChestSearchMarkers() {}

    public static void showMarkers(ServerLevel level, ServerPlayer player, Map<BlockPos, Item> matches) {
        clearMarkers(level, player);

        if (matches.isEmpty()) return;

        Map<BlockPos, Integer> spawned = new HashMap<>();
        for (Map.Entry<BlockPos, Item> entry : matches.entrySet()) {
            BlockPos pos = entry.getKey();

            ItemEntity marker = new ItemEntity(
              level,
              pos.getX() + 0.5,
              pos.getY() + HEIGHT_OFFSET,
              pos.getZ() + 0.5,
              new ItemStack(entry.getValue())
            );

            marker.setNeverPickUp();
            marker.setUnlimitedLifetime();
            marker.setNoGravity(true);
            marker.setDeltaMovement(Vec3.ZERO);
            marker.setInvulnerable(true);
            marker.addTag(MARKER_TAG);
            marker.addTag(playerTag(player));

            level.addFreshEntity(marker);
            spawned.put(pos.immutable(), marker.getId());
        }

        ACTIVE_MARKERS.put(player.getUUID(), spawned);
    }

    public static void clearMarkers(ServerLevel level, ServerPlayer player) {
        Map<BlockPos, Integer> existing = ACTIVE_MARKERS.remove(player.getUUID());
        if (existing == null) return;

        for (int entityId : existing.values()) {
            discard(level, entityId);
        }
    }

    public static void removeMarkerAt(ServerLevel level, ServerPlayer player, BlockPos pos) {
        Map<BlockPos, Integer> existing = ACTIVE_MARKERS.get(player.getUUID());
        if (existing == null) return;

        Integer entityId = existing.remove(pos);
        if (entityId != null) {
            discard(level, entityId);
        }

        if (existing.isEmpty()) {
            ACTIVE_MARKERS.remove(player.getUUID());
        }
    }

    public static void discard(ServerLevel level, int entityId) {
        Entity entity = level.getEntity(entityId);

        if (entity instanceof ItemEntity) {
            entity.discard();
        }
    }

    private static String playerTag(ServerPlayer player) {
        return "index_marker_" + player.getUUID();
    }
}
