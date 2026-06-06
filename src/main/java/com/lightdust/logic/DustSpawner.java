package com.lightdust.logic;

import com.lightdust.client.particle.DustParticle;
import com.lightdust.config.LightDustConfig;
import com.lightdust.init.ParticleInit;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class DustSpawner {
    public static void tick (Level level, Player player, Minecraft mc) {
        int radius = LightDustConfig.AMBIENT_RADIUS.get();
        int maxCap = LightDustConfig.AMBIENT_BLOCK_CAP.get();
        int diffThreshold = LightDustConfig.DAYTIME_LIGHT_DIFF.get();
        int radiusSqr = radius * radius;
        BlockPos playerPos = player.blockPosition();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        long tick = level.getGameTime();
        int tickMod = (int)(tick % 40L);
        long time = level.getDayTime() % 24000L;
        boolean isDay = time < 13000L || time > 23000L;


        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x + y + z) % 40 == tickMod && x * x + y * y + z * z <= radiusSqr) {
                        mutablePos.set(playerPos.getX() + x, playerPos.getY() + y, playerPos.getZ() + z);
                        if (level.dimension() != Level.OVERWORLD || !isDay || !level.canSeeSky(mutablePos)) {
                            long posKey = BlockPos.asLong(mutablePos.getX(), mutablePos.getY(), mutablePos.getZ());
                            int currentCount = DustParticle.AMBIENT_COUNTS.get(posKey);
                            if (currentCount < maxCap) {
                                int blockLight = level.getBrightness(LightLayer.BLOCK, mutablePos);
                                if (blockLight < LightDustConfig.MIN_BLOCK_LIGHT.get()) {
                                    double distToPlayer = mutablePos.distToCenterSqr(player.position());
                                    // Need to add  && DustUtils.isHoldingLight(player) to this. Also better to check other classes for this (I hate this culling.....)
                                    if (distToPlayer < 16.0) {
                                        blockLight = 12;
                                    }
                                }

                                if (blockLight >= LightDustConfig.MIN_BLOCK_LIGHT.get()) {
                                    if (isDay) {
                                        int skyLight = level.getBrightness(LightLayer.SKY, mutablePos);
                                        if (blockLight - skyLight <= diffThreshold) {
                                            continue;
                                        }
                                    }

                                    if (!level.getFluidState(mutablePos).is(FluidTags.WATER)) {
                                        BlockState state = level.getBlockState(mutablePos);
                                        if (state.getCollisionShape(level, mutablePos).isEmpty()) {
                                            int targetCap;
                                            if (blockLight >= 9) {
                                                targetCap = maxCap;
                                            } else if (blockLight >= 7) {
                                                targetCap = Math.max(1, (int)(maxCap * 0.6F));
                                            } else {
                                                targetCap = Math.max(1, (int)(maxCap * 0.3F));
                                            }

                                            if (currentCount < targetCap) {
                                                DustParticle.PENDING_POS = mutablePos.immutable();
                                                int spawnCount = 1;
                                                if (currentCount == 0) {
                                                    spawnCount = 4;
                                                } else if (currentCount < targetCap / 2) {
                                                    spawnCount = 2;
                                                }

                                                spawnCount = Math.min(spawnCount, targetCap - currentCount);

                                                for (int i = 0; i < spawnCount; i++) {
                                                    double px = mutablePos.getX() + level.random.nextDouble();
                                                    double py = mutablePos.getY() + level.random.nextDouble();
                                                    double pz = mutablePos.getZ() + level.random.nextDouble();
                                                    level.addParticle(ParticleInit.DUST_PARTICLE.get(), px, py, pz, 0.0, 0.0, 0.0);
                                                }

                                                DustParticle.PENDING_POS = null;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (tick % 60L == 0L) {
            double hardCap = LightDustConfig.AMBIENT_HARD_CAP.get();
            double pruneDistSqr = (hardCap + 1.0) * (hardCap + 1.0);
            ObjectIterator<Long2IntMap.Entry> iterator = DustParticle.AMBIENT_COUNTS.long2IntEntrySet().iterator();

            while (iterator.hasNext()) {
                long key = iterator.next().getLongKey();
                if (BlockPos.of(key).distSqr(playerPos) > pruneDistSqr) {
                    iterator.remove();
                }
            }
        }

        if (player.swingTime == 1 && mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos breakPos = ((BlockHitResult)mc.hitResult).getBlockPos();
            if (level.getBlockState(breakPos).isAir()) {
                int count = LightDustConfig.BREAK_PARTICLE_COUNT.get();
                double speed = LightDustConfig.BREAK_PARTICLE_SPEED.get();

                for (int i = 0; i < count; i++) {
                    double px = breakPos.getX() + level.random.nextDouble();
                    double py = breakPos.getY() + level.random.nextDouble();
                    double pz = breakPos.getZ() + level.random.nextDouble();
                    double vx = (level.random.nextDouble() - 0.5) * speed;
                    double vy = (level.random.nextDouble() - 0.5) * speed;
                    double vz = (level.random.nextDouble() - 0.5) * speed;
                    level.addParticle(ParticleInit.DUST_PARTICLE.get(), px, py, pz, vx, vy, vz);
                }
            }
        }
    }
}
