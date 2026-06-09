package com.luminousdust.client.particle.helpers;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.core.BlockPos;

import org.slf4j.Logger;

public class DustLifeManager {

    // Not Ready Yet.

    public static final Long2IntOpenHashMap AMBIENT_COUNTS = new Long2IntOpenHashMap();
    public static int TOTAL_AMBIENT_COUNT = 0;
    public static BlockPos PENDING_POS = null;

    private static final Logger LOGGER = LogUtils.getLogger();

    public static void registerParticle(BlockPos pos) {
        if (pos != null) {
            LOGGER.debug("[Luminous Dust {Debug}] Registered Particle ");

            AMBIENT_COUNTS.addTo(pos.asLong(), 1);
            TOTAL_AMBIENT_COUNT++;
        }
    }

    public static void unregisterParticle(BlockPos pos) {
        if (pos != null) {
            long key = pos.asLong();
            int oldVal = AMBIENT_COUNTS.addTo(key, -1);
            if (oldVal <= 1) {
                AMBIENT_COUNTS.remove(key);
            }
            if (TOTAL_AMBIENT_COUNT > 0) {
                TOTAL_AMBIENT_COUNT--;
            }
        }
    }
}

