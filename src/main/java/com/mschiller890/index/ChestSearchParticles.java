package com.mschiller890.index;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;

public final class ChestSearchParticles {
    private ChestSearchParticles() {}

    // /particle minecraft:end_rod x y z 0.4 0.4 0.4 0 3
    public static void spawnAt(ServerPlayer player, BlockPos pos) {
        player.level().sendParticles(
                player,
                ParticleTypes.END_ROD,
                false,
                true,
                pos.getX()+0.5,
                pos.getY()+0.5,
                pos.getZ()+0.5,
                3,
                0.4,0.4,0.4,
                0.0
        );
    }

}
