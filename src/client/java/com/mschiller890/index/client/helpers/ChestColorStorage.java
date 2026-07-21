package com.mschiller890.index.client.helpers;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import org.spongepowered.asm.mixin.injection.selectors.dynamic.IResolvedDescriptor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class ChestColorStorage {
    private static final Path BASE_DIR = FabricLoader.getInstance().getGameDir().resolve("index").resolve("chest_colors");

    private ChestColorStorage() {}

    public static void save(String worldId) {
        Map<BlockPos, Integer> colors = ChestColorManager.snapshot();
        CompoundTag root = new CompoundTag();
        ListTag entries = new ListTag();

        for (Map.Entry<BlockPos, Integer> e : colors.entrySet()) {
            BlockPos pos = e.getKey();
            CompoundTag tag = new CompoundTag();
            tag.putInt("x", pos.getX());
            tag.putInt("y", pos.getY());
            tag.putInt("z", pos.getZ());
            tag.putInt("color", e.getValue());
            entries.add(tag);
        }

        root.put("colors", entries);

        try {
            Files.createDirectories(BASE_DIR);
            NbtIo.writeCompressed(root, resolveFile(worldId));
        } catch (IOException e) {
            System.err.println("Could not save chest colors! " + e.getMessage());
        }
    }

    public static void load(String worldId) {
        Path file = resolveFile(worldId);
        if (!Files.exists(file)) {
            ChestColorManager.clearAll();
            return;
        }

        try {
            CompoundTag root = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
            ListTag entries = root.getList("colors").orElse(new ListTag());

            Map<BlockPos, Integer> loaded = new HashMap<>();
            for (int i = 0; i < entries.size(); i++) {
                CompoundTag tag = entries.getCompound(i).orElseThrow();
                int x = tag.getInt("x").orElse(0);
                int y = tag.getInt("y").orElse(0);
                int z = tag.getInt("z").orElse(0);
                int color = tag.getInt("color").orElse(0xFFFFFFFF);
                loaded.put(new BlockPos(x,y,z), color);
            }
            ChestColorManager.loadAll(loaded);
        } catch (IOException e) {
            System.err.println("Could not load chest colors! " + e.getMessage());
            ChestColorManager.clearAll();
        }
    }

    private static Path resolveFile(String worldId) {
        return BASE_DIR.resolve(worldId + ".dat");
    }
}
