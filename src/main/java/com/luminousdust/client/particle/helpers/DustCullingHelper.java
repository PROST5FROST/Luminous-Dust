package com.luminousdust.client.particle.helpers;

import com.luminousdust.client.particle.DustParticle;
import com.luminousdust.config.LumDustConfCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;

public class DustCullingHelper {

    // Not Ready Yet.

    public static boolean shouldRemove(DustParticle particle, ClientLevel level, int tickOffset) {
        int min_block_light = LumDustConfCache.MIN_BLOCK_LIGHT;
        // Every 1 second we check everything here.
        if ((particle.getAge() + tickOffset) % 20 != 0) {
            return false;
        }

        BlockPos currentPos = BlockPos.containing(particle.getX(), particle.getY(), particle.getZ());

        // Water Check
        if (level.getFluidState(currentPos).is(FluidTags.WATER)) {
            return true;
        }

        // Min light check
        int blockLight = level.getBrightness(LightLayer.BLOCK, currentPos);
        if (blockLight < min_block_light) {
            return true;
        }

        // Daylight check
        long time = level.getDayTime() % 24000;
        boolean isDay = time < 13000 || time > 23000;
        if (isDay) {
            int skyLight = level.getBrightness(LightLayer.SKY, currentPos);
            if ((blockLight - skyLight) <= LumDustConfCache.DAYTIME_LIGHT_DIFF) {
                return true;
            }
        }

        // Hard cap from player check
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            double maxDist = LumDustConfCache.AMBIENT_HARD_CAP;
            if (player.distanceToSqr(particle.getX(), particle.getY(), particle.getZ()) > maxDist * maxDist) {
                return true;
            }
        }

        return false;
    }
}

